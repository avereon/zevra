package com.avereon.event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class EventHub {

	private static final EventHub ROOT = new EventHub();

	private final Set<EventHub> peers;

	private final Map<EventType<? extends Event>, Map<EventHandler<Event>, EventHandler<Event>>> handlers;

	private final Map<Class<? extends Event>, Event> priorEvent;

	private EventHub parent = ROOT;

	public EventHub() {
		this.peers = new CopyOnWriteArraySet<>();
		this.handlers = new ConcurrentHashMap<>();
		this.priorEvent = new ConcurrentHashMap<>();
	}

	public Event dispatch( Event event ) {
		// While the type of the incoming event is known, the parent event types,
		// used later in the method are not well known. They could be of any event
		// type and therefore this variable needs to allow any event type.
		EventType<?> type = event.getEventType();

		priorEvent.put( event.getClass(), event );

		// Go through all the handlers of the event type and all handlers of all
		// the parent event types, passing the event to each handler.
		while( type != null ) {
			// If a ConcurrentModificationException occurs, try again until a clean
			// copy of the map can be generated.
			ConcurrentModificationException exception;
			do {
				try {
					exception = null;

					var typeHandlers = new HashMap<>( handlers.getOrDefault( type, Map.of() ) );
					try {
						typeHandlers.forEach( ( k, v ) -> v.handle( event ) );
					} catch( RuntimeException re ) {
						System.err.println( "event type=" + type.getClass() );


						throw re;
					}
					type = type.getParentEventType();
				} catch( ConcurrentModificationException cme ) {
					exception = cme;
				}
			} while( exception != null );
		}

		// Dispatch the event to the peer hubs
		peers.forEach( p -> p.dispatch( event ) );

		// Dispatch the event to the parent hub
		if( this != ROOT ) parent.dispatch( event );

		return event;
	}

	public EventHub parent( EventHub parent ) {
		this.parent = parent == null ? ROOT : parent;
		return this;
	}

	public void register( EventHub peer ) {
		this.peers.add( peer );
	}

	public void unregister( EventHub peer ) {
		this.peers.remove( peer );
	}

	@SuppressWarnings( "unchecked" )
	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		EventHandler<Event> eventHandler = (EventHandler<Event>)handler;
		handlers.computeIfAbsent( type, ( k ) -> new WeakHashMap<>() ).put( eventHandler, eventHandler );
		return this;
	}

	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		handlers.computeIfPresent( type, ( t, m ) -> {
			m.remove( handler );
			return m.isEmpty() ? null : m;
		} );
		return this;
	}

	public Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		return handlers.keySet().stream().collect( Collectors.toMap( k -> k, k -> handlers.get( k ).values() ) );
	}

	@SuppressWarnings( "unchecked" )
	public <T extends Event> T getPriorEvent( Class<? extends Event> type ) {
		return (T)priorEvent.get( type );
	}

	protected EventHub getParent() {
		return parent;
	}

}
