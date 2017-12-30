package com.xeomar.settings;

import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Test the storage and retrieval of Java beans using YAML
 */
public class YamlSettingsTest {

	@Test
	public void testStoreBeanMap() {
		// Create the beans
		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );
		MockBean bean3 = new MockBean();
		bean3.setIntegerPrimitiveProperty( 3 );

		Map<String, MockBean> expectedBeanMap = new HashMap<>();
		expectedBeanMap.put( "a", bean1 );
		expectedBeanMap.put( "b", bean2 );
		expectedBeanMap.put( "c", bean3 );

		// Save the beans
		String store = new Yaml().dump( expectedBeanMap );

		// Load the beans
		assertThat( ((Map<String, MockBean>)new Yaml().load( store )).values(), containsInAnyOrder( bean1, bean2, bean3 ) );

		//System.out.println( store );
	}

	@Test
	public void testStoreBeanList() {
		// Create the beans
		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );
		MockBean bean3 = new MockBean();
		bean3.setIntegerPrimitiveProperty( 3 );

		// Create the list
		List<MockBean> expectedBeanList = new ArrayList<>();
		expectedBeanList.add( bean1 );
		expectedBeanList.add( bean2 );
		expectedBeanList.add( bean3 );

		// Save the beans
		String store = new Yaml().dump( expectedBeanList );

		// Load the beans
		assertThat( (List<MockBean>)new Yaml().load( store ), contains( bean1, bean2, bean3 ) );

		//System.out.println( store );
	}

	@Test
	public void testStoreBeanSet() {
		// Create the beans
		MockBean bean1 = new MockBean();
		bean1.setIntegerPrimitiveProperty( 1 );
		MockBean bean2 = new MockBean();
		bean2.setIntegerPrimitiveProperty( 2 );
		MockBean bean3 = new MockBean();
		bean3.setIntegerPrimitiveProperty( 3 );

		// Create the list
		Set<MockBean> expectedBeanSet = new HashSet<>();
		expectedBeanSet.add( bean1 );
		expectedBeanSet.add( bean2 );
		expectedBeanSet.add( bean3 );

		// Save the beans
		DumperOptions options = new DumperOptions();
		options.setLineBreak( DumperOptions.LineBreak.UNIX );
		//options.setPrettyFlow( true );
		options.setSplitLines( false );
		//options.setWidth( 256 );
		String store = new Yaml( options ).dump( expectedBeanSet );

		// Load the beans
		assertThat( (Set<MockBean>)new Yaml( options ).load( store ), containsInAnyOrder( bean1, bean2, bean3 ) );

		//System.out.println( store );
	}

}
