package com.avereon.util;

import com.avereon.product.Version;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.*;

class XmlUtilTest {

	@Test
	void testLoadXmlDocument() throws Exception {
		InputStream input = XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" );
		Document document = XmlUtil.loadXmlDocument( input );
		assertNotNull( document );
	}

	@Test
	void testLoadXmlDocumentWithNullUri() throws Exception {
		assertNull( XmlUtil.loadXmlDocument( (String)null ) );
	}

	@Test
	void testLoadXmlDocumentWithNullReader() throws Exception {
		assertNull( XmlUtil.loadXmlDocument( (Reader)null ) );
	}

	@Test
	void testLoadXmlDocumentWithNullStream() throws Exception {
		assertNull( XmlUtil.loadXmlDocument( (InputStream)null ) );
	}

	@Test
	void testGetDocumentType() throws Exception {
		InputStream input = XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" );
		Document document = XmlUtil.loadXmlDocument( input );
		assertEquals( "test", document.getDocumentElement().getNodeName() );
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

		assertEquals( test, output.toString().replace( "\r\n", "\n" ).trim() );
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

		assertEquals( test, output.toString().replace( "\r\n", "\n" ).trim() );
	}

	@Test
	void testGetPath() throws Exception {
		Document document = XmlUtil.loadXmlDocument( XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" ) );

		assertNull( XmlUtil.getPath( null ) );
		assertEquals( "/test", XmlUtil.getPath( document.getFirstChild() ) );
		assertEquals( "/test/a", XmlUtil.getPath( document.getFirstChild().getFirstChild().getNextSibling() ) );
	}

}
