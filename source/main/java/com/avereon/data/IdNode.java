package com.avereon.data;

public abstract class IdNode extends Node {

	public static final String ID = "id";

	public IdNode() {
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
