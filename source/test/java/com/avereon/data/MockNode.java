package com.avereon.data;

import java.util.Set;

class MockNode extends Node {

	public static final String MID = "mid";

	public static final String ITEMS = "items";

	private final NodeWatcher watcher;

	MockNode() {
		this( null );
	}

	MockNode( String id ) {
		definePrimaryKey( MID );
		if( id != null ) setMockId( id );
		addModifyingKeys( ITEMS, "key", "child", "a", "b", "c", "x", "y", "z" );
		register( NodeEvent.ANY, watcher = new NodeWatcher() );
	}

	public String getMockId() {
		return getValue( MID );
	}

	public MockNode setMockId( String id ) {
		setValue( MID, id );
		return this;
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
