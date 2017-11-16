package com.xeomar.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;

public class XmlUtil {

	private static final Logger log = LoggerFactory.getLogger( XmlUtil.class );

	private static final int DEFAULT_INDENT = 2;

	public static Document loadXmlDocument( File file ) throws SAXException, IOException {
		return loadXmlDocument( new InputStreamReader( new FileInputStream( file ), TextUtil.ENCODING ) );
	}

	public static Document loadXmlDocument( String uri ) throws SAXException, IOException {
		if( uri == null ) return null;

		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			document = factory.newDocumentBuilder().parse( uri );
			document.getDocumentElement().normalize();
		} catch( ParserConfigurationException exception ) {
			// Intentionally ignore exception.
		}

		return document;
	}

	public static Document loadXmlDocument( Reader reader ) throws SAXException, IOException {
		if( reader == null ) return null;

		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			document = factory.newDocumentBuilder().parse( new InputSource( reader ) );
			document.getDocumentElement().normalize();
		} catch( ParserConfigurationException exception ) {
			// Intentionally ignore exception.
		}

		return document;
	}

	public static Document loadXmlDocument( InputStream stream ) throws SAXException, IOException {
		if( stream == null ) return null;

		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			document = factory.newDocumentBuilder().parse( new InputSource( stream ) );
			document.getDocumentElement().normalize();
		} catch( ParserConfigurationException exception ) {
			// Intentionally ignore exception.
		}

		return document;
	}

	public static Node getNode( Node node, String path ) {
		if( node == null || TextUtil.isEmpty( path ) ) return null;

		Node value = null;
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			value = (Node)xpath.evaluate( path, node, XPathConstants.NODE );
		} catch( XPathExpressionException exception ) {
			log.error( "Error evaluating xpath: " + path, new Exception( path, exception ) );
		}

		return value;
	}

	public static void save( Node node, File file ) throws IOException {
		try( Writer writer = new OutputStreamWriter( new BufferedOutputStream( new FileOutputStream( file ) ), "UTF-8" ) ) {
			save( node, writer );
		}
	}

	public static void save( Node node, Writer output ) throws IOException {
		save( node, output, DEFAULT_INDENT );
	}

	public static void save( Node node, Writer output, int indentAmount ) throws IOException {
		format( new DOMSource( node ), new StreamResult( output ), indentAmount );
	}

	public static void format( InputStream input, OutputStream output ) throws IOException {
		format( input, output, DEFAULT_INDENT );
	}

	public static void format( InputStream input, OutputStream output, int indent ) throws IOException {
		format( new StreamSource( input ), new StreamResult( output ), indent );
	}

	public static void format( Reader reader, Writer writer ) throws IOException {
		format( reader, writer, DEFAULT_INDENT );
	}

	public static void format( Reader reader, Writer writer, int indent ) throws IOException {
		format( new StreamSource( reader ), new StreamResult( writer ), indent );
	}

	public static String toString( Node node ) {
		StringWriter output = new StringWriter();
		try {
			save( node, output );
		} catch( IOException exception ) {
			log.error( "Error converting node to string", exception );
		}
		return output.toString();
	}

	public static String getPath( Node node ) {
		if( node == null ) return null;

		StringBuilder builder = new StringBuilder();
		while( node.getNodeType() != Node.DOCUMENT_NODE ) {
			builder.insert( 0, node.getNodeName() );
			builder.insert( 0, "/" );
			node = node.getParentNode();
		}

		return builder.toString();
	}

	private static void format( Source source, Result result, int indent ) throws IOException {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
			transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
			transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", String.valueOf( indent ) );
			transformer.transform( source, result );
		} catch( TransformerException exception ) {
			throw new IOException( exception );
		}
	}

}
