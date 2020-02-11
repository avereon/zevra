package com.avereon.data;

import com.avereon.event.EventType;
import com.avereon.transaction.TxnEvent;

import java.util.Objects;

public class NodeEvent extends TxnEvent {

	public static final EventType<NodeEvent> VALUE_CHANGED = new EventType<>( "VALUE_CHANGED" );

	public static final EventType<NodeEvent> CHILD_ADDED = new EventType<>( "CHILD_ADDED" );

	public static final EventType<NodeEvent> CHILD_REMOVED = new EventType<>( "CHILD_REMOVED" );

	public static final EventType<NodeEvent> MODIFIED = new EventType<>( "MODIFIED" );

	public static final EventType<NodeEvent> UNMODIFIED = new EventType<>( "UNMODIFIED" );

	public static final EventType<NodeEvent> NODE_CHANGED = new EventType<>( "NODE_CHANGED" );

	// TODO Should there be PARENT_CHANGED events???

	private Node node;

	private String key;

	private Object oldValue;

	private Object newValue;

	public NodeEvent( Node node, EventType<? extends NodeEvent> type ) {
		this( node, type, null, null, null );
	}

	public NodeEvent( Node node, EventType<? extends NodeEvent> type, String key, Object oldValue, Object newValue ) {
		super( node, type );
		this.node = node;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Node getNode() {
		return node;
	}

	public String getKey() {
		return key;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
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
		builder.append( "node=" );
		builder.append( node );
		builder.append( ", type=" );
		builder.append( getEventType() );
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

		if( node != null ) code |= node.hashCode();
		if( getEventType() != null ) code |= getEventType().hashCode();
		if( key != null ) code |= key.hashCode();

		return code;
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof NodeEvent) ) return false;

		NodeEvent that = (NodeEvent)object;
		if( !Objects.equals( this.node, that.node ) ) return false;
		if( !Objects.equals( this.getEventType(), that.getEventType() ) ) return false;
		return Objects.equals( this.key, that.key );
	}

}
