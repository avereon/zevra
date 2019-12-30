package com.avereon.event;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus implements EventDispatcher {

	private EventBus parent;

	private Map<EventType<? extends Event>, Collection<EventHandlerWrapper<?>>> handlers;

	public EventBus() {
		this( null );
	}

	public EventBus( EventBus parent ) {
		handlers = new ConcurrentHashMap<>();
		parent( parent );
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
			handlers.computeIfPresent( type, ( t, handlers ) -> {
				handlers.forEach( handler -> handler.dispatch( event ) );
				return handlers;
			} );
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

	public <T extends Event> EventBus register( EventType<T> type, EventHandler<T> handler ) {
		handlers.computeIfAbsent( type, k -> new HashSet<>() ).add( new EventHandlerWrapper<>( handler ) );
		return this;
	}

	public <T extends Event> EventBus unregister( EventType<T> type, EventHandler<T> handler ) {
		handlers.computeIfPresent( type, ( t, c ) -> {
			c.removeIf( w -> w.getHandler() == handler );
			return c.isEmpty() ? null : c;
		} );
		return this;
	}

	private static class EventHandlerWrapper<T extends Event> implements EventDispatcher, EventHandler<T> {

		private EventHandler<T> handler;

		public EventHandlerWrapper( EventHandler<T> handler ) {
			this.handler = handler;
		}

		public EventHandler<T> getHandler() {
			return handler;
		}

		@Override
		@SuppressWarnings( "unchecked" )
		public Event dispatch( Event event ) {
			handle( (T)event );
			return event;
		}

		@Override
		public void handle( T event ) {
			handler.handle( event );
		}

	}

}
