package com.avereon.util;

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

/**
 * A convenience class to simplify the use of XML documents as descriptors. This
 * class makes accessing simpler by treating all values as paths in the XML
 * document where the path elements are nodes in the XML DOM tree. For example,
 * given the XML document
 * <pre>
 * &lt;config&gt;
 *   &lt;threads&gt;
 *     &lt;min&gt;2&lt;/min&gt;
 *     &lt;max&gt;8&lt;/max&gt;
 *   &lt;/threads&gt;
 * &lt;/config&gt;</pre>
 * calling getValue("/config/threads/max") would return "8".
 * <p/>
 * Other methods are provided for creating descriptors, getting attribute
 * values and getting other information from the descriptor.
 */
public class XmlDescriptor {

	private static final System.Logger log = Log.get();

	private Node node;

	private Map<String, List<String>> attrNames = new ConcurrentHashMap<>();

	private Map<String, List<String>> names = new ConcurrentHashMap<>();

	private List<String> paths;

	/**
	 * Create an empty descriptor.
	 */
	public XmlDescriptor() {}

	/**
	 * Load an XML descriptor from a URI.
	 *
	 * @param uri The URI of the XML content
	 * @throws IOException If an I/O error occurs
	 */
	public XmlDescriptor( URI uri ) throws IOException {
		if( uri == null ) return;
		try {
			node = XmlUtil.loadXmlDocument( uri.toString() );
		} catch( SAXException exception ) {
			throw new IOException( exception );
		}
	}

	/**
	 * Load an XML descriptor from a URL.
	 *
	 * @param url The URL of the XML content
	 * @throws IOException If an I/O error occurs
	 */
	public XmlDescriptor( URL url ) throws IOException {
		if( url == null ) return;
		try {
			node = XmlUtil.loadXmlDocument( url.toString() );
		} catch( SAXException exception ) {
			throw new IOException( exception );
		}
	}

	/**
	 * Load an XML descriptor from a Reader.
	 *
	 * @param reader The Reader from which to load the XML content
	 * @throws IOException If an I/O error occurs
	 */
	public XmlDescriptor( Reader reader ) throws IOException {
		if( reader == null ) return;
		try {
			node = XmlUtil.loadXmlDocument( reader );
		} catch( SAXException exception ) {
			throw new IOException( exception );
		}
	}

	/**
	 * Load an XML descriptor from an InputStream.
	 *
	 * @param input The InputStream from which to load the XML content
	 * @throws IOException If an I/O error occurs
	 */
	public XmlDescriptor( InputStream input ) throws IOException {
		if( input == null ) throw new NullPointerException( "Input stream cannot be null." );
		try {
			node = XmlUtil.loadXmlDocument( input );
		} catch( SAXException exception ) {
			throw new IOException( exception );
		}
	}

	/**
	 * Create a descriptor from an existing XML node, which includes XML documents.
	 *
	 * @param node The XML node to use as the root of the descriptor
	 */
	public XmlDescriptor( Node node ) {
		if( node == null ) return;
		this.node = node;
	}

	/**
	 * Get the parent XML document of this descriptor if this descriptor was
	 * created from a node. If the descriptor node is the parent document then it
	 * is returned.
	 */
	public Document getDocument() {
		return (node instanceof Document) ? (Document)node : node.getOwnerDocument();
	}

	/**
	 * Get the XML node that is the root of this descriptor.
	 *
	 * @return The root XML node of the descriptor
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * Get the attribute names for the node at the specified path. For example,
	 * give the following XML document:
	 * <pre>
	 * &lt;config&gt;
	 *   &lt;threads min=&quot;2&quot; max=&quot;8&quot;/&gt;
	 * &lt;/config&gt;</pre>
	 * calling <code>getAttributeNames("/config/threads")</code> returns a
	 * List&lt;String&gt; with "min" and "max".
	 *
	 * @param path The path of the node for which to retrieve attribute names
	 * @return The list of attribute names for the node
	 */
	public List<String> getAttributeNames( String path ) {
		List<String> names = this.attrNames.get( path );
		if( names == null ) {
			names = listAttributeNames( getNode( path ) );
			this.attrNames.put( path, names );
		}
		return names;
	}

	/**
	 * Get the unique names of child nodes of the node at the specified path. For
	 * example, give the XML document:
	 * <pre>
	 * &lt;config&gt;
	 *   &lt;threads&gt;
	 *     &lt;min&gt;2&lt;/min&gt;
	 *     &lt;max&gt;8&lt;/max&gt;
	 *   &lt;/threads&gt;
	 * &lt;/config&gt;</pre>
	 * calling <code>getAttributeNames("/config/threads")</code> returns a
	 * List&lt;String&gt; with "min" and "max".
	 *
	 * @param path The path of the node for which to retrieve child node names
	 * @return The list of child node names for the node
	 */
	public List<String> getNames( String path ) {
		List<String> names = this.names.get( path );
		if( names == null ) {
			names = listNames( getNode( path ) );
			this.names.put( path, names );
		}
		return names;
	}

	/**
	 * Get the paths of non-empty values for the entire descriptor. For example,
	 * give the XML document
	 * <pre>
	 * &lt;config&gt;
	 *   &lt;threads&gt;
	 *     &lt;min/min&gt;
	 *     &lt;max&gt;8&lt;/max&gt;
	 *   &lt;/threads&gt;
	 * &lt;/config&gt;</pre>
	 * calling <code>getPaths()</code> returns a List&lt;String&gt; with only
	 * "/config/thread/max". Because the path "/config/thread/min" does not have
	 * a non-empty value it is not returned in the list.
	 *
	 * @return The list of paths with non-empty values
	 */
	public List<String> getPaths() {
		if( paths == null ) paths = listPaths( node );
		return paths;
	}

	/**
	 * Get the first XML child node with the specified path in this descriptor.
	 *
	 * @param path The path of the child node
	 * @return The XML child node
	 */
	public Node getNode( String path ) {
		return getNode( this.node, path );
	}

	/**
	 * Get all the XML child nodes with the specified path in this descriptor.
	 *
	 * @param path The path of the XML child nodes
	 * @return An array of the XML child nodes
	 */
	public Node[] getNodes( String path ) {
		return getNodes( this.node, path );
	}

	/**
	 * Get the text value of the XML node at the specified path. For example,
	 * given the XML document
	 * <pre>
	 * &lt;config&gt;
	 *   &lt;threads&gt;
	 *     &lt;min&gt;2&lt;/min&gt;
	 *     &lt;max&gt;8&lt;/max&gt;
	 *   &lt;/threads&gt;
	 * &lt;/config&gt;</pre>
	 * calling &lt;getValue("/config/threads/max")&gt; would return "8".
	 *
	 * @param path The path of the value
	 * @return The string value
	 */
	public String getValue( String path ) {
		return getValue( this.node, path );
	}

	/**
	 * Get the text value of the XML node at the specified path. If the value is
	 * empty then return the default value. For example, given the XML document
	 * <pre>
	 * &lt;config&gt;
	 *   &lt;threads&gt;
	 *     &lt;min&gt;&lt;/min&gt;
	 *     &lt;max&gt;8&lt;/max&gt;
	 *   &lt;/threads&gt;
	 * &lt;/config&gt;</pre>
	 * calling &lt;getValue("/config/threads/min", "3" )&gt; would return "3" but
	 * calling &lt;getValue("/config/threads/max", "5" )&gt; would return "8".
	 *
	 * @param path The path of the value
	 * @return The string value
	 */
	public String getValue( String path, String defaultValue ) {
		return getValue( this.node, path, defaultValue );
	}

	/**
	 * Get an array of all the values that have the same path.
	 *
	 * @param path The path of the values
	 * @return An array of values with the same path.
	 */
	public String[] getValues( String path ) {
		return getValues( this.node, path );
	}

	/**
	 * Return formatted XML, using the descriptor node as the root, as returned
	 * by {@link com.avereon.util.XmlUtil#toString(Node)}
	 *
	 * @return The formatted XML string
	 */
	@Override
	public String toString() {
		return XmlUtil.toString( node );
	}

	/**
	 * Using the specified node as the root, return the node at the give path.
	 *
	 * @param node The node to use as the root node
	 * @param path The node path
	 * @return The node at the specified path
	 */
	public static Node getNode( Node node, String path ) {
		if( node == null || TextUtil.isEmpty( path ) ) return null;

		Node value = null;
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			value = (Node)xpath.evaluate( path, node, XPathConstants.NODE );
		} catch( XPathExpressionException exception ) {
			log.log( Log.ERROR, "Error evaluating xpath: " + path, new Exception( path, exception ) );
		}

		return value;
	}

	/**
	 * Using the specified node as the root, return the nodes at the give path.
	 *
	 * @param node The node to use as the root node
	 * @param path The node path
	 * @return The nodes at the specified path
	 */
	public static Node[] getNodes( Node node, String path ) {
		if( node == null || TextUtil.isEmpty( path ) ) return null;

		NodeList nodes = null;
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			nodes = (NodeList)xpath.evaluate( path, node, XPathConstants.NODESET );
		} catch( XPathExpressionException exception ) {
			log.log( Log.ERROR, "Error evaluating xpath: " + path, new Exception( path, exception ) );
		}
		if( nodes == null ) return null;

		ArrayList<Node> values = new ArrayList<>();
		int count = nodes.getLength();
		for( int index = 0; index < count; index++ ) {
			values.add( nodes.item( index ) );
		}

		return values.toArray( new Node[ values.size() ] );
	}

	/**
	 * Using the specified node as the root, get the text value of the XML node at
	 * the specified path.
	 *
	 * @param node The node to use as the root node
	 * @param path The value path
	 * @return The value at the specified path
	 */
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

	/**
	 * Using the specified node as the root, get the text value of the XML node at
	 * the specified path. If the value is empty then use the default value.
	 *
	 * @param node The node to use as the root node
	 * @param path The value path
	 * @return The value at the specified path
	 */
	public static String getValue( Node node, String path, String defaultValue ) {
		String value = getValue( node, path );
		if( value == null ) return defaultValue;
		return value;
	}

	/**
	 * Get an array of all the values in the node that have the same path.
	 *
	 * @param node The node from which to get values
	 * @param path The xpath of the values
	 * @return An array of values with the same path.
	 */
	public static String[] getValues( Node node, String path ) {
		if( node == null || TextUtil.isEmpty( path ) ) return null;

		NodeList nodes = null;
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			nodes = (NodeList)xpath.evaluate( path, node, XPathConstants.NODESET );
		} catch( XPathExpressionException exception ) {
			log.log( Log.ERROR, "Error evaluating xpath: " + path, new Exception( path, exception ) );
		}
		if( nodes == null ) return null;

		ArrayList<String> values = new ArrayList<>();
		int count = nodes.getLength();
		for( int index = 0; index < count; index++ ) {
			try {
				values.add( (String)xpath.evaluate( "normalize-space()", nodes.item( index ), XPathConstants.STRING ) );
			} catch( XPathExpressionException exception ) {
				log.log( Log.ERROR, "Error evaluating xpath: " + path, new Exception( path, exception ) );
			}
		}

		return values.toArray( new String[ 0 ] );
	}

	/**
	 * Using the specified XML node, get the text value of the attribute with the
	 * specified name.
	 *
	 * @param node The node
	 * @param name The attribute name
	 * @return The attribute value
	 */
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
