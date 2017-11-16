package com.xeomar.util;

import junit.framework.TestCase;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;

public class XmlUtilTest extends TestCase {

	@Test
	public void testLoadXmlDocument() throws Exception {
		InputStream input = XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" );
		Document document = XmlUtil.loadXmlDocument( input );
		assertNotNull( document );
	}

	@Test
	public void testLoadXmlDocumentWithNullUri() throws Exception {
		Document document = XmlUtil.loadXmlDocument( (String)null );
		assertNull( document );
	}

	@Test
	public void testLoadXmlDocumentWithNullReader() throws Exception {
		Document document = XmlUtil.loadXmlDocument( (Reader)null );
		assertNull( document );
	}

	@Test
	public void testLoadXmlDocumentWithNullStream() throws Exception {
		Document document = XmlUtil.loadXmlDocument( (InputStream)null );
		assertNull( document );
	}

	@Test
	public void testGetDocumentType() throws Exception {
		InputStream input = XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" );
		Document document = XmlUtil.loadXmlDocument( input );
		assertEquals( "test", document.getDocumentElement().getNodeName() );
	}

	@Test
	public void testFormat() throws Exception {
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
	public void testFormatWithIndentSize() throws Exception {
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

	public void testGetPath() throws Exception {
		assertNull( XmlUtil.getPath( null ) );

		Document document = XmlUtil.loadXmlDocument( XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" ) );
		assertEquals( "/test", XmlUtil.getPath( document.getFirstChild() ) );
		assertEquals( "/test/a", XmlUtil.getPath( document.getFirstChild().getFirstChild().getNextSibling() ) );
	}

}
