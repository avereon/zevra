package com.avereon.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NodeEventTest {

	@Test
	void testEquals() {
		Node node = new MockNode();
		NodeEvent event1 = new NodeEvent( node, NodeEvent.NODE_CHANGED );
		NodeEvent event2 = new NodeEvent( node, NodeEvent.NODE_CHANGED );
		assertThat( event1.equals( event2 ) ).isEqualTo( true );
		assertThat( event2.equals( event1 ) ).isEqualTo( true );
	}

	@Test
	void testEqualsWithValueKey() {
		Node node = new MockNode();
		NodeEvent event1 = new NodeEvent( node, NodeEvent.VALUE_CHANGED, "a", null, "1" );
		NodeEvent event2 = new NodeEvent( node, NodeEvent.VALUE_CHANGED, "a", "1", "5" );
		assertThat( event1.equals( event2 ) ).isEqualTo( true );
		assertThat( event2.equals( event1 ) ).isEqualTo( true );
	}

}
