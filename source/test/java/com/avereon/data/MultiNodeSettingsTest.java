package com.avereon.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class MultiNodeSettingsTest {

	private MockNode node1;

	private MockNode node2;

	private MockNode node3;

	private MultiNodeSettings settings;

	@BeforeEach
	public void setup() {
		node1 = new MockNode( "a" );
		node2 = new MockNode( "b" );
		node3 = new MockNode( "c" );

		node1.setValue( "color", "blue" );
		node2.setValue( "color", "blue" );
		node3.setValue( "color", "blue" );

		node1.setValue( "size", "S" );
		node2.setValue( "size", "M" );
		node3.setValue( "size", "L" );

		node2.setValue( "count", 42 );

		settings = new MultiNodeSettings( node1, node2, node3 );
	}

	@Test
	void testCreateWithCollection() {
		MultiNodeSettings settings = new MultiNodeSettings( Set.of( node1, node2, node3 ) );

		// These are MOCK_ID, "color" and "size"
		assertThat( settings.getKeys().size(), is( 3 ) );
	}

	@Test
	void testGetName() {
		assertNull( settings.getName() );
	}

	@Test
	void testGetPath() {
		assertNull( settings.getPath() );
	}

	@Test
	void testGetKeys() {
		assertThat( settings.getKeys(), containsInAnyOrder( MockNode.MOCK_ID, "color", "size" ) );
	}

	@Test
	void testExists() {
		assertTrue( settings.exists( "color" ) );
		assertTrue( settings.exists( "size" ) );
		assertFalse( settings.exists( "count" ) );
	}

	@Test
	void testGet() {
		assertThat( settings.get( "color" ), is( "blue" ) );
		assertThat( settings.get( "size", "default" ), is( "default" ) );
		assertThat( settings.get( "size" ), is( nullValue() ) );
		assertThat( settings.get( "count" ), is( nullValue() ) );
	}

	@Test
	void testSet() {
		assertThat( node1.getValue( "count" ), is( nullValue() ) );
		assertThat( node2.getValue( "count" ), is( 42 ) );
		assertThat( node3.getValue( "count" ), is( nullValue() ) );
		settings.set( "count", 37 );
		assertThat( node1.getValue( "count" ), is( 37 ) );
		assertThat( node2.getValue( "count" ), is( 37 ) );
		assertThat( node3.getValue( "count" ), is( 37 ) );
	}

	@Test
	void testRemove() {
		assertTrue( node1.exists( "size" ) );
		assertTrue( node2.exists( "size" ) );
		assertTrue( node3.exists( "size" ) );
		settings.remove( "size" );
		assertFalse( node1.exists( "size" ) );
		assertFalse( node2.exists( "size" ) );
		assertFalse( node3.exists( "size" ) );
	}

}
