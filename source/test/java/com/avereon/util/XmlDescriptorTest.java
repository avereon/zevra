package com.avereon.util;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

class XmlDescriptorTest {

	@Test
	void testConstructor() {
		XmlDescriptor descriptor = new XmlDescriptor();
		assertThat( descriptor ).isNotNull();
		assertThat( descriptor.getPaths() ).isNotNull();
		assertThat( descriptor.getPaths().size() ).isEqualTo( 0 );
	}

	@Test
	void testConstructorWithUri() throws URISyntaxException, IOException {
		URI uri = Objects.requireNonNull( XmlUtilTest.class.getResource( "/xml.test.xml" ) ).toURI();
		XmlDescriptor descriptor = new XmlDescriptor( uri );
		assertThat( descriptor ).isNotNull();
		assertThat( descriptor.getPaths() ).isNotNull();
		assertThat( descriptor.getPaths().size() ).isEqualTo( 1 );
	}

	@Test
	void testConstructorWithUrl() throws IOException {
		URL uri = Objects.requireNonNull( XmlUtilTest.class.getResource( "/xml.test.xml" ) );
		XmlDescriptor descriptor = new XmlDescriptor( uri );
		assertThat( descriptor ).isNotNull();
		assertThat( descriptor.getPaths() ).isNotNull();
		assertThat( descriptor.getPaths().size() ).isEqualTo( 1 );
	}

	@Test
	void testConstructorWithReader() throws IOException {
		Reader reader = new InputStreamReader( Objects.requireNonNull( XmlUtilTest.class.getResourceAsStream( "/xml.test.xml" ) ), StandardCharsets.UTF_8 );
		XmlDescriptor descriptor = new XmlDescriptor( reader );
		assertThat( descriptor ).isNotNull();
		assertThat( descriptor.getPaths() ).isNotNull();
		assertThat( descriptor.getPaths().size() ).isEqualTo( 1 );
	}

	@Test
	void testConstructorWithNullNode() {
		XmlDescriptor descriptor = new XmlDescriptor( (Node)null );
		assertThat( descriptor ).isNotNull();
		assertThat( descriptor.getPaths() ).isNotNull();
		assertThat( descriptor.getPaths().size() ).isEqualTo( 0 );
	}

	@Test
	void testConstructorWithNullStream() throws Exception {
		XmlDescriptor descriptor = null;
		try {
			descriptor = new XmlDescriptor( (InputStream)null );
			fail( "XmlDescriptor constructor should not allow null streams." );
		} catch( NullPointerException exception ) {
			// Intentionally ignore exception.
		}
		assertThat( descriptor ).isNull();
	}

	@Test
	void testConstructorWithNode() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();
		XmlDescriptor descriptor2 = new XmlDescriptor( descriptor.getNode( "/test" ) );
		assertThat( descriptor2 ).isNotNull();
		assertThat( descriptor2.getValue( "name" ) ).isEqualTo( "test.name" );
	}

	@Test
	void testConstructorWithStream() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();
		assertThat( descriptor ).isNotNull();
	}

	@Test
	void testGetDocument() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();
		assertThat( descriptor.getDocument() ).isNotNull();
	}

	@Test
	void testGetPathsWithEmptyDescriptor() {
		XmlDescriptor descriptor = new XmlDescriptor();
		List<String> paths = descriptor.getPaths();
		assertThat( paths ).isNotNull();
		assertThat( paths.size() ).isEqualTo( 0 );
	}

	@Test
	void testGetValueWithEmptyDescriptor() {
		XmlDescriptor descriptor = new XmlDescriptor();
		assertThat( descriptor.getValue( null ) ).isNull();
		assertThat( descriptor.getValue( "" ) ).isNull();
		assertThat( descriptor.getValue( "test/name" ) ).isNull();
	}

	@Test
	void testGetValueWithDefaultWithEmptyDescriptor() {
		XmlDescriptor descriptor = new XmlDescriptor();
		assertThat( descriptor.getValue( (String)null, null ) ).isNull();
		assertThat( descriptor.getValue( "", null ) ).isNull();
		assertThat( descriptor.getValue( "notfound", null ) ).isNull();
		assertThat( descriptor.getValue( "test/name", null ) ).isNull();
		assertThat( descriptor.getValue( (String)null, "default" ) ).isEqualTo( "default" );
		assertThat( descriptor.getValue( "notfound", "default" ) ).isEqualTo( "default" );
	}

	@Test
	void testGetAttributeNames() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		List<String> names = descriptor.getAttributeNames( "/test/bounds" );
		assertThat( names.size() ).isEqualTo( 4 );
		assertThat( names.contains( "x" ) ).isTrue();
		assertThat( names.contains( "y" ) ).isTrue();
		assertThat( names.contains( "w" ) ).isTrue();
		assertThat( names.contains( "h" ) ).isTrue();
	}

	@Test
	void testGetNames() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		List<String> names = descriptor.getNames( "/test" );
		assertThat( names.size() ).isEqualTo( 8 );
		assertThat( names.contains( "name" ) ).isTrue();
		assertThat( names.contains( "alias" ) ).isTrue();
		assertThat( names.contains( "path" ) ).isTrue();
		assertThat( names.contains( "bounds" ) ).isTrue();
		assertThat( names.contains( "integer" ) ).isTrue();
		assertThat( names.contains( "list" ) ).isTrue();
		assertThat( names.contains( "nodes" ) ).isTrue();

		names = descriptor.getNames( "/test/list" );
		assertThat( names.contains( "one" ) ).isTrue();
		assertThat( names.contains( "two" ) ).isTrue();
		assertThat( names.contains( "three" ) ).isTrue();
	}

	@Test
	void testGetPaths() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		List<String> paths = descriptor.getPaths();

		int count = 0;
		assertThat( paths ).isNotNull();
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/name" );
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/alias" );
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/path/value" );
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/integer" );
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/nodes/node" );
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/nodes/not-this-one" );
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/nodes/node" );
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/nodes/not-this-one" );
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/nodes/node" );
		assertThat( paths.get( count++ ) ).isEqualTo( "/test/summary" );
		assertThat( paths.size() ).isEqualTo( count );
	}

	@Test
	void testGetNode() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		Document document = descriptor.getDocument();

		assertThat( descriptor.getNode( null ) ).isNull();
		assertThat( descriptor.getNode( "" ) ).isNull();
		assertThat( descriptor.getNode( "/" ) ).isEqualTo( document );
		assertThat( descriptor.getNode( "/test" ) ).isEqualTo( document.getDocumentElement() );
	}

	@Test
	void testGetNodes() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		assertThat( descriptor.getNodes( null ) ).isNull();
		assertThat( descriptor.getNodes( "" ) ).isNull();

		Node[] values = descriptor.getNodes( "/test/nodes/node" );
		assertThat( values[ 0 ].getTextContent() ).isEqualTo( "one" );
		assertThat( values[ 1 ].getTextContent() ).isEqualTo( "two" );
		assertThat( values[ 2 ].getTextContent() ).isEqualTo( "three" );
	}

	@Test
	void testGetValue() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		assertThat( descriptor.getValue( null ) ).isNull();
		assertThat( descriptor.getValue( "" ) ).isNull();
		assertThat( descriptor.getValue( "notfound" ) ).isNull();
		assertThat( descriptor.getValue( "test/name" ) ).isEqualTo( "test.name" );
		assertThat( descriptor.getValue( "test/alias" ) ).isEqualTo( "test.alias" );
		assertThat( descriptor.getValue( "test/path/value" ) ).isEqualTo( "test.path.value" );
	}

	@Test
	void testGetValueWithDefault() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		assertThat( descriptor.getValue( (String)null, null ) ).isNull();
		assertThat( descriptor.getValue( "", null ) ).isNull();
		assertThat( descriptor.getValue( "notfound", null ) ).isNull();
		assertThat( descriptor.getValue( (String)null, "default" ) ).isEqualTo( "default" );
		assertThat( descriptor.getValue( "test/name", null ) ).isEqualTo( "test.name" );
		assertThat( descriptor.getValue( "notfound", "default" ) ).isEqualTo( "default" );
	}

	@Test
	void testGetMultilineValue() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		assertThat( descriptor.getValue( "/test/summary" ) ).isEqualTo(
			"This summary needs to span multiple lines in order for the test to work correctly. Please ensure that this summary is wrapped roughly at characters per line so that there are three lines." );
	}

	@Test
	void testGetValues() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		assertThat( descriptor.getValues( null ) ).isNull();
		assertThat( descriptor.getValues( "" ) ).isNull();

		String[] values = descriptor.getValues( "/test/nodes/node" );
		assertThat( values.length ).isEqualTo( 3 );
		assertThat( values[ 0 ] ).isEqualTo( "one" );
		assertThat( values[ 1 ] ).isEqualTo( "two" );
		assertThat( values[ 2 ] ).isEqualTo( "three" );
	}

	@Test
	void testGetAttribute() throws IOException {
		XmlDescriptor descriptor = loadTestDescriptor();

		assertThat( descriptor.getValues( null ) ).isNull();
		assertThat( descriptor.getValues( "" ) ).isNull();

		assertThat( XmlDescriptor.getAttribute( descriptor.getNode( "/test/bounds" ), "x" ) ).isEqualTo( "5" );
		assertThat( XmlDescriptor.getAttribute( descriptor.getNode( "/test/bounds" ), "y" ) ).isEqualTo( "10" );
		assertThat( XmlDescriptor.getAttribute( descriptor.getNode( "/test/bounds" ), "w" ) ).isEqualTo( "20" );
		assertThat( XmlDescriptor.getAttribute( descriptor.getNode( "/test/bounds" ), "h" ) ).isEqualTo( "15" );
	}

	@Test
	void testGetMultilineValues() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();

		assertThat( descriptor.getValues( null ) ).isNull();
		assertThat( descriptor.getValues( "" ) ).isNull();

		String[] values = descriptor.getValues( "/test/summary" );
		assertThat( values.length ).isEqualTo( 1 );
		assertThat( values[ 0 ] ).isEqualTo(
			"This summary needs to span multiple lines in order for the test to work correctly. Please ensure that this summary is wrapped roughly at characters per line so that there are three lines." );
	}

	@Test
	void testGetAttributeValueFromSubDescriptor() throws Exception {
		XmlDescriptor descriptor = loadTestDescriptor();
		XmlDescriptor subDescriptor = new XmlDescriptor( descriptor.getNode( "/test/bounds" ) );

		assertThat( descriptor.getValue( "/test/bounds/@h" ) ).isEqualTo( "15" );
		assertThat( subDescriptor.getValue( "@h" ) ).isEqualTo( "15" );
	}

	private XmlDescriptor loadTestDescriptor() throws IOException {
		InputStream input = XmlDescriptorTest.class.getResourceAsStream( "/descriptor.test.xml" );
		assertThat( input ).isNotNull();
		XmlDescriptor descriptor = new XmlDescriptor( input );
		assertThat( descriptor ).isNotNull();
		return descriptor;
	}

}
