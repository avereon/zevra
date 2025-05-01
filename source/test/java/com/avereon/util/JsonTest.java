package com.avereon.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JsonTest {

	@Test
	void testLoad() throws IOException {
		InputStream input = getClass().getResourceAsStream( "/json.text.json" );
		JsonTest.Item item = new ObjectMapper().readerFor( new TypeReference<JsonTest.Item>() {} ).readValue( input );

		assertThat( item.getName() ).isEqualTo( "Avereon" );
		assertThat( item.getTimestamp().getTime() ).isEqualTo( 1296848535284L );
		assertThat( item.getSite() ).isEqualTo( URI.create( "http://www.avereon.com" ) );
	}

	@Setter
	@Getter
	@SuppressWarnings( { "WeakerAccess", "unused" } )
	static class Item {

		private String name;

		@JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss.SSS" )
		private Date timestamp;

		private URI site;

	}

}
