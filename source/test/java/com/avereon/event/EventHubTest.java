package com.avereon.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class EventHubTest {

	@Test
	void testRemovingEventHandlerFromItself() {
		EventHub bus = new EventHub();

		EventHandler<TestEvent> handler = e -> {};
		bus.register( Event.ANY, handler );
		bus.register( Event.ANY, e -> bus.unregister( Event.ANY, handler ) );

		try {
			bus.dispatch( new TestEvent( this, TestEvent.ANY ) );
		} catch( ConcurrentModificationException exception ) {
			fail( "EventBus.dispatch() not implemented in a way that prevents ConcurrentModificationException" );
		}
	}

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
		bus.dispatch( new Event( this, Event.ANY ) );

		assertThat( rootEvents.size(), is( 4 ) );
		assertThat( testEvents1.size(), is( 3 ) );
		assertThat( testEvents2.size(), is( 3 ) );
		assertThat( aEvents.size(), is( 1 ) );
		assertThat( bEvents.size(), is( 1 ) );
	}

	@Test
	void testPrior() {
		EventHub bus = new EventHub();
		assertThat( bus.getPriorEvent( TestEvent.class ), is( nullValue() ) );
		TestEvent any = new TestEvent( this, TestEvent.ANY );
		bus.dispatch( any );
		assertThat( bus.getPriorEvent( TestEvent.class ), is( any ) );

		TestEvent a = new TestEvent( this, TestEvent.A );
		bus.dispatch( a );
		assertThat( bus.getPriorEvent( TestEvent.class ), is( a ) );

		TestEvent b = new TestEvent( this, TestEvent.B );
		bus.dispatch( b );
		assertThat( bus.getPriorEvent( TestEvent.class ), is( b ) );
	}

	@Test
	void testDispatchWithPeer() {
		List<Event> testEvents = new ArrayList<>();
		List<Event> peerEvents = new ArrayList<>();

		EventHub bus = new EventHub();
		EventHub peer = new EventHub();
		bus.register( peer );
		bus.register( TestEvent.ANY, testEvents::add );
		peer.register( TestEvent.ANY, peerEvents::add );

		bus.dispatch( new TestEvent( this, TestEvent.ANY ) );
		assertThat( peerEvents.size(), is( 1 ) );
		assertThat( testEvents.size(), is( 1 ) );
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
