package com.avereon.data;

import com.avereon.event.Event;

import java.util.Collection;
import java.util.Set;

class MockNode extends Node {

	public static final String MOCK_ID = "mock-id";

	public static final String ITEMS = "items";

	private final NodeWatcher watcher;

	MockNode() {
		this( null );
	}

	MockNode( String id ) {
		definePrimaryKey( MOCK_ID );
		if( id != null ) setMockId( id );
		addModifyingKeys( ITEMS, "key", "child", "a", "b", "c", "x", "y", "z" );
		register( Event.ANY, watcher = new NodeWatcher() );
	}

	public String getMockId() {
		return getValue( MOCK_ID );
	}

	public MockNode setMockId( String id ) {
		setValue( MOCK_ID, id );
		return this;
	}

	/**
	 * This method follows the pattern documented in {@link NodeSet}
	 *
	 * @return The set of items
	 */
	public Set<MockNode> getItems() {
		return getValues( ITEMS );
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

	public MockNode addItems( Collection<MockNode> items ) {
		addToSet( ITEMS, items );
		return this;
	}

	public MockNode removeItems( Collection<MockNode> items ) {
		removeFromSet( ITEMS, items );
		return this;
	}

	public MockNode clearItems() {
		clearSet( ITEMS );
		return this;
	}

	public Event event(int index ) {
		return watcher.getEvents().get(index);
	}

	public NodeWatcher getWatcher() {
		return watcher;
	}

	public int getEventCount() {
		return watcher.getEvents().size();
	}

}
