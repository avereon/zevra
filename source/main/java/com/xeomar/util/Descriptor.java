package com.xeomar.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Descriptor {

	private static final Logger log = LoggerFactory.getLogger( Descriptor.class );

	private Node node;

	private Map<String, List<String>> attrNames = new ConcurrentHashMap<>();

	private Map<String, List<String>> names = new ConcurrentHashMap<>();

	private List<String> paths;

	public Descriptor() {}

	public Descriptor( URI uri ) throws IOException {
		if( uri == null ) return;
		try {
			node = XmlUtil.loadXmlDocument( uri.toString() );
		} catch( SAXException exception ) {
			throw new IOException( exception );
		}
	}

	public Descriptor( URL url ) throws IOException {
		if( url == null ) return;
		try {
			node = XmlUtil.loadXmlDocument( url.toString() );
		} catch( SAXException exception ) {
			throw new IOException( exception );
		}
	}

	public Descriptor( Reader reader ) throws IOException {
		if( reader == null ) return;
		try {
			node = XmlUtil.loadXmlDocument( reader );
		} catch( SAXException exception ) {
			throw new IOException( exception );
		}
	}

	public Descriptor( InputStream input ) throws IOException {
		if( input == null ) throw new NullPointerException( "Input stream cannot be null." );
		try {
			node = XmlUtil.loadXmlDocument( input );
		} catch( SAXException exception ) {
			throw new IOException( exception );
		}
	}

	public Descriptor( Node node ) {
		if( node == null ) return;
		this.node = node;
	}

	public Document getDocument() {
		return (node instanceof Document) ? (Document)node : node.getOwnerDocument();
	}

	public Node getNode() {
		return node;
	}

	public List<String> getAttributeNames( String path ) {
		List<String> names = this.attrNames.get( path );
		if( names == null ) {
			names = listAttributeNames( getNode( path ) );
			this.attrNames.put( path, names );
		}
		return names;
	}

	public List<String> getNames( String path ) {
		List<String> names = this.names.get( path );
		if( names == null ) {
			names = listNames( getNode( path ) );
			this.names.put( path, names );
		}
		return names;
	}

	public List<String> getPaths() {
		if( paths == null ) paths = listPaths( node );
		return paths;
	}

	public Node getNode( String path ) {
		return getNode( this.node, path );
	}

	public Node[] getNodes( String path ) {
		return getNodes( this.node, path );
	}

	public String getValue( String path ) {
		return getValue( this.node, path );
	}

	public String getValue( String path, String defaultValue ) {
		return getValue( this.node, path, defaultValue );
	}

	/**
	 * Get an array of all the values that have the same path.
	 *
	 * @param path
	 * @return An array of values with the same path.
	 */
	public String[] getValues( String path ) {
		return getValues( this.node, path );
	}

	@Override
	public String toString() {
		return XmlUtil.toString( node );
	}

	public static Node getNode( Node node, String path ) {
		if( node == null || TextUtil.isEmpty( path ) ) return null;

		Node value = null;
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			value = (Node)xpath.evaluate( path, node, XPathConstants.NODE );
		} catch( XPathExpressionException exception ) {
			log.error( "Error evaluating xpath: " + path, new Exception( path, exception ) );
		}

		return value;
	}

	public static Node[] getNodes( Node node, String path ) {
		if( node == null || TextUtil.isEmpty( path ) ) return null;

		NodeList nodes = null;
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			nodes = (NodeList)xpath.evaluate( path, node, XPathConstants.NODESET );
		} catch( XPathExpressionException exception ) {
			log.error( "Error evaluating xpath: " + path, new Exception( path, exception ) );
		}
		if( nodes == null ) return null;

		ArrayList<Node> values = new ArrayList<>();
		int count = nodes.getLength();
		for( int index = 0; index < count; index++ ) {
			values.add( nodes.item( index ) );
		}

		return values.toArray( new Node[ values.size() ] );
	}

	public static String getValue( Node node, String path ) {
		if( node == null || TextUtil.isEmpty( path ) ) return null;

		String value;
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			value = (String)xpath.evaluate( "normalize-space(" + path + ")", node, XPathConstants.STRING );
		} catch( XPathExpressionException exception ) {
			throw new RuntimeException( path, exception );
		}

		if( TextUtil.isEmpty( value ) ) return null;

		return value;
	}

	public static String getValue( Node node, String path, String defaultValue ) {
		String value = getValue( node, path );
		if( value == null ) return defaultValue;
		return value;
	}

	/**
	 * Get an array of all the values in the node that have the same path.
	 *
	 * @param path
	 * @return An array of values with the same path.
	 */
	public static String[] getValues( Node node, String path ) {
		if( node == null || TextUtil.isEmpty( path ) ) return null;

		NodeList nodes = null;
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			nodes = (NodeList)xpath.evaluate( path, node, XPathConstants.NODESET );
		} catch( XPathExpressionException exception ) {
			log.error( "Error evaluating xpath: " + path, new Exception( path, exception ) );
		}
		if( nodes == null ) return null;

		ArrayList<String> values = new ArrayList<>();
		int count = nodes.getLength();
		for( int index = 0; index < count; index++ ) {
			try {
				values.add( (String)xpath.evaluate( "normalize-space()", nodes.item( index ), XPathConstants.STRING ) );
			} catch( XPathExpressionException exception ) {
				log.error( "Error evaluating xpath: " + path, new Exception( path, exception ) );
			}
		}

		return values.toArray( new String[ values.size() ] );
	}

	public static String getAttribute( Node node, String name ) {
		Node attribute = node.getAttributes().getNamedItem( name );
		return attribute == null ? null : attribute.getNodeValue();
	}

	private List<String> listAttributeNames( Node parent ) {
		List<String> names = new ArrayList<>();
		if( parent == null ) return names;

		Node node;
		NamedNodeMap map = parent.getAttributes();
		if( map == null ) return names;

		int count = map.getLength();
		for( int index = 0; index < count; index++ ) {
			node = map.item( index );
			if( node instanceof Attr ) names.add( node.getNodeName() );
		}

		return names;
	}

	private List<String> listNames( Node parent ) {
		List<String> names = new ArrayList<>();
		if( parent == null ) return names;

		Node node;
		NodeList list = parent.getChildNodes();
		int count = list.getLength();
		for( int index = 0; index < count; index++ ) {
			node = list.item( index );
			if( node instanceof Element ) names.add( node.getNodeName() );
		}

		return names;
	}

	private List<String> listPaths( Node parent ) {
		List<String> paths = new ArrayList<>();
		if( parent == null ) return paths;

		Node node;
		NodeList list = parent.getChildNodes();
		int count = list.getLength();
		for( int index = 0; index < count; index++ ) {
			node = list.item( index );
			if( node instanceof Text ) {
				if( TextUtil.isEmpty( node.getTextContent() ) ) continue;
				paths.add( getPath( node ) );
			}
			paths.addAll( listPaths( node ) );
		}

		return paths;
	}

	private String getPath( Node node ) {
		StringBuilder builder = new StringBuilder();
		Node parent = node.getParentNode();
		while( parent != this.node ) {
			builder.insert( 0, parent.getNodeName() );
			builder.insert( 0, "/" );
			parent = parent.getParentNode();
		}
		return builder.toString();
	}

}
