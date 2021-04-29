package com.avereon.transaction;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class TxnEvent extends Event {

	public static final EventType<TxnEvent> ANY = new EventType<>( EventType.ROOT, "TXN" );

	public static final EventType<TxnEvent> COMMIT_BEGIN = new EventType<>( ANY, "COMMIT_BEGIN" );

	public static final EventType<TxnEvent> COMMIT_SUCCESS = new EventType<>( ANY, "COMMIT_SUCCESS" );

	public static final EventType<TxnEvent> COMMIT_FAIL = new EventType<>( ANY, "COMMIT_FAIL" );

	public static final EventType<TxnEvent> COMMIT_END = new EventType<>( ANY, "COMMIT_END" );

	/**
	 * Create a TxnEvent where the source and target are the same object. This is
	 * a common pattern where the eventual target of the event is the same object
	 * that is creating it.
	 *
	 * @param source The event source/target
	 */
	public TxnEvent( TxnEventTarget source, EventType<? extends TxnEvent> type ) {
		super( source, type );
	}

	public boolean collapseUp() {
		return false;
	}

}
