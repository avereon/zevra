package com.avereon.data;

import java.util.UUID;

public abstract class IdNode extends Node {

	public static final String ID = "id";

	public IdNode() {
		definePrimaryKey( ID );
		//try( Txn ignore = Txn.create() ) {
			setId( UUID.randomUUID().toString() );
//		} catch( TxnException exception ) {
//			exception.printStackTrace( System.err );
//		}
	}

	public String getId() {
		return getValue( ID );
	}

	public IdNode setId( String id ) {
		setCollectionId( id );
		setValue( ID, id );
		return this;
	}

}
