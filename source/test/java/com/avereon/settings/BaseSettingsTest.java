package com.avereon.settings;

import com.avereon.util.PathUtil;
import com.avereon.util.TypeReference;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public abstract class BaseSettingsTest {

	protected Settings settings;

	@Test
	void testNodeExists() {
		assertThat( settings.nodeExists( "/" ) ).isTrue();

		Settings deep = settings.getNode( "deep" );
		deep.set( "a", "A" );
		assertThat( deep.get( "a" ) ).isEqualTo( "A" );
		assertThat( settings.nodeExists( "deep" ) ).isTrue();
		assertThat( settings.nodeExists( "/deep" ) ).isTrue();

		Settings deeper = deep.getNode( "deeper" );
		deeper.set( "b", "B" );
		assertThat( deeper.get( "b" ) ).isEqualTo( "B" );
		assertThat( deep.nodeExists( "deeper" ) ).isTrue();
		assertThat( deep.nodeExists( "/deeper" ) ).isFalse();
		assertThat( settings.nodeExists( "/deep/deeper" ) ).isTrue();
	}

	@Test
	void testGetPath() {
		assertThat( settings.getPath() ).startsWith( "/" );
	}

	@Test
	void testRootNode() {
		Settings rootByEmpty = settings.getNode( PathUtil.EMPTY );
		Settings rootByRoot = settings.getNode( PathUtil.ROOT );
		assertThat( rootByRoot ).isEqualTo( rootByEmpty );
		assertThat( rootByEmpty.getPath() ).isEqualTo( PathUtil.ROOT );
		assertThat( rootByRoot.getPath() ).isEqualTo( PathUtil.ROOT );
		assertThat( rootByEmpty.getName() ).isEqualTo( PathUtil.EMPTY );
		assertThat( rootByRoot.getName() ).isEqualTo( PathUtil.EMPTY );
	}

	@Test
	void testGetNode() {
		Settings peer = settings.getNode( "peer" );
		assertThat( peer ).isInstanceOf( settings.getClass() );
		assertThat( peer.getPath() ).isEqualTo( "/peer" );

		// Is the settings object viable
		peer.set( "a", "A" );
		peer.flush();
		assertThat( peer.get( "a" ) ).isEqualTo( "A" );
	}

	@Test
	void testGetNodeWithParentAndName() {
		assertThat( settings.getNode( "", "test" ).getPath() ).isEqualTo( "/test" );
		assertThat( settings.getNode( "/", "test" ).getPath() ).isEqualTo( "/test" );
		assertThat( settings.getNode( "/test", "path" ).getPath() ).isEqualTo( "/test/path" );
	}

	@Test
	void testGetGrandNodes() {
		assertThat( settings.getPath() ).startsWith( "" );

		Settings childSettings = settings.getNode( "child" );
		Settings grandchildSettings = childSettings.getNode( "grand" );
		assertThat( grandchildSettings ).isInstanceOf( settings.getClass() );
		assertThat( grandchildSettings.getPath() ).isEqualTo( "/child/grand" );

		// Is the settings object viable
		grandchildSettings.set( "a", "A" );
		grandchildSettings.flush();
		assertThat( grandchildSettings.get( "a" ) ).isEqualTo( "A" );
	}

	@Test
	void testGetNodeReturnsSameObject() {
		assertThat( settings.getNode( "" ) ).isEqualTo( settings );
		assertThat( settings.getNode( "/" ) ).isEqualTo( settings );
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

		assertThat( settings.getNodes() ).contains( "children" );

		Settings children = settings.getNode( folder );
		assertThat( children.getNodes().size() ).isEqualTo( 3 );
	}

	@Test
	void testExists() {
		String key = "key";
		String value = "value";
		assertThat( settings.exists( key ) ).isFalse();

		settings.set( key, value );
		assertThat( settings.exists( key ) ).isTrue();

		settings.set( key, null );
		assertThat( settings.exists( key ) ).isFalse();
	}

	@Test
	void testSetStringAndGetString() {
		String key = "key";
		String value = "value";
		assertThat( settings.get( key ) ).isNull();

		settings.set( key, value );
		assertThat( settings.get( key, String.class ) ).isEqualTo( value );

		settings.set( key, null );
		assertThat( settings.get( key, String.class ) ).isNull();
	}

	@Test
	void testSetIntegerAndGetInteger() {
		String key = "key";
		Integer value = 5;
		assertThat( settings.get( key, Integer.class ) ).isNull();

		settings.set( key, value );
		assertThat( settings.get( key, Integer.class ) ).isEqualTo( value );

		settings.set( key, null );
		assertThat( settings.get( key, Integer.class ) ).isNull();
	}

	@Test
	void testSetStringAndGetBoolean() {
		String key = "key";
		assertThat( settings.get( key, Boolean.class ) ).isNull();

		settings.set( key, String.valueOf( true ) );
		assertThat( settings.get( key, Boolean.class ) ).isTrue();

		settings.set( key, String.valueOf( false ) );
		assertThat( settings.get( key, Boolean.class ) ).isFalse();

		settings.set( key, null );
		assertThat( settings.get( key, Boolean.class ) ).isNull();
	}

	@Test
	void testSetStringAndGetInteger() {
		String key = "key";
		Integer value = 5;
		assertThat( settings.get( key, Integer.class ) ).isNull();

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Integer.class ) ).isEqualTo( value );

		settings.set( key, null );
		assertThat( settings.get( key, Integer.class ) ).isNull();
	}

	@Test
	void testSetStringAndGetLong() {
		String key = "key";
		Long value = 5L;
		assertThat( settings.get( key, Long.class ) ).isNull();

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Long.class ) ).isEqualTo( value );

		settings.set( key, null );
		assertThat( settings.get( key, Long.class ) ).isNull();
	}

	@Test
	void testSetStringAndGetFloat() {
		String key = "key";
		Float value = 5.0F;
		assertThat( settings.get( key, Float.class ) ).isNull();

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Float.class ) ).isEqualTo( value );

		settings.set( key, null );
		assertThat( settings.get( key, Float.class ) ).isNull();
	}

	@Test
	void testSetStringAndGetDouble() {
		String key = "key";
		Double value = 5.0;
		assertThat( settings.get( key, Double.class ) ).isNull();

		settings.set( key, String.valueOf( value ) );
		assertThat( settings.get( key, Double.class ) ).isEqualTo( value );

		settings.set( key, null );
		assertThat( settings.get( key, Double.class ) ).isNull();
	}

	@Test
	void testSetUriAndGetStringAndUri() throws Exception {
		URI uri = new URI( "test:uri" );
		settings.set( "uri", uri );

		assertThat( settings.get( "uri" ) ).isEqualTo( uri.toString() );
		assertThat( settings.get( "uri", URI.class ) ).isEqualTo( uri );
	}

	@Test
	void testSetBeanAndGetBean() {
		MockBean bean = new MockBean();
		bean.setIntegerPrimitiveProperty( 284 );
		bean.setIntegerProperty( 1 );
		bean.setStringProperty( "one" );

		assertThat( settings.get( "bean", MockBean.class ) ).isNull();

		settings.set( "bean", bean );
		assertThat( settings.get( "bean", MockBean.class ) ).isNotNull();

		MockBean result = settings.get( "bean", MockBean.class );
		assertThat( result ).isEqualTo( bean );
	}

	@Test
	void testSetMapAndGetMap() {
		Map<String, MockBean> beans = new HashMap<>();
		TypeReference<Map<String, MockBean>> reference = new TypeReference<>() {};
		assertThat( settings.get( "beans", reference ) ).isNull();

		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		beans.put( "a", bean1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );
		beans.put( "b", bean2 );

		// Store the map
		settings.set( "beans", beans );
		assertThat( settings.get( "beans", reference ) ).isNotNull();

		// Get the map and check the beans
		Map<String, MockBean> map = settings.get( "beans", reference );
		assertThat( map ).contains( entry( "a", bean1 ) );
		assertThat( map ).contains( entry( "b", bean2 ) );
		assertThat( map.size() ).isEqualTo( beans.size() );
	}

	@Test
	void testSetListAndGetList() {
		List<MockBean> beans = new ArrayList<>();
		TypeReference<List<MockBean>> reference = new TypeReference<>() {};
		assertThat( settings.get( "beans", reference ) ).isNull();

		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		beans.add( bean1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );
		beans.add( bean2 );

		// Store the list
		settings.set( "beans", beans );
		assertThat( settings.get( "beans", reference ) ).isNotNull();

		// Get the list and check the beans
		List<MockBean> list = settings.get( "beans", reference );
		assertThat( list ).contains( bean1, bean2 );
	}

	@Test
	void testSetSetAndGetSet() {
		Set<MockBean> beans = new HashSet<>();
		TypeReference<Set<MockBean>> reference = new TypeReference<>() {};

		assertThat( settings.get( "beans", reference ) ).isNull();

		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		beans.add( bean1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );
		beans.add( bean2 );

		// Store the set
		settings.set( "beans", beans );
		assertThat( settings.get( "beans", reference ) ).isNotNull();

		// Get the set and check the beans
		Set<MockBean> set = settings.get( "beans", reference );
		assertThat( set ).contains( bean1, bean2 );
	}

	@Test
	void testSetEnumAndGetEnum() {
		assertThat( settings.get( "enum", MockEnum.class ) ).isNull();
		assertThat( settings.get( "enum", MockEnum.class, MockEnum.A ) ).isEqualTo( MockEnum.A );
		settings.set( "enum", MockEnum.A );
		assertThat( settings.get( "enum", MockEnum.class ) ).isEqualTo( MockEnum.A );
		settings.set( "enum", MockEnum.B );
		assertThat( settings.get( "enum", MockEnum.class ) ).isEqualTo( MockEnum.B );
		settings.set( "enum", null );
		assertThat( settings.get( "enum", MockEnum.class ) ).isNull();
	}

	@Test
	void testSetStringAndGetEnum() {
		assertThat( settings.get( "enum", MockEnum.class ) ).isNull();
		assertThat( settings.get( "enum", MockEnum.class, MockEnum.A ) ).isEqualTo( MockEnum.A );
		settings.set( "enum", "a" );
		assertThat( settings.get( "enum", MockEnum.class ) ).isEqualTo( MockEnum.A );
		settings.set( "enum", "b" );
		assertThat( settings.get( "enum", MockEnum.class ) ).isEqualTo( MockEnum.B );
		settings.set( "enum", "x" );
		assertThat( settings.get( "enum", MockEnum.class ) ).isNull();
		settings.set( "enum", " " );
		assertThat( settings.get( "enum", MockEnum.class ) ).isNull();
		settings.set( "enum", "" );
		assertThat( settings.get( "enum", MockEnum.class ) ).isNull();
		settings.set( "enum", null );
		assertThat( settings.get( "enum", MockEnum.class ) ).isNull();
	}

	@Test
	void testGetUsingDefaultValue() {
		assertThat( settings.get( "missing", "default" ) ).isEqualTo( "default" );
	}

	@Test
	void testGetUsingDefaultValueWithObject() {
		assertThat( settings.get( "missing", false ) ).isEqualTo( "false" );
	}

	@Test
	void testGetBooleanUsingDefaultValue() {
		assertThat( settings.get( "missing", Boolean.class, false ) ).isFalse();
	}

	@Test
	void testGetValueFromDefaultSettings() {
		String key = "key";
		String value = "value";
		String defaultValue = "defaultValue";

		Map<String, Object> defaultValues = new HashMap<>();
		defaultValues.put( key, defaultValue );

		// Start by checking the value is null
		assertThat( settings.get( key, String.class ) ).isNull();

		settings.set( key, value );
		assertThat( settings.get( key, String.class ) ).isEqualTo( value );

		settings.set( key, null );
		assertThat( settings.get( key, String.class ) ).isNull();

		// Test the default settings
		settings.setDefaultValues( defaultValues );
		assertThat( settings.get( key, String.class ) ).isEqualTo( defaultValue );

		settings.set( key, value );
		assertThat( settings.get( key, String.class ) ).isEqualTo( value );

		settings.set( key, null );
		assertThat( settings.get( key, String.class ) ).isEqualTo( defaultValue );

		settings.setDefaultValues( null );
		assertThat( settings.get( key, String.class ) ).isNull();
	}

	@Test
	void testGetBooleanFromStringDefault() {
		String key = "key";
		String value = "false";
		String defaultValue = "true";

		Map<String, Object> defaultValues = new HashMap<>();
		defaultValues.put( key, defaultValue );
		settings.setDefaultValues( defaultValues );
		assertThat( settings.get( "key", Boolean.class ) ).isTrue();

		settings.set( "key", value );
		assertThat( settings.get( "key", Boolean.class ) ).isFalse();

		settings.set( "key", null );
		assertThat( settings.get( "key", Boolean.class ) ).isTrue();
	}

	@Test
	void testUpdatedEvent() {
		SettingsEventWatcher watcher = new SettingsEventWatcher();
		settings.register( SettingsEvent.ANY, watcher );
		assertThat( watcher.getEvents().size() ).isEqualTo( 0 );

		// The value does not change so there should not be an extra event
		settings.set( "a", null );
		assertThat( watcher.getEvents().size() ).isEqualTo( 0 );

		settings.set( "a", "A" );
		SettingsEventAssert.assertThat( watcher.getEvents().get( 0 ) ).hasValues( settings, SettingsEvent.CHANGED, settings.getPath(), "a", null, "A" );
		assertThat( watcher.getEvents().size() ).isEqualTo( 1 );

		// The value does not change so there should not be an extra event
		settings.set( "a", "A" );
		assertThat( watcher.getEvents().size() ).isEqualTo( 1 );

		settings.set( "a", null );
		SettingsEventAssert.assertThat( watcher.getEvents().get( 1 ) ).hasValues( settings, SettingsEvent.CHANGED, settings.getPath(), "a", null, null );
		assertThat( watcher.getEvents().size() ).isEqualTo( 2 );

		// The value does not change so there should not be an extra event
		settings.set( "a", null );
		assertThat( watcher.getEvents().size() ).isEqualTo( 2 );
	}

	@Test
	void testShallowCopyFrom() {
		Settings source = new MapSettings();
		source.set( "a", "A" );
		source.set( "b", 2 );
		settings.copyFrom( source );
		assertThat( settings.get( "a" ) ).isEqualTo( "A" );
		assertThat( settings.get( "b", Integer.class ) ).isEqualTo( 2 );
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
		assertThat( settings.get( "a" ) ).isEqualTo( "A" );
		assertThat( settings.get( "b", Integer.class ) ).isEqualTo( 2 );
		assertThat( settings.nodeExists( "deep" ) ).isTrue();
		assertThat( settings.getNode( "deep" ).get( "c" ) ).isEqualTo( "see" );
		assertThat( settings.getNode( "deep" ).get( "d" ) ).isEqualTo( "D" );
	}

	@Test
	void testDelete() {
		assertThat( settings.nodeExists( "test" ) ).isFalse();
		Settings test = settings.getNode( "test" );
		test.flush();
		assertThat( settings.nodeExists( "test" ) ).isTrue();
		test.delete();
		assertThat( settings.nodeExists( "test" ) ).isFalse();
	}

}
