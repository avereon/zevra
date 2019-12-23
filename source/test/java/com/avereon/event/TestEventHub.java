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
		List<Event> testEvents = new ArrayList<>();
		List<Event> aEvents = new ArrayList<>();
		List<Event> bEvents = new ArrayList<>();

		EventHub<Event> hub = new EventHub<>();
		hub.register( Event.ANY, rootEvents::add );
		hub.register( TestEvent.ANY, testEvents::add );
		hub.register( TestEvent.A, aEvents::add );
		hub.register( TestEvent.B, bEvents::add );

		hub.handle( new TestEvent( this, TestEvent.ANY ) );
		hub.handle( new TestEvent( this, TestEvent.A ) );
		hub.handle( new TestEvent( this, TestEvent.B ) );

		assertThat( rootEvents.size(), is( 3 ) );
		assertThat( testEvents.size(), is( 3 ) );
		assertThat( aEvents.size(), is( 1 ) );
		assertThat( bEvents.size(), is( 1 ) );
	}

	private static class TestEvent extends Event {

		public static final EventType<TestEvent> TEST = new EventType<>( EventType.ROOT, "TEST" );

		public static final EventType<TestEvent> ANY = TEST;

		public static final EventType<TestEvent> A = new EventType<>( TestEvent.ANY, "A" );

		public static final EventType<TestEvent> B = new EventType<>( TestEvent.ANY, "B" );

		public TestEvent( Object source, EventType<? extends TestEvent> type ) {
			super( source, type );
		}

	}

}
