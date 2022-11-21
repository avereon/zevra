package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenReplacingReaderTest {

	@Test
	void testReadWithGoodReplacementTokenAndValueMap() throws Exception {
		// given
		String template = "Hello ${name}!";
		Map<String, String> values = Map.of( "name", "World" );
		TokenReplacingReader reader = new TokenReplacingReader( new StringReader( template ), values );

		// when
		String result = IoUtil.toString( reader );

		// then
		assertThat( result ).isEqualTo( "Hello World!" );
	}

	@Test
	void testReadWithNoReplacementToken() throws Exception {
		// given
		String template = "Hello World!";
		Map<String, String> values = Map.of( "name", "Not Used" );
		TokenReplacingReader reader = new TokenReplacingReader( new StringReader( template ), values );

		// when
		String result = IoUtil.toString( reader );

		// then
		assertThat( result ).isEqualTo( "Hello World!" );
	}

	@Test
	void testReadWithBadReplacementKey() throws Exception {
		// given
		String template = "Hello ${name}!";
		Map<String, String> values = Map.of( "bad-name", "World" );
		TokenReplacingReader reader = new TokenReplacingReader( new StringReader( template ), values );

		// when
		String result = IoUtil.toString( reader );

		// then
		assertThat( result ).isEqualTo( "Hello ${name}!" );
	}

}
