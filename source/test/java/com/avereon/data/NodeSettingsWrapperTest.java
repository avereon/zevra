package com.avereon.data;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NodeSettingsWrapperTest {

	@Test
	public void testSetAndGet() {
		MockNode node = new MockNode();
		NodeSettingsWrapper settings = new NodeSettingsWrapper( node );
		assertNull( settings.get( "value" ) );

		settings.set( "value", Double.MAX_VALUE );
		assertThat( settings.get( "value", Double.class ), is( Double.MAX_VALUE ) );
		assertThat( settings.get( "value" ), is( String.valueOf( Double.MAX_VALUE ) ) );
	}

	@Test
	public void testRemove() {
		MockNode node = new MockNode();
		NodeSettingsWrapper settings = new NodeSettingsWrapper( node );
		settings.set( "value", Double.MAX_VALUE );
		assertNotNull( settings.get( "value" ) );

		settings.remove( "value" );
		assertNull( settings.get( "value" ) );
	}

}
