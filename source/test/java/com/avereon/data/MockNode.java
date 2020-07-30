package com.avereon.data;

class MockNode extends Node {

	private final NodeWatcher watcher;

	MockNode() {
		this( null );
	}

	MockNode( String id ) {
		definePrimaryKey( "id" );
		addModifyingKeys( "id", "key", "child", "a", "b", "x", "y", "z" );

		setId( id );
		setModified( false );
		register( NodeEvent.ANY, watcher = new NodeWatcher() );
	}

	private String getId() {
		return getValue( "id" );
	}

	private void setId( String id ) {
		setValue( "id", id );
	}

	public NodeWatcher getWatcher() {
		return watcher;
	}

	int getEventCount() {
		return watcher.getEvents().size();
	}

}
