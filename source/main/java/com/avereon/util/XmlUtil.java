package com.avereon.util;

import lombok.CustomLog;
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
import java.nio.charset.StandardCharsets;

/**
 * This class provides utility methods for working with XML documents.
 */
@CustomLog
@SuppressWarnings( "WeakerAccess" )
public class XmlUtil {

	private static final String INDENT_AMOUNT = "{http://xml.apache.org/xslt}indent-amount";

	private static final int DEFAULT_INDENT = 2;

	/**
	 * Loads an XML document from the given file.
	 *
	 * @param file The file to load the XML document from.
	 * @return The loaded XML document.
	 * @throws SAXException If there is an error parsing the XML document.
	 * @throws IOException If there is an error reading the file.
	 */
	public static Document loadXmlDocument( File file ) throws SAXException, IOException {
		return loadXmlDocument( new InputStreamReader( new FileInputStream( file ), TextUtil.ENCODING ) );
	}

	/**
	 * Load an XML document from a uri.
	 *
	 * @param uri The uri from which to load the XML document
	 * @return The XML document
	 * @throws SAXException If there is an XML parse problem
	 * @throws IOException If there is an I/O problem
	 */
	public static Document loadXmlDocument( String uri ) throws SAXException, IOException {
		if( uri == null ) return null;
		return loadXmlDocument( new InputSource( uri ) );
	}

	/**
	 * Load an XML document from a reader.
	 *
	 * @param reader The reader from which to load the XML document
	 * @return The XML document
	 * @throws SAXException If there is an XML parse problem
	 * @throws IOException If there is an I/O problem
	 */
	public static Document loadXmlDocument( Reader reader ) throws SAXException, IOException {
		if( reader == null ) return null;
		return loadXmlDocument( new InputSource( reader ) );
	}

	/**
	 * Load an XML document from a stream.
	 *
	 * @param stream The stream from which to load the XML document
	 * @return The XML document
	 * @throws SAXException If there is an XML parse problem
	 * @throws IOException If there is an I/O problem
	 */
	public static Document loadXmlDocument( InputStream stream ) throws SAXException, IOException {
		if( stream == null ) return null;
		return loadXmlDocument( new InputSource( stream ) );
	}

	/**
	 * Query an DOM node by path expression for a DOM node. This method returns
	 * the first DOM node that matches the path expression.
	 *
	 * @param node The DOM node to query
	 * @param path The XPath path expression
	 * @return The first DOM node that matches the path expression
	 */
	public static Node query( Node node, String path ) {
		if( node == null || TextUtil.isEmpty( path ) ) return null;

		Node value = null;
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			value = (Node)xpath.evaluate( path, node, XPathConstants.NODE );
		} catch( XPathExpressionException exception ) {
			log.atSevere().withCause( new Exception( path, exception ) ).log( "Error evaluating xpath: %s", path );
		}

		return value;
	}

	/**
	 * Save a DOM node (an XML Document is also a DOM node) to a file.
	 *
	 * @param node The DOM node to save
	 * @param file The file to which to write the XML
	 * @throws IOException If there is an I/O problem
	 */
	public static void save( Node node, File file ) throws IOException {
		try( Writer writer = new OutputStreamWriter( new BufferedOutputStream( new FileOutputStream( file ) ), StandardCharsets.UTF_8 ) ) {
			save( node, writer );
		}
	}

	/**
	 * Save a DOM node (an XML Document is also a DOM node) to a writer.
	 *
	 * @param node The DOM node to save
	 * @param writer The writer to which to write the XML
	 * @throws IOException If there is an I/O problem
	 */
	public static void save( Node node, Writer writer ) throws IOException {
		save( node, writer, DEFAULT_INDENT );
	}

	/**
	 * Save a DOM node (an XML Document is also a DOM node) to a writer.
	 *
	 * @param node The DOM node to save
	 * @param writer The writer to which to write the XML
	 * @param indent The number of spaces to use as an indent
	 * @throws IOException If there is an I/O problem
	 */
	public static void save( Node node, Writer writer, int indent ) throws IOException {
		format( new DOMSource( node ), new StreamResult( writer ), indent );
	}

	/**
	 * Format XML from an input stream and write to an output stream.
	 *
	 * @param input The source of the XML content
	 * @param output The target of the XML content
	 * @throws IOException If there is an I/O problem
	 */
	public static void format( InputStream input, OutputStream output ) throws IOException {
		format( input, output, DEFAULT_INDENT );
	}

	/**
	 * Format XML from an input stream and write to an output stream.
	 *
	 * @param input The source of the XML content
	 * @param output The target of the XML content
	 * @param indent The number of spaces to use as an indent
	 * @throws IOException If there is an I/O problem
	 */
	public static void format( InputStream input, OutputStream output, int indent ) throws IOException {
		format( new StreamSource( input ), new StreamResult( output ), indent );
	}

	/**
	 * Format XML from a reader and write to a writer.
	 *
	 * @param reader The source of the XML content
	 * @param writer The target of the XML content
	 * @throws IOException If there is an I/O problem
	 */
	public static void format( Reader reader, Writer writer ) throws IOException {
		format( reader, writer, DEFAULT_INDENT );
	}

	/**
	 * Format XML from a reader and write to a writer.
	 *
	 * @param reader The source of the XML content
	 * @param writer The target of the XML content
	 * @param indent The number of spaces to use as an indent
	 * @throws IOException If there is an I/O problem
	 */
	public static void format( Reader reader, Writer writer, int indent ) throws IOException {
		format( new StreamSource( reader ), new StreamResult( writer ), indent );
	}

	/**
	 * Format a DOM node to a string.
	 *
	 * @param node The DOM node
	 */
	public static String toString( Node node ) {
		StringWriter output = new StringWriter();
		try {
			save( node, output );
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Error converting node to string" );
		}
		return output.toString();
	}

	/**
	 * Get the path of the specified DOM node. This method simply creates a path
	 * based on the node names from the root node to the specified node. The path
	 * is generated as forward slash separated strings. Example: /root/node/leaf
	 *
	 * @param node The DOM node for which to get the path
	 * @return The path of the DOM node
	 */
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

	/**
	 * Loads an XML document from the given input source.
	 *
	 * @param source the input source from which to load the document
	 * @return the loaded XML document, or null if the source is null
	 * @throws SAXException if an error occurs during parsing
	 * @throws IOException if an error occurs while reading the document from the input source
	 */
	private static Document loadXmlDocument( InputSource source ) throws SAXException, IOException {
		if( source == null ) return null;

		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			document = factory.newDocumentBuilder().parse( source );
			document.getDocumentElement().normalize();
		} catch( ParserConfigurationException exception ) {
			// Intentionally ignore exception.
		}

		return document;
	}

	/**
	 * This method formats the given source XML using XSLT and writes the
	 * formatted result to the specified output.
	 *
	 * @param source The XML source to be formatted.
	 * @param result The output where the formatted XML will be written.
	 * @param indent The number of spaces to use for each level of indentation.
	 * @throws IOException If an I/O error occurs while formatting the XML.
	 */
	private static void format( Source source, Result result, int indent ) throws IOException {
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "no" );
			transformer.setOutputProperty( OutputKeys.METHOD, "xml" );
			transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.setOutputProperty( INDENT_AMOUNT, String.valueOf( indent ) );
			transformer.transform( source, result );
		} catch( TransformerException exception ) {
			throw new IOException( exception );
		}
	}

}
