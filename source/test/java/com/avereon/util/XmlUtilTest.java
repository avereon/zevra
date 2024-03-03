package com.avereon.util;

import com.avereon.product.Version;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

class XmlUtilTest {

	@Test
	void testLoadXmlDocument() throws Exception {
		InputStream input = XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" );
		Document document = XmlUtil.loadXmlDocument( input );
		assertThat( document ).isNotNull();
	}

	@Test
	void testLoadXmlFile() throws Exception {
		Document document = XmlUtil.loadXmlDocument( new File( "source/test/resources/xml.test.xml" ) );
		assertThat( document ).isNotNull();
	}

	@Test
	void testLoadXmlDocumentWithNullUri() throws Exception {
		assertThat( XmlUtil.loadXmlDocument( (String)null ) ).isNull();
	}

	@Test
	void testLoadXmlDocumentWithNullReader() throws Exception {
		assertThat( XmlUtil.loadXmlDocument( (Reader)null ) ).isNull();
	}

	@Test
	void testLoadXmlDocumentWithNullStream() throws Exception {
		assertThat( XmlUtil.loadXmlDocument( (InputStream)null ) ).isNull();
	}

	@Test
	void testGetDocumentType() throws Exception {
		InputStream input = XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" );
		Document document = XmlUtil.loadXmlDocument( input );
		assertThat( document ).isNotNull();
		assertThat( document.getDocumentElement().getNodeName() ).isEqualTo( "test" );
	}

	@Test
	void testFormat() throws Exception {
		String data = "<tag><indent/></tag>";
		String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tag>\n  <indent/>\n</tag>";

		ByteArrayInputStream input = new ByteArrayInputStream( data.getBytes() );
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		XmlUtil.format( input, output );

		// As of Java 7, the transformer creates slightly different output.
		Version current = new Version( System.getProperty( "java.version" ) );
		if( current.compareTo( new Version( "1.7" ) ) >= 0 ) {
			test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tag>\n  <indent/>\n</tag>";
		}

		assertThat( output.toString().replace( "\r\n", "\n" ).trim() ).isEqualTo( test );
	}

	@Test
	void testFormatWithIndentSize() throws Exception {
		String data = "<tag><indent/></tag>";
		String test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tag>\n   <indent/>\n</tag>";
		ByteArrayInputStream input = new ByteArrayInputStream( data.getBytes() );
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		XmlUtil.format( input, output, 3 );

		// As of Java 7, the transformer creates slightly different output.
		Version current = new Version( System.getProperty( "java.version" ) );
		if( current.compareTo( new Version( "1.7" ) ) >= 0 ) {
			test = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tag>\n   <indent/>\n</tag>";
		}

		assertThat( output.toString().replace( "\r\n", "\n" ).trim() ).isEqualTo( test );
	}

	@Test
	void testGetPath() throws Exception {
		Document document = XmlUtil.loadXmlDocument( XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" ) );

		assertThat( XmlUtil.getPath( null ) ).isNull();
		assertThat( XmlUtil.getPath( document ) ).isNotNull();
		assertThat( XmlUtil.getPath( document.getFirstChild() ) ).isEqualTo( "/test" );
		assertThat( XmlUtil.getPath( document.getFirstChild().getFirstChild().getNextSibling() ) ).isEqualTo( "/test/a" );
	}

}
