package com.avereon.event;

import java.util.Collection;
import java.util.EventObject;

public class Event extends EventObject {

	public static final EventType<Event> ANY = EventType.ROOT;

	private EventType<? extends Event> type;

	public Event( final Object source, final EventType<? extends Event> type ) {
		super( source );
		this.type = type;
	}

	public String toString() {
		String sourceClass = getSource().getClass().getSimpleName();
		String eventClass = getClass().getSimpleName();
		return sourceClass + " > " + eventClass + " : " + getEventType();
	}

	public EventType<? extends Event> getEventType() {
		return type;
	}

	@SuppressWarnings( "unchecked" )
	public <E extends Event> E fire( final Collection<? extends EventHandler<E>> handlers ) {
		handlers.forEach( (h) -> h.handle( (E)this ) );
		return (E)this;
	}

}
