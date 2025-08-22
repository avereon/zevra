package com.avereon.event;

import lombok.CustomLog;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@CustomLog
public class EventHub {

	private static final EventHub ROOT = new EventHub();

	private final Set<EventHub> peers;

	private final Map<Object, Map<EventType<? extends Event>, Set<EventHandler<? extends Event>>>> eventTypeHandlers;

	private final Map<Class<? extends Event>, Event> priorEvent;

	private EventHub parent = ROOT;

	public EventHub() {
		this.peers = new CopyOnWriteArraySet<>();
		this.eventTypeHandlers = new WeakHashMap<>();
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

					Collection<? extends EventHandler<Event>> typeHandlers = new HashSet<>( getEventHandlers( type ) );
					typeHandlers.forEach( v -> {
						try {
							v.handle( event );
						} catch( RuntimeException handlerException ) {
							// Do not let any handler break the others
							if( log.atConfig().isEnabled() ) {
								log.atConfig().withCause( handlerException ).log( "Event handler exception" );
							} else {
								log.atWarn().log( "Event handler exception: {0}", handlerException.getMessage() );
							}
						}
					} );
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

	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return register( this, type, handler );
	}

	public <T extends Event> EventHub register( Object owner, EventType<? super T> type, EventHandler<? super T> handler ) {
		Map<EventType<? extends Event>, Set<EventHandler<? extends Event>>> typeHandlers = eventTypeHandlers.computeIfAbsent( owner, ( k ) -> new HashMap<>() );
		Set<EventHandler<? extends Event>> handlers = typeHandlers.computeIfAbsent( type, ( k ) -> new CopyOnWriteArraySet<>() );
		handlers.add( handler );
		return this;
	}

	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return unregister( this, type, handler );
	}

	public <T extends Event> EventHub unregister( Object owner, EventType<? super T> type, EventHandler<? super T> handler ) {
		Map<EventType<? extends Event>, Set<EventHandler<? extends Event>>> typeHandlers = eventTypeHandlers.get( owner );
		if( typeHandlers == null ) return this;
		Set<EventHandler<? extends Event>> handlers = typeHandlers.get( type );
		if( handlers == null ) return this;
		handlers.remove( handler );
		if( handlers.isEmpty() ) typeHandlers.remove( type );
		if( typeHandlers.isEmpty() ) eventTypeHandlers.remove( owner, typeHandlers );

		return this;
	}

	@SuppressWarnings( "unchecked" )
	public Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> result = new HashMap<>();

		eventTypeHandlers.values().forEach( v -> v.keySet().forEach( k -> {
			Collection<? extends EventHandler<? extends Event>> handlers = result.computeIfAbsent( k, y -> new CopyOnWriteArraySet<>() );
			((Collection<EventHandler<? extends Event>>)handlers).addAll( v.get( k ) );
		} ) );

		return result;
	}

	@SuppressWarnings( "unchecked" )
	public Collection<? extends EventHandler<Event>> getEventHandlers( EventType<? extends Event> type ) {
		Collection<EventHandler<Event>> result = new HashSet<>();
		eventTypeHandlers.values().forEach( m -> {
			Set<EventHandler<? extends Event>> handlers = m.getOrDefault( type, Set.of() );
			handlers.forEach( h -> result.add( (EventHandler<Event>)h ) );
		} );
		return result;
	}

	@SuppressWarnings( "unchecked" )
	public <T extends Event> T getPriorEvent( Class<? extends Event> type ) {
		return (T)priorEvent.get( type );
	}

	protected EventHub getParent() {
		return parent;
	}

}
