package com.avereon.data;

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
		// NOTE Collection ID should not be part of a transaction
		setCollectionId( id );
		setValue( ID, id );
		return (T)this;
	}

}
