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
		addModifyingKeys( "key", "child", "a", "b", "x", "y", "z", ITEMS );
		register( NodeEvent.ANY, watcher = new NodeWatcher() );
	}

	/**
	 * This method follows the pattern documented in {@link NodeSet}
	 *
	 * @return The set of items
	 */
	public Set<MockNode> getItems() {
		return getValue( ITEMS );
	}

	/**
	 * This method follows the pattern documented in {@link NodeSet}
	 *
	 * @param item The item to add
	 * @return This node
	 */
	public MockNode addItem( MockNode item ) {
		addToSet( ITEMS, item );
		return this;
	}

	/**
	 * This method follows the pattern documented in {@link NodeSet}
	 *
	 * @param item The item to remove
	 * @return This node
	 */
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
