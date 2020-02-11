package com.avereon.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus implements EventDispatcher {

	private EventBus parent;

	private Map<EventType<? extends Event>, Collection<EventHandler<Event>>> handlers;

	public EventBus() {
		handlers = new ConcurrentHashMap<>();
	}

	@Override
	public Event dispatch( Event event ) {
		// While the type of the incoming event is known, the parent event types,
		// used later in the method are not well known. They could be of any event
		// type and therefore this variable needs to allow any event type.
		EventType<?> type = event.getEventType();

		// Go through all the handlers of the event type and all handlers of all
		// the parent event types, passing the event to each handler.
		while( type != null ) {
			Collection<EventHandler<Event>> typeHandlers = handlers.get( type );
			if( typeHandlers != null ) typeHandlers.forEach( handler -> handler.handle( event ) );
			type = type.getParentEventType();
		}

		// If there is a parent event hub, pass the event to it
		if( parent != null ) parent.dispatch( event );

		return event;
	}

	public EventBus parent( EventBus parent ) {
		this.parent = parent;
		return this;
	}

	@SuppressWarnings( "unchecked" )
	public <T extends Event> EventBus register( EventType<? super T> type, EventHandler<? super T> handler ) {
		handlers.computeIfAbsent( type, k -> new HashSet<>() ).add( (EventHandler<Event>)handler );
		return this;
	}

	public <T extends Event> EventBus unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		handlers.computeIfPresent( type, ( t, c ) -> {
			c.removeIf( w -> w == handler );
			return c.isEmpty() ? null : c;
		} );
		return this;
	}

	public Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		return new HashMap<>( handlers );
	}

}
