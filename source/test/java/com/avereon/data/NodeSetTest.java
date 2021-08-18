package com.avereon.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;

public class NodeSetTest {

	private NodeSet<MockNode> set;

	@BeforeEach
	void setup() {
		set = new NodeSet<>( "test" );
	}

	@Test
	void testCircularReferenceCheck() {
		MockNode node = new MockNode();
		try {
			node.addItem( node );
			fail( "CircularReferenceException should be thrown" );
		} catch( CircularReferenceException exception ) {
			// Intentionally ignore exception.
			assertThat( exception.getMessage(), startsWith( "Circular reference detected" ) );
		}
	}

	@Test
	void testAdd() {
		assertThat( set.size(), is( 0 ) );
		assertFalse( set.isModified() );

		set.add( new MockNode() );
		assertThat( set.size(), is( 1 ) );
		assertTrue( set.isModified() );
	}

	@Test
	void testContains() {
		MockNode node = new MockNode();
		assertFalse( set.contains( node ) );
		set.add( node );
		assertTrue( set.contains( node ) );
		set.remove( node );
		assertFalse( set.contains( node ) );
	}

	@Test
	void testSize() {
		MockNode node = new MockNode();
		assertThat( set.size(), is( 0 ) );
		set.add( node );
		assertThat( set.size(), is( 1 ) );
		set.remove( node );
		assertThat( set.size(), is( 0 ) );
	}

	@Test
	void testAddAll() {
		Set<MockNode> nodes = Set.of( new MockNode( "a" ), new MockNode( "b" ) );
		assertThat( set.size(), is( 0 ) );
		set.addAll( nodes );
		assertThat( set.size(), is( 2 ) );
		set.removeAll( nodes );
		assertThat( set.size(), is( 0 ) );
	}

	@Test
	void testClearSet() {
		MockNode node = new MockNode();
		node.addItems( Set.of( new MockNode( "a" ), new MockNode( "b" ) ) );
		assertThat( node.getItems().size(), is( 2 ) );
		node.clearItems();
		assertThat( node.getItems().size(), is( 0 ) );
	}

}
