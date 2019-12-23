package com.avereon.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventHub<T extends Event> implements EventHandler<T> {

	private Map<EventType<? extends Event>, EventHandler<T>> handlers;

	public EventHub() {
		handlers = new ConcurrentHashMap<>();
	}

	@Override
	public void handle( T event ) {
		EventType<? extends Event> type = event.getEventType();
		while( type != null ) {
			handlers.getOrDefault( type, ( e ) -> {} ).handle( event );
			type = type.getParentEventType();
		}
	}

	public EventHub<T> register( final EventType<? extends Event> type, final EventHandler<T> handler ) {
		handlers.put( type, handler );
		return this;
	}

	public EventHub<T> unregister( final EventType<? extends Event> type ) {
		handlers.remove( type );
		return this;
	}

}
