package com.avereon.event;

import com.avereon.util.JavaUtil;

import java.util.EventObject;

public class Event extends EventObject {

	public static final EventType<Event> ANY = EventType.ROOT;

	private final EventType<? extends Event> type;

	private final String toString;

	public Event( final Object source, final EventType<? extends Event> type ) {
		super( source );
		this.type = type;

		String sourceClass = JavaUtil.getClassName( source.getClass() );
		String eventClass = JavaUtil.getClassName( this.getClass() );
		toString = sourceClass + " > " + eventClass + " : " + getEventType().getName();
	}

	public EventType<? extends Event> getEventType() {
		return type;
	}

	public String toString() {
		return toString;
	}

}
