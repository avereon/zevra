package com.avereon.data;

import java.util.Set;

class MockNode extends IdNode {

	public static final String ITEMS = "items";

	private final NodeWatcher watcher;

	MockNode() {
		this( null );
	}

	MockNode( String id ) {
		if( id != null ) setId( id );
		addModifyingKeys( "key", "child", "a", "b", "x", "y", "z" );
		register( NodeEvent.ANY, watcher = new NodeWatcher() );
	}

	public Set<MockNode> getItems() {
		return getValue( ITEMS );
	}

	public MockNode addItem( MockNode item ) {
		addToSet( ITEMS, item );
		return this;
	}

	public MockNode removeItem( MockNode item ) {
		removeFromSet( ITEMS, item );
		return this;
	}

	public NodeWatcher getWatcher() {
		return watcher;
	}

	public int getEventCount() {
		return watcher.getEvents().size();
	}

}
