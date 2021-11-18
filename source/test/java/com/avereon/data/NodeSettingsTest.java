package com.avereon.data;

import com.avereon.settings.SettingsEvent;
import com.avereon.settings.SettingsEventAssert;
import com.avereon.settings.SettingsEventWatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
		assertThat( watcher.getEvents().size() ).isEqualTo( 0 );

		node.setValue( "value", Double.MAX_VALUE );

		int index = 0;
		SettingsEventAssert.assertThat( watcher.getEvents().get( index++ ) ).hasValues( settings, SettingsEvent.CHANGED, settings.getPath(), "value", null, Double.MAX_VALUE );
		assertThat( watcher.getEvents().size() ).isEqualTo( index );
	}

	@Test
	public void testFlush() {
		assertThat( settings.flush() ).isEqualTo( settings );
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
		assertThat( settings.getName() ).isEqualTo( node.getCollectionId() );
	}

	@Test
	public void testGetPath() {
		assertThat( settings.getPath() ).isEqualTo( node.getCollectionId() );
	}

	@Test
	public void testGetKeys() {
		node.setValue( "b", "B" );
		node.setValue( "a", "A" );
		node.setValue( "c", "C" );
		assertThat( settings.getKeys() ).contains( "a", "b", "c" );
	}

	@Test
	public void testSet() {
		assertNull( node.getValue( "value" ) );
		settings.set( "value", Double.MIN_VALUE );
		assertThat( node.<Double> getValue( "value" ) ).isEqualTo( Double.MIN_VALUE );
	}

	@Test
	public void testGet() {
		assertNull( node.getValue( "value" ) );
		node.setValue( "value", Double.MAX_VALUE );
		assertThat( settings.get( "value", Double.class ) ).isEqualTo( Double.MAX_VALUE );
	}

	@Test
	public void testRemove() {
		settings.set( "value", Double.MAX_VALUE );
		assertThat( node.<Double> getValue( "value" ) ).isEqualTo( Double.MAX_VALUE );

		settings.remove( "value" );
		assertNull( node.getValue( "value" ) );
	}

}
