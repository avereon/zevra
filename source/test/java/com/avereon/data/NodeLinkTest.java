package com.avereon.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class NodeLinkTest {

	private MockNode data;

	@BeforeEach
	void setup() {
		data = new MockNode( "a" );
	}

	@Test
	@SuppressWarnings( "unchecked" )
	void testAddRemove() {
		MockNode a = new MockNode( "a" );
		MockNode peer = new MockNode( "peer" );

		data.setValue( "a", a );
		data.setModified( false );
		assertFalse( data.isModified() );

		NodeLink<MockNode> l = new NodeLink<>( a );
		peer.setValue( "l", l );
		assertFalse( peer.isModified() );
		assertFalse( data.isModified() );
		// Node 'l' should belong to peer
		assertThat( peer.getValue( "l" ), is( l ) );
		// Node 'a' should still belong to 'data'
		assertThat( data.getValue( "a" ), is( a ) );
		assertThat( ((NodeLink<MockNode>)peer.getValue( "l" )).getNode(), is( a ) );

		peer.setValue( "l", null );
		assertFalse( peer.isModified() );
		assertFalse( data.isModified() );
		assertNull( peer.getValue( "l" ) );
		assertThat( data.getValue( "a" ), is( a ) );
	}

	@Test
	void testModified() {
		MockNode a = new MockNode( "a" );
		MockNode peer = new MockNode( "peer" );

		data.setValue( "a", a );
		data.setModified( false );
		assertFalse( data.isModified() );

		NodeLink<MockNode> l = new NodeLink<>( a );
		peer.setValue( "l", l );
		assertFalse( peer.isModified() );

		a.setValue( "x", 2.4 );
		assertTrue( a.isModified() );
		assertTrue( data.isModified() );
		assertFalse( l.isModified() );
		assertFalse( peer.isModified() );
	}

}
