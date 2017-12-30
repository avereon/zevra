package com.xeomar.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Test the storage and retrieval of Java beans using JSON
 */
public class JsonSettingsTest {

	@Test
	@SuppressWarnings( "unchecked" )
	public void testStoreBeanMap() throws Exception {
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

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );

		String store = mapper.writeValueAsString( expectedBeanMap );

		// Load the beans
		TypeReference<Map<String, MockBean>> typeRef = new TypeReference<Map<String, MockBean>>() {};
		assertThat( ((Map<String, MockBean>)mapper.readerFor( typeRef ).readValue( store )).values(), containsInAnyOrder( bean1, bean2, bean3 ) );

		//System.out.println( store );
	}

	@Test
	public void testStoreBeanList() throws Exception {
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

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );

		String store = mapper.writeValueAsString( expectedBeanList );

		// Load the beans
		TypeReference<List<MockBean>> typeRef = new TypeReference<List<MockBean>>() {};
		assertThat( mapper.readerFor( typeRef ).readValue( store ), containsInAnyOrder( bean1, bean2, bean3 ) );

		//System.out.println( store );
	}

	@Test
	public void testStoreBeanSet() throws Exception {
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

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );

		String store = mapper.writeValueAsString( expectedBeanSet );

		// Load the beans
		TypeReference<Set<MockBean>> typeRef = new TypeReference<Set<MockBean>>() {};
		assertThat( mapper.readerFor( typeRef ).readValue( store ), containsInAnyOrder( bean1, bean2, bean3 ) );

		//System.out.println( store );
	}
}
