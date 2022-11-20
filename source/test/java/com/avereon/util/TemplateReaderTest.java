package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateReaderTest {

	@Test
	void testRead() throws Exception {
		// given
		String template = "Hello ${name}!";
		Map<String, String> values = Map.of( "name", "World" );
		TemplateReader reader = new TemplateReader( new StringReader( template ), values );

		// when
		String result = IoUtil.toString( reader );

		// then
		assertThat( result ).isEqualTo( "Hello World!" );
	}

}
