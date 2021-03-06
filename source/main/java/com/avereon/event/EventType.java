package com.avereon.event;

public final class EventType<T extends Event> {

	public static final EventType<Event> ROOT = new EventType<>( null, "EVENT" );

	private final EventType<? super T> parent;

	private final String name;

	public EventType( final String name ) {
		this( ROOT, name );
	}

	public EventType( final EventType<? super T> parent ) {
		this( parent, parent.getName() );
	}

	public EventType( final EventType<? super T> parent, final String name ) {
		this.parent = parent;
		this.name = name;
	}

	public EventType<? super T> getParentEventType() {
		return parent;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
