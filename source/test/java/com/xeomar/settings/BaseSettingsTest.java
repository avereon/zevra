package com.xeomar.settings;

import com.xeomar.util.PathUtil;
import com.xeomar.util.TypeReference;
import org.junit.Test;

import java.net.URI;
import java.util.*;

import static com.xeomar.settings.SettingsMatchers.eventHas;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public abstract class BaseSettingsTest {

	protected static final String SETTINGS_NAME = "XeomarSettingsTest";

	protected Settings settings;

	@Test
	public void testExists() {
		assertThat( settings.exists( "/" ), is( true ) );
	}

	@Test
	public void testGetPath() {
		assertThat( settings.getPath(), startsWith( "/" ) );
	}

	@Test
	public void testRootNode() {
		Settings rootByEmpty = settings.getNode( PathUtil.EMPTY );
		Settings rootByRoot = settings.getNode( PathUtil.ROOT );
		assertEquals( rootByEmpty, rootByRoot );
		assertThat( rootByEmpty.getPath(), is( PathUtil.ROOT ) );
		assertThat( rootByRoot.getPath(), is( PathUtil.ROOT ) );
		assertThat( rootByEmpty.getName(), is( PathUtil.EMPTY ) );
		assertThat( rootByRoot.getName(), is( PathUtil.EMPTY ) );
	}

	@Test
	public void testGetNode() {
		Settings peer = settings.getNode( "peer" );
		assertThat( peer, instanceOf( settings.getClass() ) );
		assertThat( peer.getPath(), is( "/peer" ) );

		// Is the settings object viable
		peer.set( "a", "A" );
		peer.flush();
		assertThat( peer.get( "a" ), is( "A" ) );
	}

	@Test
	public void testGetNodeWithParentAndName() {
		assertThat( settings.getNode( "", "test" ).getPath(), is( "/test" ) );
		assertThat( settings.getNode( "/", "test" ).getPath(), is( "/test" ) );
		assertThat( settings.getNode( "/test", "path" ).getPath(), is( "/test/path" ) );
	}

	@Test
	public void testGetGrandNodes() {
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
	public void testGetNodeReturnsSameObject() {
		assertThat( settings.getNode( "" ), is( settings ) );
		assertThat( settings.getNode( "/" ), is( settings ) );
	}

	@Test
	public void testGetNodes() {
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
	public void testSetStringAndGetString() {
		String key = "key";
		String value = "value";
		assertThat( settings.get( key ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.get( key, String.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, String.class ), is( nullValue() ) );
	}

	@Test
	public void testSetIntegerAndGetInteger() {
		String key = "key";
		Integer value = 5;
		assertThat( settings.get( key, Integer.class ), is( nullValue() ) );

		settings.set( key, value );
		assertThat( settings.get( key, Integer.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Integer.class ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetBoolean() {
		String key = "key";
		Boolean value = true;
		assertThat( settings.get( key, Boolean.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Boolean.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Boolean.class ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetInteger() {
		String key = "key";
		Integer value = 5;
		assertThat( settings.get( key, Integer.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Integer.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Integer.class ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetLong() {
		String key = "key";
		Long value = 5L;
		assertThat( settings.get( key, Long.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Long.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Long.class ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetFloat() {
		String key = "key";
		Float value = 5.0F;
		assertThat( settings.get( key, Float.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Float.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Float.class ), is( nullValue() ) );
	}

	@Test
	public void testSetStringAndGetDouble() {
		String key = "key";
		Double value = 5.0;
		assertThat( settings.get( key, Double.class ), is( nullValue() ) );

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Double.class ), is( value ) );

		settings.set( key, null );
		assertThat( settings.get( key, Double.class ), is( nullValue() ) );
	}

	@Test
	public void testSetUriAndGetStringAndUri() throws Exception {
		URI uri = new URI( "test:uri" );
		settings.set( "uri", uri );

		assertThat( settings.get( "uri" ), is( uri.toString() ) );
		assertThat( settings.get( "uri", URI.class ), is( uri ) );
	}

	@Test
	public void testSetBeanAndGetBean() {
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
	public void testSetMapAndGetMap() {
		Map<String, MockBean> beans = new HashMap<>();
		TypeReference<Map<String, MockBean>> reference = new TypeReference<Map<String, MockBean>>() {};
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
	public void testSetListAndGetList() {
		List<MockBean> beans = new ArrayList<>();
		TypeReference<List<MockBean>> reference = new TypeReference<List<MockBean>>() {};
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
	public void testSetSetAndGetSet() {
		Set<MockBean> beans = new HashSet<>();
		TypeReference<Set<MockBean>> reference = new TypeReference<Set<MockBean>>() {};

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
	public void testGetUsingDefaultValue() {
		assertThat( settings.get( "missing", "default" ), is( "default" ) );
	}

	@Test
	public void testGetBooleanUsingDefaultValue() {
		assertThat( settings.get( "missing", Boolean.class, false ), is( false ) );
	}

	@Test
	public void testGetValueFromDefaultSettings() {
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
	public void testGetBooleanFromStringDefault() {
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
	public void testUpdatedEvent() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.addSettingsListener( watcher );

		assertThat( watcher.getEvents().size(), is( 0 ) );

		settings.set( "a", "A" );
		assertThat( watcher.getEvents().get( 0 ), eventHas( settings, SettingsEvent.Type.CHANGED, settings.getPath(), "a", null, "A" ) );

		settings.set( "a", null );
		assertThat( watcher.getEvents().get( 1 ), eventHas( settings, SettingsEvent.Type.CHANGED, settings.getPath(), "a", null, null ) );
	}

	@Test
	public void testDelete() {
		assertThat( settings.exists( "test" ), is( false ) );
		Settings test = settings.getNode( "test" );
		test.flush();
		assertThat( settings.exists( "test" ), is( true ) );
		test.delete();
		assertThat( settings.exists( "test" ), is( false ) );
	}

}
