package com.avereon.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

	@SuppressWarnings( { "WeakerAccess", "unused" } )
	static class Item {

		private String name;

		@JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss.SSS" )
		private Date timestamp;

		private URI site;

		public String getName() {
			return name;
		}

		public void setName( String name ) {
			this.name = name;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public void setTimestamp( Date timestamp ) {
			this.timestamp = timestamp;
		}

		public URI getSite() {
			return site;
		}

		public void setSite( URI site ) {
			this.site = site;
		}
	}

}
