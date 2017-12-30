package com.xeomar.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JsonTest {

	@Test
	public void testLoad() throws IOException {
		InputStream input = getClass().getResourceAsStream( "/json.text.json" );
		JsonTest.Item item = new ObjectMapper( ).readerFor( new TypeReference<JsonTest.Item>() {} ).readValue( input );

		assertThat( item.getName(), is( "Xeomar" ) );
		assertThat( item.getTimestamp().getTime(), is( 1296848535284L ) );
		assertThat( item.getSite(), is( URI.create( "http://www.xeomar.com" ) ) );
	}

	public static class Item {

		private String name;

		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
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
