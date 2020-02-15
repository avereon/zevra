package com.avereon.data;

class NamedNode extends MockNode {

	public NamedNode() {
		defineNaturalKey( "name" );
	}

	public String getName(){
		return getValue( "name" );
	}

	public void setName( String name ) {
		setValue( "name", name );
	}

}
