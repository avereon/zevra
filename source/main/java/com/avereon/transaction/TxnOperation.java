package com.avereon.transaction;

/**
 * A single Txn operation that is processed during the commit process and
 * generates a result that is collected and published when the commit
 * successfully completes.
 */
public abstract class TxnOperation {

	public enum Status {
		WAITING,
		COMMITTING,
		COMMITTED,
		REVERTING,
		REVERTED,
		FAILED
	}

	private final TxnEventTarget target;

	private final TxnOperationResult result;

	private TxnOperation.Status status;

	protected TxnOperation( TxnEventTarget target ) {
		this.target = target;
		result = new TxnOperationResult( this );
		status = Status.WAITING;
	}

	protected abstract void commit() throws TxnException;

	protected abstract void revert() throws TxnException;

	public TxnEventTarget getTarget() {
		return target;
	}

	public TxnOperationResult getResult() {
		return result;
	}

	Status getStatus() {
		return status;
	}

	final TxnOperationResult callCommit() throws TxnException {
		try {
			status = Status.COMMITTING;
			commit();
			status = Status.COMMITTED;
		} catch( TxnException exception ) {
			status = Status.FAILED;
			throw exception;
		}
		return getResult();
	}

	final void callRevert() throws TxnException {
		try {
			status = Status.REVERTING;
			revert();
			status = Status.REVERTED;
		} catch( TxnException exception ) {
			status = Status.FAILED;
			throw exception;
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + System.identityHashCode( this ) + "]";
	}

}
