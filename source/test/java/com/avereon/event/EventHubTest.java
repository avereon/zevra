package com.avereon.event;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

class EventHubTest {

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

		assertThat( rootEvents.size() ).isEqualTo( 4 );
		assertThat( testEvents1.size() ).isEqualTo( 3 );
		assertThat( testEvents2.size() ).isEqualTo( 3 );
		assertThat( aEvents.size() ).isEqualTo( 1 );
		assertThat( bEvents.size() ).isEqualTo( 1 );
	}

	@Test
	void testPrior() {
		EventHub bus = new EventHub();
		assertThat( bus.<Event> getPriorEvent( TestEvent.class ) ).isNull();
		TestEvent any = new TestEvent( this, TestEvent.ANY );
		bus.dispatch( any );
		assertThat( bus.<Event> getPriorEvent( TestEvent.class ) ).isEqualTo( any );

		TestEvent a = new TestEvent( this, TestEvent.A );
		bus.dispatch( a );
		assertThat( bus.<Event> getPriorEvent( TestEvent.class ) ).isEqualTo( a );

		TestEvent b = new TestEvent( this, TestEvent.B );
		bus.dispatch( b );
		assertThat( bus.<Event> getPriorEvent( TestEvent.class ) ).isEqualTo( b );
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
		assertThat( peerEvents.size() ).isEqualTo( 1 );
		assertThat( testEvents.size() ).isEqualTo( 1 );
	}

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
	void testWeaklyMappedEventHandler() {
		// given
		EventHub bus = new EventHub();
		List<Event> events = new ArrayList<>();

		Object owner = new Object();
		EventHandler<TestEvent> handler = events::add;
		bus.register( owner, Event.ANY, handler );
		assertThat( bus.getEventHandlers( Event.ANY ) ).isNotEmpty();
		assertThat( events ).isEmpty();

		// when
		// Set owner to null to allow for garbage collected
		owner = null;
		System.gc();

		// then
		assertThat( bus.getEventHandlers( Event.ANY ) ).isEmpty();
		assertThat( events ).isEmpty();
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
