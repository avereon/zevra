package com.avereon.data;

import java.util.UUID;

public abstract class IdNode extends Node {

	public static final String ID = "id";

	public IdNode() {
		setId( UUID.randomUUID().toString() );
		definePrimaryKey( ID );
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
