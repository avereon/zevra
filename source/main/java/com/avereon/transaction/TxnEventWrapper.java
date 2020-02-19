package com.avereon.transaction;

class TxnEventWrapper {

	private TxnEventTarget target;

	private TxnEvent event;

	TxnEventWrapper( TxnEventTarget target, TxnEvent event ) {
		if( target == null ) throw new NullPointerException( "Target cannot be null" );
		if( event == null ) throw new NullPointerException( "Event cannot be null" );
		this.target = target;
		this.event = event;
	}

	public TxnEventTarget getTarget() {
		return target;
	}

	public TxnEvent getEvent() {
		return event;
	}

}
