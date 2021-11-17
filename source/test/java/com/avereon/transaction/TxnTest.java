package com.avereon.transaction;

import com.avereon.event.EventType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

class TxnTest {

	@BeforeEach
	void setup() {
		Txn.reset();
	}

	@Test
	void testRun() {
		final AtomicInteger count = new AtomicInteger();
		Txn.run( count::incrementAndGet );
		assertThat( count.get(), is( 1 ) );
	}

	@Test
	void testCall() throws Exception {
		final AtomicInteger count = new AtomicInteger();
		Txn.call( count::incrementAndGet );
		assertThat( count.get(), is( 1 ) );
	}

	@Test
	void testCommit() throws Exception {
		MockTransactionOperation step = new MockTransactionOperation();

		Txn.create();
		Txn.submit( step );
		Txn.commit();

		assertThat( step.getCommitCallCount(), is( 1 ) );
		assertThat( step.getRollbackCallCount(), is( 0 ) );
	}

	@Test
	void testAutoCloseable() throws Exception {
		MockTransactionOperation step = new MockTransactionOperation();

		try( Txn ignored = Txn.create() ) {
			Txn.submit( step );
		}

		assertThat( step.getCommitCallCount(), is( 1 ) );
		assertThat( step.getRollbackCallCount(), is( 0 ) );
	}

	@Test
	void testRollback() throws Exception {
		MockTransactionOperation step1 = new MockTransactionOperation();
		MockTransactionOperation step2 = new MockTransactionOperation();
		MockTransactionOperation step3 = new MockTransactionOperation();
		MockTransactionOperation step4 = new MockTransactionOperation();
		MockTransactionOperation step5 = new MockTransactionOperation();

		step3.setThrowException( new NullPointerException() );

		Txn.create();
		Txn.submit( step1 );
		Txn.submit( step2 );
		Txn.submit( step3 );
		Txn.submit( step4 );
		Txn.submit( step5 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 1 ) );
		assertThat( step1.getRollbackCallCount(), is( 1 ) );
		assertThat( step2.getCommitCallCount(), is( 1 ) );
		assertThat( step2.getRollbackCallCount(), is( 1 ) );
		assertThat( step3.getCommitCallCount(), is( 1 ) );
		assertThat( step3.getRollbackCallCount(), is( 0 ) );
		assertThat( step4.getCommitCallCount(), is( 0 ) );
		assertThat( step4.getRollbackCallCount(), is( 0 ) );
		assertThat( step5.getCommitCallCount(), is( 0 ) );
		assertThat( step5.getRollbackCallCount(), is( 0 ) );
	}

	@Test
	void testReset() throws Exception {
		MockTransactionOperation step = new MockTransactionOperation();

		Txn.create();
		Txn.submit( step );
		Txn.reset();

		assertThat( step.getCommitCallCount(), is( 0 ) );
		assertThat( step.getRollbackCallCount(), is( 0 ) );
	}

	@Test
	void testContinuedTransaction() throws Exception {
		MockTransactionOperation step1 = new MockTransactionOperation();
		MockTransactionOperation step2 = new MockTransactionOperation();
		MockTransactionOperation step3 = new MockTransactionOperation();
		assertThat( step1.getCommitCallCount(), is( 0 ) );
		assertThat( step2.getCommitCallCount(), is( 0 ) );
		assertThat( step3.getCommitCallCount(), is( 0 ) );

		Txn.create();
		Txn.submit( step1 );

		Txn.create();
		Txn.submit( step2 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 0 ) );
		assertThat( step2.getCommitCallCount(), is( 0 ) );
		assertThat( step3.getCommitCallCount(), is( 0 ) );

		Txn.submit( step3 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 1 ) );
		assertThat( step2.getCommitCallCount(), is( 1 ) );
		assertThat( step3.getCommitCallCount(), is( 1 ) );
	}

	@Test
	void testNestedTransaction() throws Exception {
		MockTransactionOperation step1 = new MockTransactionOperation();
		MockTransactionOperation step2 = new MockTransactionOperation();
		MockTransactionOperation step3 = new MockTransactionOperation();
		assertThat( step1.getCommitCallCount(), is( 0 ) );
		assertThat( step2.getCommitCallCount(), is( 0 ) );
		assertThat( step3.getCommitCallCount(), is( 0 ) );

		Txn.create();
		Txn.submit( step1 );

		Txn.create( true );
		Txn.submit( step2 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 0 ) );
		assertThat( step2.getCommitCallCount(), is( 1 ) );
		assertThat( step3.getCommitCallCount(), is( 0 ) );

		Txn.submit( step3 );
		Txn.commit();

		assertThat( step1.getCommitCallCount(), is( 1 ) );
		assertThat( step2.getCommitCallCount(), is( 1 ) );
		assertThat( step3.getCommitCallCount(), is( 1 ) );
	}

	@Test
	void testCommitWithoutTransaction() {
		try {
			Txn.commit();
			fail( "Commit should throw an exception if there is not an active transaction" );
		} catch( TxnException exception ) {
			// Pass
		}
	}

	@Test
	void testSubmitWithoutTransaction() {
		MockTransactionOperation step = new MockTransactionOperation();
		try {
			Txn.submit( step );
			fail( "Submit should throw an exception if there is not an active transaction" );
		} catch( TxnException exception ) {
			// Pass
		}
	}

	@Test
	void testResetWithoutTransaction() {
		assertThat( Txn.getActiveTransaction(), is( nullValue() ) );
		Txn.reset();
	}

	@Test
	void testTxnEventsWithSingleTransaction() throws Exception {
		MockTxnEventTarget target = new MockTxnEventTarget();

		Txn.create();
		Txn.submit( new MockTransactionOperation( target ) );
		Txn.submit( new MockTransactionOperation( target ) );
		Txn.submit( new MockTransactionOperation( target ) );
		assertThat( target.getEvents().size(), is( 0 ) );
		Txn.commit();

		int index = 0;
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_BEGIN ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( MockTxnEvent.MODIFIED ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( MockTxnEvent.MODIFIED ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( MockTxnEvent.MODIFIED ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_SUCCESS ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_END ) );
		assertThat( target.getEvents().size(), is( index ) );
	}

	@Test
	void testTxnEventsWithMultipleTransactions() throws Exception {
		MockTxnEventTarget target = new MockTxnEventTarget();

		Txn.create();
		Txn.submit( new MockTransactionOperation( target ) );
		Txn.commit();
		Txn.create();
		Txn.submit( new MockTransactionOperation( target ) );
		Txn.commit();
		Txn.create();
		Txn.submit( new MockTransactionOperation( target ) );
		Txn.commit();

		int index = 0;
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_BEGIN ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( MockTxnEvent.MODIFIED ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_SUCCESS ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_END ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_BEGIN ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( MockTxnEvent.MODIFIED ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_SUCCESS ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_END ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_BEGIN ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( MockTxnEvent.MODIFIED ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_SUCCESS ) );
		assertThat( target.getEvents().get( index++ ).getEventType(), Matchers.is( TxnEvent.COMMIT_END ) );
		assertThat( target.getEvents().size(), is( index ) );
	}

	@Test
	void testTxnWithConsumerOperation() throws Exception {
		MockTxnEventTarget target = new MockTxnEventTarget();
		AtomicInteger count = new AtomicInteger();
		assertThat( count.get(), is( 0 ) );

		Txn.create();
		Txn.submit( target, t -> count.incrementAndGet() );
		// The value should not have changed until after commit
		assertThat( count.get(), is( 0 ) );
		Txn.commit();

		assertThat( count.get(), is( 1 ) );
	}

	private static class MockTxnEventTarget implements TxnEventTarget {

		private final List<TxnEvent> events;

		MockTxnEventTarget() {
			events = new CopyOnWriteArrayList<>();
		}

		@Override
		public void dispatch( TxnEvent event ) {
			events.add( event );
		}

		public List<TxnEvent> getEvents() {
			return events;
		}
	}

	private static class MockTransactionOperation extends TxnOperation {

		private int commitCallCount;

		private int rollbackCallCount;

		private Throwable throwable;

		protected MockTransactionOperation() {
			super( new MockTxnEventTarget() );
		}

		protected MockTransactionOperation( TxnEventTarget target ) {
			super( target );
		}

		@Override
		public MockTxnEventTarget getTarget() {
			return (MockTxnEventTarget)super.getTarget();
		}

		@Override
		protected MockTransactionOperation commit() throws TxnException {
			commitCallCount++;
			getResult().addEvent( getTarget(), new MockTxnEvent( getTarget(), MockTxnEvent.MODIFIED ) );
			if( throwable != null ) throw new TxnException( throwable );
			return this;
		}

		@Override
		protected MockTransactionOperation revert() {
			rollbackCallCount++;
			return this;
		}

		int getCommitCallCount() {
			return commitCallCount;
		}

		int getRollbackCallCount() {
			return rollbackCallCount;
		}

		void setThrowException( Throwable throwable ) {
			this.throwable = throwable;
		}
	}

	private static class MockTxnEvent extends TxnEvent {

		public static final EventType<MockTxnEvent> ANY = new EventType<>( EventType.ROOT.getName() );

		public static final EventType<MockTxnEvent> MODIFIED = new EventType<>( "MODIFIED" );

		/**
		 * Create a TxnEvent where the source and target are the same object. This is
		 * a common pattern where the eventual target of the event is the same object
		 * that is creating it.
		 *
		 * @param source The event source/target
		 * @param type
		 */
		public MockTxnEvent( TxnEventTarget source, EventType<? extends TxnEvent> type ) {
			super( source, type );
		}

	}

}
