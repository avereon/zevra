package com.avereon.transaction;

import com.avereon.event.EventType;
import com.avereon.util.Log;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * Txn is a transaction utility. Transactions are thread local for convenience
 * since most transactions are created and committed on the same thread.
 * Creating a new transaction will append to an existing transaction unless the
 * transaction is explicitly created as a nested transaction. Example:
 * <pre>
 *   ...
 *   public void setPoint( double x, double y ) {
 *     // Note that SetX and SetY are implementations of TxnOperation
 *     Txn.create();
 *     Txn.submit( new SetX( x ) );
 *     Txn.submit( new SetY( y ) );
 *     Txn.commit();
 *   }
 *   ...
 * </pre>
 */
public class Txn implements AutoCloseable {

	private static final System.Logger log = Log.get();

	private static final ThreadLocal<Deque<Txn>> transactions = new ThreadLocal<>();

	private final ReentrantLock commitLock = new ReentrantLock();

	private final Queue<TxnOperation> operations;

	private int depth;

	static {
		transactions.set( new ArrayDeque<>() );
	}

	private Txn() {
		operations = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Create a transaction for this thread if there is not already an active
	 * transaction. If there is already an active transaction then the active
	 * transaction is returned, otherwise a new transaction is created and
	 * returned.
	 *
	 * @return The transaction
	 */
	public static Txn create() {
		return create( false );
	}

	/**
	 * Create a transaction for this thread if there is not already an active
	 * transaction or the nest flag is set to intentionally start a nested
	 * transaction instead of using an existing transaction.
	 * <p/>
	 * Note that the nested transaction becomes the new active transaction until
	 * it is completed (committed or reset) and therefore nested transactions must
	 * be completed before the outer transaction can be completed.
	 *
	 * @param nest Set to true if this transaction should not be part of an
	 * existing transaction.
	 * @return The transaction
	 */
	public static Txn create( boolean nest ) {
		Txn transaction = peekTransaction();
		if( transaction == null || nest ) transaction = pushTransaction();
		transaction.incrementDepth();

		return transaction;
	}

	public static void submit( TxnOperation operation ) throws TxnException {
		verifyActiveTransaction().operations.offer( operation );
	}

	public static void submit( TxnEventTarget target, Consumer<TxnEventTarget> consumer ) throws TxnException {
		submit( new ConsumerTxnOperation( target, consumer ) );
	}

	private static class ConsumerTxnOperation extends TxnOperation {

		private final Consumer<TxnEventTarget> consumer;

		public ConsumerTxnOperation( TxnEventTarget target, Consumer<TxnEventTarget> consumer ) {
			super( target );
			this.consumer = consumer;
		}

		@Override
		protected void commit() throws TxnException {
			consumer.accept( getTarget() );
		}

		@Override
		protected void revert() throws TxnException {}

	}

	public static void commit() throws TxnException {
		Txn transaction = verifyActiveTransaction();
		transaction.decrementDepth();
		if( transaction.isActive() ) return;
		try {
			transaction.doCommit();
		} finally {
			pullTransaction();
		}
	}

	@Override
	public void close() throws TxnException {
		commit();
	}

	public static void reset() {
		Txn transaction = peekTransaction();
		if( transaction == null ) return;
		if( --transaction.depth > 0 ) return;

		while( transaction != null ) {
			transaction.doReset();
			transaction = pullTransaction();
		}
	}

	public static Txn getActiveTransaction() {
		return peekTransaction();
	}

	boolean isActive() {
		return depth > 0;
	}

	void incrementDepth() {
		depth++;
	}

	void decrementDepth() {
		depth--;
	}

	private static Txn verifyActiveTransaction() throws TxnException {
		Txn transaction = peekTransaction();
		if( transaction == null ) throw new TxnException( "No active transaction" );
		return transaction;
	}

	private static Txn peekTransaction() {
		Deque<Txn> deque = transactions.get();
		return deque == null ? null : deque.peekFirst();
	}

	private static Txn pushTransaction() {
		Deque<Txn> deque = transactions.get();
		if( deque == null ) transactions.set( deque = new ArrayDeque<>() );
		Txn transaction = new Txn();
		deque.offerFirst( transaction );
		return transaction;
	}

	private static Txn pullTransaction() {
		Deque<Txn> deque = transactions.get();
		if( deque == null ) return null;
		Txn transaction = deque.pollFirst();
		if( deque.size() == 0 ) transactions.set( null );
		return transaction;
	}

	private void doCommit() throws TxnException {
		Set<TxnOperation> operations = new HashSet<>( this.operations );

		try {
			commitLock.lock();
			log.log( Log.TRACE, System.identityHashCode( this ) + " locked by: " + Thread.currentThread() );

			// Send a commit begin event to all unique targets
			sendEvent( TxnEvent.COMMIT_BEGIN, operations );

			// Process all the operations
			List<TxnOperationResult> operationResults = new ArrayList<>( processOperations() );

			// Go through each operation result and collect the events by target
			// This process also removes duplicate events and puts them in the correct order
			Map<TxnEventTarget, List<TxnEvent>> txnEvents = new HashMap<>();
			for( TxnOperationResult operationResult : operationResults ) {
				for( TxnEventWrapper wrapper : operationResult.getEvents() ) {
					TxnEventTarget target = wrapper.getTarget();
					TxnEvent event = wrapper.getEvent();
					List<TxnEvent> events = txnEvents.computeIfAbsent( target, k -> new ArrayList<>() );
					events.remove( event );
					events.add( event );
				}
			}

			sendEvent( TxnEvent.COMMIT_SUCCESS, operations );

			// Dispatch the events to the targets
			txnEvents.forEach( ( target, events ) -> events.forEach( event -> {
				try {
					target.dispatch( event );
				} catch( Throwable throwable ) {
					log.log( Log.ERROR, "Error dispatching transaction event", throwable );
				}
			} ) );
		} catch( TxnException throwable ) {
			sendEvent( TxnEvent.COMMIT_FAIL, operations );
			throw throwable;
		} finally {
			sendEvent( TxnEvent.COMMIT_END, operations );
			doReset();
			commitLock.unlock();
			log.log( Log.TRACE, "Transaction[" + System.identityHashCode( this ) + "] committed!" );
		}
	}

	/**
	 * Send an event to all unique targets.
	 *
	 * @param type The event type
	 */
	private void sendEvent( EventType<? extends TxnEvent> type, Collection<TxnOperation> operations ) {
		operations.stream().map( TxnOperation::getTarget ).distinct().forEach( t -> t.dispatch( new TxnEvent( t, type ) ) );
	}

	private List<TxnOperationResult> processOperations() throws TxnException {
		// Process the operations.
		List<TxnOperationResult> operationResults = new ArrayList<>();
		List<TxnOperation> completedOperations = new ArrayList<>();
		try {
			TxnOperation operation;
			while( (operation = operations.poll()) != null ) {
				operationResults.add( operation.callCommit() );
				completedOperations.add( operation );
			}
		} catch( TxnException commitException ) {
			try {
				for( TxnOperation operation : completedOperations ) {
					if( operation.getStatus() == TxnOperation.Status.COMMITTED ) operation.callRevert();
				}
			} catch( TxnException rollbackException ) {
				throw new TxnException( "Error rolling back transaction", rollbackException );
			}
		}
		return operationResults;
	}

	private void doReset() {
		operations.clear();
	}

}
