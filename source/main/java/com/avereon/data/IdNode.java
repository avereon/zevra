package com.avereon.data;

import com.avereon.transaction.Txn;

import java.util.UUID;

public abstract class IdNode extends Node {

	public static final String ID = "id";

	public IdNode() {
		definePrimaryKey( ID );
		setId( UUID.randomUUID().toString() );
	}

	public String getId() {
		return getValue( ID );
	}

	@SuppressWarnings( "unchecked" )
	public <T> T setId( String id ) {
		Txn.run( () -> {
			setCollectionId( id );
			setValue( ID, id );
		} );
		return (T)this;

	}

}
