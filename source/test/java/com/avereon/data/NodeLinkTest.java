package com.avereon.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeLinkTest {

	private MockNode data;

	@BeforeEach
	void setup() {
		data = new MockNode( "a" );
	}

	@Test
	void testAddRemove() {
		MockNode a = new MockNode( "a" );
		MockNode peer = new MockNode( "peer" );

		data.setValue( "a", a );
		data.setModified( false );
		assertThat( data.isModified() ).isFalse();

		NodeLink<MockNode> l = new NodeLink<>( a );
		peer.setValue( "l", l );
		assertThat( peer.isModified() ).isFalse();
		assertThat( data.isModified() ).isFalse();
		// Node 'l' should belong to peer
		assertThat( peer.<Object> getValue( "l" ) ).isEqualTo( l );
		// Node 'a' should still belong to 'data'
		assertThat( data.<Object> getValue( "a" ) ).isEqualTo( a );
		assertThat( peer.<NodeLink<MockNode>> getValue( "l" ).getNode() ).isEqualTo( a );

		peer.setValue( "l", null );
		assertThat( peer.isModified() ).isFalse();
		assertThat( data.isModified() ).isFalse();
		assertThat( peer.<Object> getValue( "l" ) ).isNull();
		assertThat( data.<MockNode> getValue( "a" ) ).isEqualTo( a );
	}

	@Test
	void testModified() {
		MockNode a = new MockNode( "a" );
		MockNode peer = new MockNode( "peer" );

		data.setValue( "a", a );
		data.setModified( false );
		assertThat( data.isModified() ).isFalse();

		NodeLink<MockNode> l = new NodeLink<>( a );
		peer.setValue( "l", l );
		assertThat( peer.isModified() ).isFalse();

		a.setValue( "x", 2.4 );
		assertThat( a.isModified() ).isTrue();
		assertThat( data.isModified() ).isTrue();
		assertThat( l.isModified() ).isFalse();
		assertThat( peer.isModified() ).isFalse();
	}

}
