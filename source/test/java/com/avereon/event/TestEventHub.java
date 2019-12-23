package com.avereon.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TestEventHub {

	@Test
	void testHandle() {
		List<Event> rootEvents = new ArrayList<>();
		List<Event> aEvents = new ArrayList<>();
		List<Event> bEvents = new ArrayList<>();

		EventHub<Event> hub = new EventHub<>();
		hub.register( EventType.ROOT, rootEvents::add );
		hub.register( TestEventType.A, aEvents::add );
		hub.register( TestEventType.B, bEvents::add );

		hub.handle( new Event( this, EventType.ROOT ) );
		hub.handle( new Event( this, TestEventType.A ) );
		hub.handle( new Event( this, TestEventType.B ) );

		assertThat( rootEvents.size(), is( 3 ) );
		assertThat( aEvents.size(), is( 1 ) );
		assertThat( bEvents.size(), is( 1 ) );
	}

	private static class TestEventType extends EventType<Event> {

		public static final EventType<Event> ANY = new EventType<>( Event.ANY, "TEST" );

		public static final EventType<Event> A = new EventType<>( ANY, "A" );

		public static final EventType<Event> B = new EventType<>( ANY, "B" );

		private TestEventType( EventType<Event> parent, String name ) {
			super( parent, name );
		}

	}
}
