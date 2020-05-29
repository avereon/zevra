package com.avereon.settings;

import com.avereon.util.PathUtil;
import com.avereon.util.TypeReference;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static com.avereon.settings.SettingsMatchers.eventHas;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class BaseSettingsTest {

	static final String SETTINGS_NAME = "AvereonSettingsTest";

	protected Settings settings;

	@Test
	void testNodeExists() {
		assertTrue( settings.nodeExists( "/" ) );

		Settings deep = settings.getNode( "deep" );
		deep.set( "a", "A" );
		assertThat( deep.get( "a" ), is( "A" ) );
		assertTrue( settings.nodeExists( "deep" ) );
		assertTrue( settings.nodeExists( "/deep" ) );

		Settings deeper = deep.getNode( "deeper" );
		deeper.set( "b", "B" );
		assertThat( deeper.get( "b" ), is( "B" ) );
		assertTrue( deep.nodeExists( "deeper" ) );
		assertFalse( deep.nodeExists( "/deeper" ) );
		assertTrue( settings.nodeExists( "/deep/deeper" ) );
	}

	@Test
	void testGetPath() {
		assertThat( settings.getPath(), startsWith( "/" ) );
	}

	@Test
	void testRootNode() {
		Settings rootByEmpty = settings.getNode( PathUtil.EMPTY );
		Settings rootByRoot = settings.getNode( PathUtil.ROOT );
		assertEquals( rootByEmpty, rootByRoot );
		assertThat( rootByEmpty.getPath(), is( PathUtil.ROOT ) );
		assertThat( rootByRoot.getPath(), is( PathUtil.ROOT ) );
		assertThat( rootByEmpty.getName(), is( PathUtil.EMPTY ) );
		assertThat( rootByRoot.getName(), is( PathUtil.EMPTY ) );
	}

	@Test
	void testGetNode() {
		Settings peer = settings.getNode( "peer" );
		assertThat( peer, instanceOf( settings.getClass() ) );
		assertThat( peer.getPath(), is( "/peer" ) );

		// Is the settings object viable
		peer.set( "a", "A" );
		peer.flush();
		assertThat( peer.get( "a" ), is( "A" ) );
	}

	@Test
	void testGetNodeWithParentAndName() {
		assertThat( settings.getNode( "", "test" ).getPath(), is( "/test" ) );
		assertThat( settings.getNode( "/", "test" ).getPath(), is( "/test" ) );
		assertThat( settings.getNode( "/test", "path" ).getPath(), is( "/test/path" ) );
	}

	@Test
	void testGetGrandNodes() {
		assertThat( settings.getPath(), startsWith( "" ) );

		Settings childSettings = settings.getNode( "child" );
		Settings grandchildSettings = childSettings.getNode( "grand" );
		assertThat( grandchildSettings, instanceOf( settings.getClass() ) );
		assertThat( grandchildSettings.getPath(), is( "/child/grand" ) );

		// Is the settings object viable
		grandchildSettings.set( "a", "A" );
		grandchildSettings.flush();
		assertThat( grandchildSettings.get( "a" ), is( "A" ) );
	}

	@Test
	void testGetNodeReturnsSameObject() {
		assertThat( settings.getNode( "" ), is( settings ) );
		assertThat( settings.getNode( "/" ), is( settings ) );
	}

	@Test
	void testGetNodes() {
		String folder = "children/" + String.format( "%08x", new Random().nextInt() );
		Settings childA = settings.getNode( folder + "/a" );
		Settings childB = settings.getNode( folder + "/b" );
		Settings childC = settings.getNode( folder + "/c" );

		childA.set( "a", "A" );
		childB.set( "b", "B" );
		childC.set( "c", "C" );

		childA.flush();
		childB.flush();
		childC.flush();

		assertThat( settings.getNodes(), contains( "children" ) );

		Settings children = settings.getNode( folder );
		assertThat( children.getNodes().size(), is( 3 ) );
	}

	@Test
	void testExists() {
		String key = "key";
		String value = "value";
		assertFalse( settings.exists( key ) );

		settings.set( key, value );
		assertTrue( settings.exists( key ) );

		settings.set( key, null );
		assertFalse( settings.exists( key ) );
	}

	@Test
	void testSetStringAndGetString() {
		String key = "key";
		String value = "value";
		assertThat( settings.get( key ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.get( key, String.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, String.class ), is( nullValue() ) );
	}

	@Test
	void testSetIntegerAndGetInteger() {
		String key = "key";
		Integer value = 5;
		assertThat( settings.get( key, Integer.class ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.get( key, Integer.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Integer.class ), is( nullValue() ) );
	}

	@Test
	void testSetStringAndGetBoolean() {
		String key = "key";
		assertThat( settings.get( key, Boolean.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( true ) );
		assertThat( settings.get( key, Boolean.class ), is( true ) );

		settings.set( key, String.valueOf( false ) );
		assertThat( settings.get( key, Boolean.class ), is( false ) );

		settings.set( key, null );
		assertThat( settings.get( key, Boolean.class ), is( nullValue() ) );
	}

	@Test
	void testSetStringAndGetInteger() {
		String key = "key";
		Integer value = 5;
		assertThat( settings.get( key, Integer.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Integer.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Integer.class ), is( nullValue() ) );
	}

	@Test
	void testSetStringAndGetLong() {
		String key = "key";
		Long value = 5L;
		assertThat( settings.get( key, Long.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Long.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Long.class ), is( nullValue() ) );
	}

	@Test
	void testSetStringAndGetFloat() {
		String key = "key";
		Float value = 5.0F;
		assertThat( settings.get( key, Float.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Float.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Float.class ), is( nullValue() ) );
	}

	@Test
	void testSetStringAndGetDouble() {
		String key = "key";
		Double value = 5.0;
		assertThat( settings.get( key, Double.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Double.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Double.class ), is( nullValue() ) );
	}

	@Test
	void testSetUriAndGetStringAndUri() throws Exception {
		URI uri = new URI( "test:uri" );
		settings.set( "uri", uri );

		assertThat( settings.get( "uri" ), is( uri.toString() ) );
		assertThat( settings.get( "uri", URI.class ), is( uri ) );
	}

	@Test
	void testSetBeanAndGetBean() {
		MockBean bean = new MockBean();
		bean.setIntegerPrimitiveProperty( 284 );
		bean.setIntegerProperty( 1 );
		bean.setStringProperty( "one" );

		assertThat( settings.get( "bean", MockBean.class ), is( nullValue() ) );

		settings.set( "bean", bean );
		assertThat( settings.get( "bean", MockBean.class ), is( not( nullValue() ) ) );

		MockBean result = settings.get( "bean", MockBean.class );
		assertThat( result, is( bean ) );
	}

	@Test
	void testSetMapAndGetMap() {
		Map<String, MockBean> beans = new HashMap<>();
		TypeReference<Map<String, MockBean>> reference = new TypeReference<>() {};
		assertThat( settings.get( "beans", reference ), is( nullValue() ) );

		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		beans.put( "a", bean1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );
		beans.put( "b", bean2 );

		// Store the map
		settings.set( "beans", beans );
		assertThat( settings.get( "beans", reference ), is( not( nullValue() ) ) );

		// Get the map and check the beans
		Map<String, MockBean> map = settings.get( "beans", reference );
		assertThat( map, hasEntry( is( "a" ), is( bean1 ) ) );
		assertThat( map, hasEntry( is( "b" ), is( bean2 ) ) );
		assertThat( map.size(), is( beans.size() ) );
	}

	@Test
	void testSetListAndGetList() {
		List<MockBean> beans = new ArrayList<>();
		TypeReference<List<MockBean>> reference = new TypeReference<>() {};
		assertThat( settings.get( "beans", reference ), is( nullValue() ) );

		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		beans.add( bean1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );
		beans.add( bean2 );

		// Store the map
		settings.set( "beans", beans );
		assertThat( settings.get( "beans", reference ), is( not( nullValue() ) ) );

		// Get the map and check the beans
		List<MockBean> list = settings.get( "beans", reference );
		assertThat( list, contains( bean1, bean2 ) );
	}

	@Test
	void testSetSetAndGetSet() {
		Set<MockBean> beans = new HashSet<>();
		TypeReference<Set<MockBean>> reference = new TypeReference<>() {};

		assertThat( settings.get( "beans", reference ), is( nullValue() ) );

		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		beans.add( bean1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );
		beans.add( bean2 );

		// Store the map
		settings.set( "beans", beans );
		assertThat( settings.get( "beans", reference ), is( not( nullValue() ) ) );

		// Get the map and check the beans
		Set<MockBean> set = settings.get( "beans", reference );
		assertThat( set, containsInAnyOrder( bean1, bean2 ) );
	}

	@Test
	void testGetUsingDefaultValue() {
		assertThat( settings.get( "missing", "default" ), is( "default" ) );
	}

	@Test
	void testGetBooleanUsingDefaultValue() {
		assertThat( settings.get( "missing", Boolean.class, false ), is( false ) );
	}

	@Test
	void testGetValueFromDefaultSettings() {
		String key = "key";
		String value = "value";
		String defaultValue = "defaultValue";

		Map<String, Object> defaultValues = new HashMap<>();
		defaultValues.put( key, defaultValue );

		// Start by checking the value is null
		assertThat( settings.get( key, String.class ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.get( key, String.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, String.class ), is( nullValue() ) );

		// Test the default settings
		settings.setDefaultValues( defaultValues );
		assertThat( settings.get( key, String.class ), is( defaultValue ) );

		settings.set( key, value );
		assertThat( settings.get( key, String.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, String.class ), is( defaultValue ) );

		settings.setDefaultValues( null );
		assertThat( settings.get( key, String.class ), is( nullValue() ) );
	}

	@Test
	void testGetBooleanFromStringDefault() {
		String key = "key";
		String value = "false";
		String defaultValue = "true";

		Map<String, Object> defaultValues = new HashMap<>();
		defaultValues.put( key, defaultValue );
		settings.setDefaultValues( defaultValues );
		assertThat( settings.get( "key", Boolean.class ), is( true ) );

		settings.set( "key", value );
		assertThat( settings.get( "key", Boolean.class ), is( false ) );

		settings.set( "key", null );
		assertThat( settings.get( "key", Boolean.class ), is( true ) );
	}

	@Test
	void testUpdatedEvent() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.register( SettingsEvent.ANY, watcher );
		assertThat( watcher.getEvents().size(), is( 0 ) );

		// The value does not change so there should not be an extra event
		settings.set( "a", null );
		assertThat( watcher.getEvents().size(), is( 0 ) );

		settings.set( "a", "A" );
		assertThat( watcher.getEvents().get( 0 ), eventHas( settings, SettingsEvent.CHANGED, settings.getPath(), "a", null, "A" ) );
		assertThat( watcher.getEvents().size(), is( 1 ) );

		// The value does not change so there should not be an extra event
		settings.set( "a", "A" );
		assertThat( watcher.getEvents().size(), is( 1 ) );

		settings.set( "a", null );
		assertThat( watcher.getEvents().get( 1 ), eventHas( settings, SettingsEvent.CHANGED, settings.getPath(), "a", null, null ) );
		assertThat( watcher.getEvents().size(), is( 2 ) );

		// The value does not change so there should not be an extra event
		settings.set( "a", null );
		assertThat( watcher.getEvents().size(), is( 2 ) );
	}

	@Test
	void testShallowCopyFrom() {
		Settings source = new MapSettings();
		source.set( "a", "A" );
		source.set( "b", 2 );
		settings.copyFrom( source );
		assertThat( settings.get( "a" ), is( "A" ) );
		assertThat( settings.get( "b", Integer.class ), is( 2 ) );
	}

	@Test
	void testDeepCopyFrom() {
		Settings source = new MapSettings();
		source.set( "a", "A" );
		source.set( "b", 2 );
		Settings deep = source.getNode( "deep" );
		deep.set( "c", "see" );
		deep.set( "d", "D" );

		settings.copyFrom( source );
		assertThat( settings.get( "a" ), is( "A" ) );
		assertThat( settings.get( "b", Integer.class ), is( 2 ) );
		assertTrue( settings.nodeExists( "deep" ) );
		assertThat( settings.getNode( "deep" ).get( "c" ), is( "see" ) );
		assertThat( settings.getNode( "deep" ).get( "d" ), is( "D" ) );
	}

	@Test
	void testDelete() {
		assertThat( settings.nodeExists( "test" ), is( false ) );
		Settings test = settings.getNode( "test" );
		test.flush();
		assertThat( settings.nodeExists( "test" ), is( true ) );
		test.delete();
		assertThat( settings.nodeExists( "test" ), is( false ) );
	}

}
