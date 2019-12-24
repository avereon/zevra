package com.avereon.event;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventHub<T extends Event> implements EventHandler<T> {

	private EventHub<? super T> parent;

	private Map<EventType<?>, Collection<EventHandler<? super T>>> handlers;

	public EventHub() {
		handlers = new ConcurrentHashMap<>();
	}

	public EventHub( EventHub<? super T> parent ) {
		this();
		parent( parent );
	}

	@Override
	public void handle( T event ) {
		// While the type of the incoming event is known, the parent event types,
		// used later in the method are not well known. They could be of any event
		// type and therefore this variable needs to allow any event type.
		EventType<?> type = event.getEventType();

		// Go through all the handlers of the event type and all handlers of all
		// the parent event types, passing the event to each handler.
		while( type != null ) {
			handlers.computeIfPresent( type, ( t, c ) -> {
				c.forEach( h -> h.handle( event ) );
				return c;
			} );
			type = type.getParentEventType();
		}

		// If there is a parent event hub, pass the event to it
		if( parent != null ) parent.handle( event );
	}

	public EventHub<T> parent( EventHub<? super T> parent ) {
		this.parent = parent;
		return this;
	}

	public EventHub<T> register( final EventType<?> type, final EventHandler<? super T> handler ) {
		handlers.computeIfAbsent( type, k -> new HashSet<>() ).add( handler );
		return this;
	}

	public EventHub<T> unregister( final EventType<?> type, final EventHandler<? super T> handler ) {
		handlers.computeIfPresent( type, ( t, c ) -> {
			c.remove( handler );
			return c.isEmpty() ? null : c;
		} );
		return this;
	}

}
