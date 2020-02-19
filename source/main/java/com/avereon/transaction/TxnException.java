package com.avereon.transaction;

@SuppressWarnings( { "WeakerAccess" } )
public class TxnException extends Exception {

	public TxnException() {
		super();
	}

	public TxnException( String message ) {
		super( message );
	}

	public TxnException( String message, Throwable cause ) {
		super( message, cause );
	}

	public TxnException( Throwable cause ) {
		super( cause );
	}

}
