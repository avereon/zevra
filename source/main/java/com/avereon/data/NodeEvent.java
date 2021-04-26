package com.avereon.data;

import com.avereon.event.EventType;
import com.avereon.transaction.TxnEvent;

import java.util.Objects;

public class NodeEvent extends TxnEvent {

	public static final EventType<NodeEvent> MODIFIED = new EventType<>( "MODIFIED" );

	public static final EventType<NodeEvent> UNMODIFIED = new EventType<>( "UNMODIFIED" );

	public static final EventType<NodeEvent> ADDED = new EventType<>( "ADDED" );

	public static final EventType<NodeEvent> REMOVED = new EventType<>( "REMOVED" );

	public static final EventType<NodeEvent> CHILD_ADDED = new EventType<>( "CHILD_ADDED" );

	public static final EventType<NodeEvent> CHILD_REMOVED = new EventType<>( "CHILD_REMOVED" );

	public static final EventType<NodeEvent> NODE_CHANGED = new EventType<>( "NODE_CHANGED" );

	public static final EventType<NodeEvent> PARENT_CHANGED = new EventType<>( "PARENT_CHANGED" );

	public static final EventType<NodeEvent> VALUE_CHANGED = new EventType<>( "VALUE_CHANGED" );

	private final String key;

	private final Object oldValue;

	private final Object newValue;

	private final boolean undoable;

	public NodeEvent( Node node, EventType<? extends NodeEvent> type ) {
		this( node, type, null, null, null );
	}

	public NodeEvent( Node node, EventType<? extends NodeEvent> type, String key, Object oldValue, Object newValue ) {
		this( node, type, key, oldValue, newValue, true );
	}

	public NodeEvent( Node node, EventType<? extends NodeEvent> type, String key, Object oldValue, Object newValue, boolean undoable ) {
		super( node, type );
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
		this.undoable = undoable;
	}

	@SuppressWarnings( "unchecked" )
	public <T extends Node> T getNode() {
		return (T)getSource();
	}

	public String getKey() {
		return key;
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getOldValue() {
		return (T)oldValue;
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getNewValue() {
		return (T)newValue;
	}

	public boolean collapseUp() {
		return getEventType() == NodeEvent.VALUE_CHANGED;
	}

	public boolean isUndoable() {
		return undoable;
	}

	@SuppressWarnings( "unchecked" )
	public EventType<? extends NodeEvent> getEventType() {
		return (EventType<? extends NodeEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append( getClass().getSimpleName() );
		builder.append( "[ " );
		builder.append( "type=" );
		builder.append( getEventType() );
		builder.append( ", node=" );
		builder.append( (Node)getNode() );
		if( key != null ) {
			builder.append( ", key=" );
			builder.append( key );
			builder.append( ", oldValue=" );
			builder.append( oldValue );
			builder.append( ", newValue=" );
			builder.append( newValue );
		}
		builder.append( " ]" );

		return builder.toString();
	}

	@Override
	public int hashCode() {
		int code = 0;

		if( getNode() != null ) code |= getNode().hashCode();
		if( getEventType() != null ) code |= getEventType().hashCode();
		if( key != null ) code |= key.hashCode();

		return code;
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof NodeEvent) ) return false;

		NodeEvent that = (NodeEvent)object;
		return Objects.equals( this.getNode(), that.getNode() ) && Objects.equals( this.getEventType(), that.getEventType() ) && Objects.equals( this.key, that.key );
	}

}
