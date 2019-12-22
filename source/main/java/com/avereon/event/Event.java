package com.avereon.event;

import java.util.Collection;
import java.util.EventObject;

public class Event extends EventObject {

	private EventType type;

	public Event( Object source, EventType type ) {
		super( source );
		this.type = type;
	}

	public String toString() {
		String sourceClass = getSource().getClass().getSimpleName();
		String eventClass = getClass().getSimpleName();
		return sourceClass + "> " + eventClass;
	}

	public EventType getEventType() {
		return type;
	}

	@SuppressWarnings( "unchecked" )
	public <E extends Event> E fire( Collection<? extends EventHandler<E>> handlers ) {
		handlers.forEach( (h) -> h.handle( (E)this ) );
		return (E)this;
	}

}
