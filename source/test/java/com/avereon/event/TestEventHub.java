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
		List<Event> testEvents1 = new ArrayList<>();
		List<Event> testEvents2 = new ArrayList<>();
		List<Event> aEvents = new ArrayList<>();
		List<Event> bEvents = new ArrayList<>();

		EventHub bus = new EventHub();
		bus.register( Event.ANY, rootEvents::add );
		bus.register( TestEvent.ANY, testEvents1::add );
		bus.register( TestEvent.ANY, testEvents2::add );
		bus.register( TestEvent.A, aEvents::add );
		bus.register( TestEvent.B, bEvents::add );

		bus.dispatch( new TestEvent( this, TestEvent.ANY ) );
		bus.dispatch( new TestEvent( this, TestEvent.A ) );
		bus.dispatch( new TestEvent( this, TestEvent.B ) );

		assertThat( rootEvents.size(), is( 3 ) );
		assertThat( testEvents1.size(), is( 3 ) );
		assertThat( testEvents2.size(), is( 3 ) );
		assertThat( aEvents.size(), is( 1 ) );
		assertThat( bEvents.size(), is( 1 ) );
	}

	private static class TestEvent extends Event {

		public static final EventType<TestEvent> TEST = new EventType<>( EventType.ROOT, "TEST" );

		public static final EventType<TestEvent> ANY = TEST;

		public static final EventType<TestEvent> A = new EventType<>( TestEvent.ANY, "A" );

		public static final EventType<TestEvent> B = new EventType<>( TestEvent.ANY, "B" );

		public TestEvent( Object source, EventType<TestEvent> type ) {
			super( source, type );
		}

	}

}
