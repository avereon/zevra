package com.avereon.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventHub<T extends Event> implements EventHandler<T> {

	private Map<EventType, EventHandler<T>> handlers;

	public EventHub() {
		handlers = new ConcurrentHashMap<>();
	}

	@Override
	public void handle( T event ) {
		EventType type = event.getEventType();
		while( type != null ) {
			handlers.getOrDefault( type, ( e ) -> {} ).handle( event );
			type = type.getParentEventType();
		}
	}

	public EventHub<T> register( EventType type, EventHandler<T> handler ) {
		handlers.put( type, handler );
		return this;
	}

	public EventHub<T> unregister( EventType type ) {
		handlers.remove( type );
		return this;
	}

}
