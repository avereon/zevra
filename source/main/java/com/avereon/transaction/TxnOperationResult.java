package com.avereon.transaction;

import java.util.ArrayList;
import java.util.List;

public class TxnOperationResult {

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
