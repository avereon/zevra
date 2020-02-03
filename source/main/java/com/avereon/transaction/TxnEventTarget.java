package com.avereon.transaction;

public interface TxnEventTarget {

	void dispatch( TxnEvent event );

}
