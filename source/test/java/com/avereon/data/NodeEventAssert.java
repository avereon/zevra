package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.event.EventType;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class NodeEventAssert extends AbstractAssert<NodeEventAssert, Event> {

	public static NodeEventAssert assertThat( Event actual ) {
		return new NodeEventAssert( actual );
	}

	public static NodeEventAssert assertThat( MockNode node, int index ) {
		return new NodeEventAssert( node.getWatcher().getEvents().get( index ) );
	}

	protected NodeEventAssert( Event actual ) {
		super( actual, NodeEventAssert.class );
	}

	public NodeEventAssert hasEventState( EventType<? extends Event> type ) {
		return hasEventState( null, type, null, null, null, null );
	}

	public NodeEventAssert hasEventState( EventType<? extends Event> type, String key, Object oldValue, Object newValue ) {
		return hasEventState( null, type, null, key, oldValue, newValue );
	}

	public NodeEventAssert hasEventState( Node node, EventType<? extends Event> type ) {
		return hasEventState( node, type, null, null, null, null );
	}

	public NodeEventAssert hasEventState( Node node, EventType<? extends Event> type, String key, Object oldValue, Object newValue ) {
		return hasEventState( node, type, null, key, oldValue, newValue );
	}

	public NodeEventAssert hasEventState( Node node, EventType<? extends Event> type, String setKey, String key, Object oldValue, Object newValue ) {
		if( node != null && !Objects.equals( node, actual.getSource() ) ) failWithMessage( "Expected node to be %s but was %s", node, actual.getSource() );
		if( type != null && !Objects.equals( type, actual.getEventType() ) ) failWithMessage( "Expected type to be %s but was %s", type, actual.getEventType() );
		if( actual instanceof NodeEvent ) {
			NodeEvent actual = (NodeEvent)this.actual;
			if( setKey != null && !Objects.equals( setKey, actual.getSetKey() ) ) failWithMessage( "Expected setKey to be %s but was %s", setKey, actual.getSetKey() );
			if( key != null && !Objects.equals( key, actual.getKey() ) ) failWithMessage( "Expected key to be %s but was %s", key, actual.getKey() );
			if( oldValue != null && !Objects.equals( oldValue, actual.getOldValue() ) ) failWithMessage( "Expected oldValue to be %s but was %s", oldValue, actual.getOldValue() );
			if( newValue != null && !Objects.equals( newValue, actual.getNewValue() ) ) failWithMessage( "Expected newValue to be %s but was %s", newValue, actual.getNewValue() );
		}
		return this;
	}

}
