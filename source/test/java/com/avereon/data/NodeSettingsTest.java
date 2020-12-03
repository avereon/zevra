package com.avereon.data;

import com.avereon.settings.SettingsEvent;
import com.avereon.settings.SettingsEventWatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.avereon.settings.SettingsMatchers.eventHas;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

public class NodeSettingsTest {

	private MockNode node;

	private NodeSettings settings;

	@BeforeEach
	public void setup() {
		node = new MockNode();
		settings = new NodeSettings( node );
	}

	@Test
	public void testEventHandling() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.register( SettingsEvent.CHANGED, watcher );
		assertThat( watcher.getEvents().size(), is( 0 ) );

		node.setValue( "value", Double.MAX_VALUE );

		int index = 0;
		assertThat( watcher.getEvents().get( index++ ), eventHas( settings, SettingsEvent.CHANGED, settings.getPath(), "value", null, Double.MAX_VALUE ) );
		assertThat( watcher.getEvents().size(), is( index ) );
	}

	@Test
	public void testFlush() {
		assertThat( settings.flush(), is( settings ) );
	}

	@Test
	public void testExists() {
		assertFalse( settings.exists( "value" ) );

		settings.set( "value", Double.MAX_VALUE );
		assertTrue( settings.exists( "value" ) );

		settings.remove( "value" );
		assertFalse( settings.exists( "value" ) );
	}

	@Test
	public void testGetName() {
		assertThat( settings.getName(), is( node.getCollectionId() ) );
	}

	@Test
	public void testGetPath() {
		assertThat( settings.getPath(), is( node.getCollectionId() ) );
	}

	@Test
	public void testGetKeys() {
		node.setValue( "b", "B" );
		node.setValue( "a", "A" );
		node.setValue( "c", "C" );
		assertThat( settings.getKeys(), containsInAnyOrder( "a", "b", "c" ) );
	}

	@Test
	public void testRemove() {
		settings.set( "value", Double.MAX_VALUE );
		assertNotNull( settings.get( "value" ) );

		settings.remove( "value" );
		assertNull( settings.get( "value" ) );
	}

	@Test
	public void testSetAndGet() {
		assertNull( settings.get( "value" ) );

		settings.set( "value", Double.MAX_VALUE );
		assertThat( settings.get( "value", Double.class ), is( Double.MAX_VALUE ) );
		assertThat( settings.get( "value" ), is( String.valueOf( Double.MAX_VALUE ) ) );
	}

}
