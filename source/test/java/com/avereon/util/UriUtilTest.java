package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UriUtilTest {

	@Test
	void understandUriParts() {
		// Opaque URI with scheme and fragment
		URI uri = URI.create( "program:product#updates" );
		assertThat( uri.isOpaque() ).isEqualTo( true );
		assertThat( uri.isAbsolute() ).isEqualTo( true );
		assertThat( uri.getScheme() ).isEqualTo( "program" );
		assertThat( uri.getUserInfo() ).isNull();
		assertThat( uri.getHost() ).isNull();
		assertThat( uri.getAuthority() ).isNull();
		assertThat( uri.getPath() ).isNull();
		assertThat( uri.getQuery() ).isNull();
		assertThat( uri.getFragment() ).isEqualTo( "updates" );
		assertThat( uri.getSchemeSpecificPart() ).isEqualTo( "product" );

		// Normal URI with scheme and fragment
		uri = URI.create( "https://avereon.com/download/xenon/product/card?version=latest&refresh=false#name" );
		assertThat( uri.isOpaque() ).isEqualTo( false );
		assertThat( uri.isAbsolute() ).isEqualTo( true );
		assertThat( uri.getScheme() ).isEqualTo( "https" );
		assertThat( uri.getUserInfo() ).isNull();
		assertThat( uri.getHost() ).isEqualTo( "avereon.com" );
		assertThat( uri.getAuthority() ).isEqualTo( "avereon.com" );
		assertThat( uri.getPath() ).isEqualTo( "/download/xenon/product/card" );
		assertThat( uri.getQuery() ).isEqualTo( "version=latest&refresh=false" );
		assertThat( uri.getFragment() ).isEqualTo( "name" );
		assertThat( uri.getSchemeSpecificPart() ).isEqualTo( "//avereon.com/download/xenon/product/card?version=latest&refresh=false" );

		// Normal URI with user info
		uri = URI.create( "ssh://user@avereon.com/tmp" );
		assertThat( uri.isOpaque() ).isEqualTo( false );
		assertThat( uri.isAbsolute() ).isEqualTo( true );
		assertThat( uri.getScheme() ).isEqualTo( "ssh" );
		assertThat( uri.getUserInfo() ).isEqualTo( "user" );
		assertThat( uri.getHost() ).isEqualTo( "avereon.com" );
		assertThat( uri.getAuthority() ).isEqualTo( "user@avereon.com" );
		assertThat( uri.getPath() ).isEqualTo( "/tmp" );
		assertThat( uri.getQuery() ).isNull();
		assertThat( uri.getFragment() ).isNull();
		assertThat( uri.getSchemeSpecificPart() ).isEqualTo( "//user@avereon.com/tmp" );

		// Files with spaces
		Path path = Path.of( "/home/user/My Documents" );
		uri = path.toUri();
		assertThat( uri.isOpaque() ).isEqualTo( false );
		assertThat( uri.isAbsolute() ).isEqualTo( true );
		assertThat( uri.getScheme() ).isEqualTo( "file" );
		assertThat( uri.getUserInfo() ).isNull();
		assertThat( uri.getHost() ).isNull();
		assertThat( uri.getAuthority() ).isNull();
		assertThat( uri.getPath() ).isEqualTo( "/home/user/My Documents" );
		assertThat( uri.getRawPath() ).isEqualTo( "/home/user/My%20Documents" );
		assertThat( uri.getQuery() ).isNull();
		assertThat( uri.getFragment() ).isNull();
		assertThat( uri.getSchemeSpecificPart() ).isEqualTo( "///home/user/My Documents" );
		assertThat( uri.getRawSchemeSpecificPart() ).isEqualTo( "///home/user/My%20Documents" );
		assertThat( uri.toString() ).isEqualTo( "file:///home/user/My%20Documents" );
		assertThat( uri.toASCIIString() ).isEqualTo( "file:///home/user/My%20Documents" );
		// converted back to path
		assertThat( Paths.get( uri ).toString() ).isEqualTo( "/home/user/My Documents" );

		URI a = URI.create( "program:help" );
		URI b = URI.create( "program:help#detail" );
		assertThat( a.compareTo( b ) ).isLessThan( 0 );

		// Opaque URI with scheme and scheme specific part
		assertThat( b.getScheme() ).isNotEqualTo( "program:help" );
		assertThat( b.getScheme() ).isEqualTo( "program" );
		assertThat( b.getSchemeSpecificPart() ).isEqualTo( "help" );

		// Name only URI
		URI c = URI.create( "help" );
		assertThat( c.getScheme() ).isNotEqualTo( "help" );
		assertThat( c.getPath() ).isEqualTo( "help" );
		assertThat( b.getSchemeSpecificPart() ).isEqualTo( "help" );
	}

	@Test
	void testAddToPath() {
		assertThat( UriUtil.addToPath( URI.create( "https://host:74/path/to?parm1=a&parm2=b" ), "resource" ) ).isEqualTo( URI.create( "https://host:74/path/to/resource?parm1=a&parm2=b" ) );
		assertThat( UriUtil.addToPath( URI.create( "/path/to" ), "resource" ) ).isEqualTo( URI.create( "/path/to/resource" ) );
		assertThat( UriUtil.addToPath( URI.create( "/path/of" ), "../to/resource" ) ).isEqualTo( URI.create( "/path/to/resource" ) );
	}

	@Test
	void testParseName() {
		assertThat( UriUtil.parseName( URI.create( "file:///C:/" ) ) ).isEqualTo( "C:" );
		assertThat( UriUtil.parseName( URI.create( "https://host:74" ) ) ).isEqualTo( "/" );
		assertThat( UriUtil.parseName( URI.create( "https://host:74/" ) ) ).isEqualTo( "/" );
		assertThat( UriUtil.parseName( URI.create( "https://host:74/path/to/resource" ) ) ).isEqualTo( "resource" );
	}

	@Test
	void testRemoveFragment() {
		assertThat( UriUtil.removeFragment( URI.create( "program:product#update" ) ) ).isEqualTo( URI.create( "program:product" ) );
		assertThat( UriUtil.removeFragment( URI.create( "https://absolute/path?query" ) ) ).isEqualTo( URI.create( "https://absolute/path?query" ) );
		assertThat( UriUtil.removeFragment( URI.create( "/absolute/path?query#fragment" ) ) ).isEqualTo( URI.create( "/absolute/path?query" ) );
		assertThat( UriUtil.removeFragment( URI.create( "relative/path?query#fragment" ) ) ).isEqualTo( URI.create( "relative/path?query" ) );
		assertThat( UriUtil.removeFragment( URI.create( "https://host:74/path/to/resource?parm1=a&parm2=b" ) ) ).isEqualTo( URI.create( "https://host:74/path/to/resource?parm1=a&parm2=b" ) );
	}

	@Test
	void testRemoveQueryAndFragment() {
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "program:product#update" ) ) ).isEqualTo( URI.create( "program:product" ) );
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "https://absolute/path?query" ) ) ).isEqualTo( URI.create( "https://absolute/path" ) );
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "/absolute/path?query#fragment" ) ) ).isEqualTo( URI.create( "/absolute/path" ) );
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "relative/path?query#fragment" ) ) ).isEqualTo( URI.create( "relative/path" ) );
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "https://host:74/path/to/resource?parm1=a&parm2=b" ) ) ).isEqualTo( URI.create( "https://host:74/path/to/resource" ) );
	}

	@Test
	void testResolveWithString() throws Exception {
		assertThat( UriUtil.resolve( "" ) ).isEqualTo( new File( "" ).getCanonicalFile().toURI() );
		assertThat( UriUtil.resolve( "." ) ).isEqualTo( new File( "." ).getCanonicalFile().toURI() );
		assertThat( UriUtil.resolve( "test" ) ).isEqualTo( new File( "test" ).getCanonicalFile().toURI() );
		assertThat( UriUtil.resolve( "/test" ) ).isEqualTo( new File( "/test" ).getCanonicalFile().toURI() );
		assertThat( UriUtil.resolve( "ssh://localhost" ) ).isEqualTo( URI.create( "ssh://localhost" ) );
	}

	@Test
	void testResolveWithRelativeUri() {
		URI base = URI.create( "file:/test/folder/" );
		URI absolute = URI.create( "file:/test/folder/absolute" );
		URI relative = URI.create( "relative" );
		URI jar = URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/resource" );

		assertThat( UriUtil.resolve( null, (URI)null ) ).isNull();
		assertThat( UriUtil.resolve( base, (URI)null ) ).isNull();
		assertThat( UriUtil.resolve( null, relative ) ).isEqualTo( URI.create( "relative" ) );
		assertThat( UriUtil.resolve( null, absolute ) ).isEqualTo( URI.create( "file:/test/folder/absolute" ) );

		assertThat( UriUtil.resolve( base, absolute ) ).isEqualTo( URI.create( "file:/test/folder/absolute" ) );
		assertThat( UriUtil.resolve( base, relative ) ).isEqualTo( URI.create( "file:/test/folder/relative" ) );
		assertThat( UriUtil.resolve( absolute, relative ) ).isEqualTo( URI.create( "file:/test/folder/relative" ) );

		assertThat( UriUtil.resolve( jar, relative ) ).isEqualTo( URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/relative" ) );
	}

	@Test
	void testResolveWithAbsoluteUri() {
		URI jar = URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/resource" );
		URI icon = URI.create( "https://www.parallelsymmetry.com/images/icons/escape.png" );
		assertThat( UriUtil.resolve( jar, icon ) ).isEqualTo( URI.create( "https://www.parallelsymmetry.com/images/icons/escape.png" ) );
	}

	@Test
	void testGetParent() {
		URI absolute = URI.create( "file:/test/folder/absolute" );
		URI opaque = URI.create( "jar:" + absolute );
		URI doubleOpaque = URI.create( "double:jar:" + absolute );

		assertThat( UriUtil.getParent( absolute ).toString() ).isEqualTo( "file:/test/folder/" );
		assertThat( UriUtil.getParent( opaque ).toString() ).isEqualTo( "jar:file:/test/folder/" );
		assertThat( UriUtil.getParent( doubleOpaque ).toString() ).isEqualTo( "double:jar:file:/test/folder/" );
	}

	@Test
	void testGetParentWithFolder() {
		URI absolute = URI.create( "file:/test/folder/" );
		URI opaque = URI.create( "jar:" + absolute );
		URI doubleOpaque = URI.create( "double:jar:" + absolute );

		assertThat( UriUtil.getParent( absolute ).toString() ).isEqualTo( "file:/test/" );
		assertThat( UriUtil.getParent( opaque ).toString() ).isEqualTo( "jar:file:/test/" );
		assertThat( UriUtil.getParent( doubleOpaque ).toString() ).isEqualTo( "double:jar:file:/test/" );
	}

	@Test
	void testIsRoot() {
		assertThat( UriUtil.isRoot( URI.create( "/" ) ) ).isTrue();
		assertThat( UriUtil.isRoot( URI.create( "file:/" ) ) ).isTrue();

		assertThat( UriUtil.isRoot( URI.create( "" ) ) ).isFalse();
		assertThat( UriUtil.isRoot( URI.create( "/test" ) ) ).isFalse();
		assertThat( UriUtil.isRoot( URI.create( "/test/" ) ) ).isFalse();
		assertThat( UriUtil.isRoot( URI.create( "file:/test" ) ) ).isFalse();
		assertThat( UriUtil.isRoot( URI.create( "file:/test/" ) ) ).isFalse();
	}

	@Test
	void testHasParent() {
		assertThat( UriUtil.hasParent( URI.create( "/" ) ) ).isFalse();
		assertThat( UriUtil.hasParent( URI.create( "file:/" ) ) ).isFalse();

		assertThat( UriUtil.hasParent( URI.create( "" ) ) ).isFalse();
		assertThat( UriUtil.hasParent( URI.create( "/../../.." ) ) ).isFalse();

		assertThat( UriUtil.hasParent( URI.create( "/test" ) ) ).isTrue();
		assertThat( UriUtil.hasParent( URI.create( "/test/" ) ) ).isTrue();
		assertThat( UriUtil.hasParent( URI.create( "file:/test" ) ) ).isTrue();
		assertThat( UriUtil.hasParent( URI.create( "file:/test/" ) ) ).isTrue();
	}

	@Test
	void testGetParentWithRootUri() {
		assertThat( UriUtil.getParent( URI.create( "/" ) ).toString() ).isEqualTo( "/" );
		assertThat( UriUtil.getParent( URI.create( "file:/" ) ).toString() ).isEqualTo( "file:/" );
	}

	@Test
	void testParseFragmentWithUri() {
		assertThat( UriUtil.parseFragment( null ) ).isNull();

		URI uri = URI.create( "test:///path#fragment" );
		assertThat( UriUtil.parseFragment( uri ) ).isEqualTo( "fragment" );

		// Query parameters after the fragment are part of the fragment
		uri = URI.create( "test:///path#fragment?attr1&attr2" );
		assertThat( UriUtil.parseFragment( uri ) ).isEqualTo( "fragment?attr1&attr2" );
	}

	@Test
	void testParseQueryWithUri() {
		assertThat( UriUtil.parseQuery( (URI)null ) ).isNull();

		URI uri = URI.create( "test:///path?attr1&attr2" );
		Map<String, String> parameters = UriUtil.parseQuery( uri.getQuery() );
		assertThat( parameters.get( "attr1" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "attr2" ) ).isEqualTo( "true" );

		uri = URI.create( "test:///path?attr1=value1&attr2=value2" );
		parameters = UriUtil.parseQuery( uri.getQuery() );
		assertThat( parameters.get( "attr1" ) ).isEqualTo( "value1" );
		assertThat( parameters.get( "attr2" ) ).isEqualTo( "value2" );

		uri = URI.create( "test:///path?attr1=value1&attr2=value2#fragment" );
		parameters = UriUtil.parseQuery( uri.getQuery() );
		assertThat( UriUtil.parseFragment( uri ) ).isEqualTo( "fragment" );
		assertThat( parameters.get( "attr1" ) ).isEqualTo( "value1" );
		assertThat( parameters.get( "attr2" ) ).isEqualTo( "value2" );
	}

	@Test
	void testParseQueryWithString() {
		assertThat( UriUtil.parseQuery( (String)null ) ).isEqualTo( Map.of() );

		Map<String, String> parameters = UriUtil.parseQuery( "attr1&attr2" );
		assertThat( parameters.get( "attr1" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "attr2" ) ).isEqualTo( "true" );

		parameters = UriUtil.parseQuery( "attr1=value1&attr2=value2" );
		assertThat( parameters.get( "attr1" ) ).isEqualTo( "value1" );
		assertThat( parameters.get( "attr2" ) ).isEqualTo( "value2" );
	}

	@Test
	void testGetUriPartsWithOpaqueUri() {
		assertThat( UriUtil.getParts( URI.create( "program:about#details" ) ) ).contains( "program", "about", "details" );
	}

	@Test
	void testGetUriPartsWithHierarchicalUri() {
		assertThat( UriUtil.getParts( URI.create( "https://avereon.com/download/razor/product/card#latest" ) ) ).contains( "https", "avereon.com", "", "download", "razor", "product", "card", "latest" );
		assertThat( UriUtil.getParts( URI.create( "/" ) ).size() ).isEqualTo( 2 );
		assertThat( UriUtil.getParts( URI.create( "" ) ) ).contains( "" );
	}

	@Test
	void testGetUriPartsWithRelativeUri() {
		assertThat( UriUtil.getParts( URI.create( "/download/razor/product" ) ) ).contains( "", "download", "razor", "product" );
	}

	@Test
	void testGetUriMatchScoreWithOpaqueUri() {
		URI a = URI.create( "program:about" );
		URI b = URI.create( "program:about#details" );
		URI c = URI.create( "program:settings" );
		URI d = URI.create( "program:settings#general" );

		assertThat( UriUtil.getMatchScore( a, a ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( b, b ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( c, c ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( d, d ) ).isEqualTo( 0 );

		assertThat( UriUtil.getMatchScore( a, b ) ).isEqualTo( 1 );
		assertThat( UriUtil.getMatchScore( b, a ) ).isEqualTo( 1 );
		assertThat( UriUtil.getMatchScore( a, c ) ).isEqualTo( 1 );
		assertThat( UriUtil.getMatchScore( c, a ) ).isEqualTo( 1 );

		assertThat( UriUtil.getMatchScore( a, d ) ).isEqualTo( 2 );
		assertThat( UriUtil.getMatchScore( d, a ) ).isEqualTo( 2 );
		assertThat( UriUtil.getMatchScore( b, d ) ).isEqualTo( 2 );
		assertThat( UriUtil.getMatchScore( d, b ) ).isEqualTo( 2 );
	}

	@Test
	void testGetUriMatchScoreWithHierarchicalUri() {
		URI a = URI.create( "" );
		URI b = URI.create( "/" );
		URI c = URI.create( "ssh://user@sshhost.com" );
		URI d = URI.create( "https://avereon.com/download/xenon" );
		URI e = URI.create( "https://avereon.com/download/xenon/product/card" );
		URI f = URI.create( "https://avereon.com/download/xenon/product/card#latest" );

		// Same matches
		assertThat( UriUtil.getMatchScore( a, a ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( b, b ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( c, c ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( d, d ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( e, e ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( f, f ) ).isEqualTo( 0 );

		// Empty and root checks
		assertThat( UriUtil.getMatchScore( a, c ) ).isEqualTo( 4 );
		assertThat( UriUtil.getMatchScore( a, d ) ).isEqualTo( 5 );
		assertThat( UriUtil.getMatchScore( a, e ) ).isEqualTo( 7 );
		assertThat( UriUtil.getMatchScore( a, f ) ).isEqualTo( 8 );

		assertThat( UriUtil.getMatchScore( b, c ) ).isEqualTo( 4 );
		assertThat( UriUtil.getMatchScore( b, d ) ).isEqualTo( 5 );
		assertThat( UriUtil.getMatchScore( b, e ) ).isEqualTo( 7 );
		assertThat( UriUtil.getMatchScore( b, f ) ).isEqualTo( 8 );

		// Different scheme
		assertThat( UriUtil.getMatchScore( c, d ) ).isEqualTo( 5 );
		assertThat( UriUtil.getMatchScore( d, c ) ).isEqualTo( 5 );

		// Different path
		assertThat( UriUtil.getMatchScore( d, e ) ).isEqualTo( 2 );
		assertThat( UriUtil.getMatchScore( d, f ) ).isEqualTo( 3 );
		assertThat( UriUtil.getMatchScore( e, f ) ).isEqualTo( 1 );
	}

	@Test
	void testGetUriMatchScoreWithPathUri() {
		URI a = URI.create( "" );
		URI b = URI.create( "/" );
		URI c = URI.create( "/root" );
		URI d = URI.create( "/root/of/the/tree" );
		URI e = URI.create( "root" );
		URI f = URI.create( "root/of/the/tree" );
		URI g = URI.create( "a/branch" );
		URI h = URI.create( "a/branch/of/the/tree" );

		// Same matches
		assertThat( UriUtil.getMatchScore( a, a ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( b, b ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( c, c ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( d, d ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( e, e ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( f, f ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( g, g ) ).isEqualTo( 0 );
		assertThat( UriUtil.getMatchScore( h, h ) ).isEqualTo( 0 );

		// Empty and root checks
		assertThat( UriUtil.getMatchScore( a, b ) ).isEqualTo( 1 );
		assertThat( UriUtil.getMatchScore( a, c ) ).isEqualTo( 1 );
		assertThat( UriUtil.getMatchScore( a, d ) ).isEqualTo( 4 );
		assertThat( UriUtil.getMatchScore( a, f ) ).isEqualTo( 4 );

		assertThat( UriUtil.getMatchScore( b, a ) ).isEqualTo( 1 );
		assertThat( UriUtil.getMatchScore( b, c ) ).isEqualTo( 1 );
		assertThat( UriUtil.getMatchScore( b, d ) ).isEqualTo( 4 );
		assertThat( UriUtil.getMatchScore( b, f ) ).isEqualTo( 4 );

		// Absolute path checks
		assertThat( UriUtil.getMatchScore( c, d ) ).isEqualTo( 3 );
		assertThat( UriUtil.getMatchScore( c, e ) ).isEqualTo( 2 );
		assertThat( UriUtil.getMatchScore( c, f ) ).isEqualTo( 4 );

		assertThat( UriUtil.getMatchScore( d, c ) ).isEqualTo( 3 );
		assertThat( UriUtil.getMatchScore( d, e ) ).isEqualTo( 5 );
		assertThat( UriUtil.getMatchScore( d, f ) ).isEqualTo( 5 );

		// Relative path checks
		assertThat( UriUtil.getMatchScore( e, f ) ).isEqualTo( 3 );
		assertThat( UriUtil.getMatchScore( e, g ) ).isEqualTo( 2 );
		assertThat( UriUtil.getMatchScore( e, h ) ).isEqualTo( 5 );

		assertThat( UriUtil.getMatchScore( f, e ) ).isEqualTo( 3 );
		assertThat( UriUtil.getMatchScore( f, g ) ).isEqualTo( 4 );
		assertThat( UriUtil.getMatchScore( f, h ) ).isEqualTo( 5 );
	}

}
