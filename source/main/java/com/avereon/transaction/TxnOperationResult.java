package com.avereon.transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * The results of a completed transaction operation. These results are collected
 * as part of the commit operation and the events are distributed when the
 * commit is successful.
 */
class TxnOperationResult {

	private TxnOperation action;

	private List<TxnEventWrapper> events = new ArrayList<>();

	TxnOperationResult( TxnOperation action ) {
		this.action = action;
	}

	public TxnOperation getOperation() {
		return action;
	}

	public List<TxnEventWrapper> getEvents() {
		return events;
	}

	public void addEvent( TxnEventTarget target, TxnEvent event ) {
		events.add( new TxnEventWrapper( target, event ) );
	}

}
