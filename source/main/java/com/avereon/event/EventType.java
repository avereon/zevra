package com.avereon.event;

public class EventType {

	public static final EventType ROOT = new EventType( null, "EVENT" );

	private EventType parent;

	private String name;

	public EventType( final String name ) {
		this( ROOT, name );
	}

	public EventType( final EventType parent ) {
		this( parent, null );
	}

	public EventType( final EventType parent, final String name ) {
		this.parent = parent;
		this.name = name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "-" + name;
	}

}
