package com.xeomar.settings;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MarshallingPerformanceTest {

	private long jsonSaveAccumulator;

	private long jsonLoadAccumulator;

	private long yamlSaveAccumulator;

	private long yamlLoadAccumulator;

	@Test
	public void testMarshallingPerformance() throws Exception {
		int runCount = 10; // Not higher than 1000
		int beanCount = 10; // Not higher than 10000

		for( int runIndex = 0; runIndex < runCount; runIndex++ ) {
			// Create the beans
			Map<String, MockBean> beans = new HashMap<>();
			for( int beanIndex = 0; beanIndex < beanCount; beanIndex++ ) {
				MockBean bean = new MockBean();
				bean.setIntegerPrimitiveProperty( beanIndex );
				bean.setIntegerProperty( beanIndex );
				bean.setStringProperty( String.valueOf( beanIndex ) );
				beans.put( UUID.randomUUID().toString(), bean );
			}
			// Save and load the beans
			testJson( beans, runIndex == 0 );
		}
		System.out.println( "JSON save=" + (jsonSaveAccumulator / (runCount - 1)) + "  load=" + (jsonLoadAccumulator / (runCount - 1)) );

		for( int runIndex = 0; runIndex < runCount; runIndex++ ) {
			// Create the beans
			Map<String, MockBean> beans = new HashMap<>();
			for( int beanIndex = 0; beanIndex < beanCount; beanIndex++ ) {
				MockBean bean = new MockBean();
				bean.setIntegerPrimitiveProperty( beanIndex );
				bean.setIntegerProperty( beanIndex );
				bean.setStringProperty( String.valueOf( beanIndex ) );
				beans.put( UUID.randomUUID().toString(), bean );
			}
			// Save and load the beans
			testYaml( beans, runIndex == 0 );
		}

		System.out.println( "YAML save=" + (yamlSaveAccumulator / (runCount - 1)) + "  load=" + (yamlLoadAccumulator / (runCount - 1)) );
	}

	private void testJson( Map<String, MockBean> beans, boolean first ) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure( SerializationFeature.WRITE_NULL_MAP_VALUES, false );
		mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );
		TypeReference<Map<String, MockBean>> typeRef = new TypeReference<Map<String, MockBean>>() {};

		long startTime = System.currentTimeMillis();
		String store = mapper.writeValueAsString( beans );
		long saveTime = System.currentTimeMillis();
		// Load the beans
		Map<String, MockBean> result = mapper.readerFor( typeRef ).readValue( store );
		long loadTime = System.currentTimeMillis();
		//assertThat( ((Map<String, MockBean>)mapper.readerFor( typeRef ).readValue( store )).values(), containsInAnyOrder( beans.values().iterator() ) );

		if( first ) {
			System.out.println( "JSON first: save=" + (saveTime - startTime) + "  load=" + (loadTime - saveTime) );
		} else {
			jsonSaveAccumulator += saveTime - startTime;
			jsonLoadAccumulator += loadTime - saveTime;
		}
	}

	private void testYaml( Map<String, MockBean> beans, boolean first ) {
		DumperOptions options = new DumperOptions();
		options.setLineBreak( DumperOptions.LineBreak.UNIX );
		options.setSplitLines( false );

		long startTime = System.currentTimeMillis();
		String store = new Yaml( options ).dump( beans );
		long saveTime = System.currentTimeMillis();
		Map<String, MockBean> result = (Map<String, MockBean>)new Yaml( options ).load( store );
		long loadTime = System.currentTimeMillis();
		//assertThat( ((Map<String, MockBean>)mapper.readerFor( typeRef ).readValue( store )).values(), containsInAnyOrder( beans.values().iterator() ) );

		if( first ) {
			System.out.println( "YAML first: save=" + (saveTime - startTime) + "  load=" + (loadTime - saveTime) );
		} else {
			yamlSaveAccumulator += saveTime - startTime;
			yamlLoadAccumulator += loadTime - saveTime;
		}
	}

}
