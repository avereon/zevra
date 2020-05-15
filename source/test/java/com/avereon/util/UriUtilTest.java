package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class UriUtilTest {

	@Test
	void understandUriParts() {
		URI uri = URI.create( "program:product#updates" );
		assertThat( uri.isOpaque(), is( true ) );
		assertThat( uri.isAbsolute(), is( true ) );
		assertThat( uri.getScheme(), is( "program" ) );
		assertThat( uri.getUserInfo(), is( nullValue() ) );
		assertThat( uri.getHost(), is( nullValue() ) );
		assertThat( uri.getAuthority(), is( nullValue() ) );
		assertThat( uri.getPath(), is( nullValue() ) );
		assertThat( uri.getQuery(), is( nullValue() ) );
		assertThat( uri.getFragment(), is( "updates" ) );
		assertThat( uri.getSchemeSpecificPart(), is( "product" ) );

		uri = URI.create( "http://avereon.com/download/xenon/product/card?version=latest&refresh=false#name" );
		assertThat( uri.isOpaque(), is( false ) );
		assertThat( uri.isAbsolute(), is( true ) );
		assertThat( uri.getScheme(), is( "http" ) );
		assertThat( uri.getUserInfo(), is( nullValue() ) );
		assertThat( uri.getHost(), is( "avereon.com" ) );
		assertThat( uri.getAuthority(), is( "avereon.com" ) );
		assertThat( uri.getPath(), is( "/download/xenon/product/card" ) );
		assertThat( uri.getQuery(), is( "version=latest&refresh=false" ) );
		assertThat( uri.getFragment(), is( "name" ) );
		assertThat( uri.getSchemeSpecificPart(), is( "//avereon.com/download/xenon/product/card?version=latest&refresh=false" ) );

		uri = URI.create( "ssh://user@avereon.com/tmp" );
		assertThat( uri.isOpaque(), is( false ) );
		assertThat( uri.isAbsolute(), is( true ) );
		assertThat( uri.getScheme(), is( "ssh" ) );
		assertThat( uri.getUserInfo(), is( "user" ) );
		assertThat( uri.getHost(), is( "avereon.com" ) );
		assertThat( uri.getAuthority(), is( "user@avereon.com" ) );
		assertThat( uri.getPath(), is( "/tmp" ) );
		assertThat( uri.getQuery(), is( nullValue() ) );
		assertThat( uri.getFragment(), is( nullValue() ) );
		assertThat( uri.getSchemeSpecificPart(), is( "//user@avereon.com/tmp" ) );

		URI a = URI.create( "program:about" );
		URI b = URI.create( "program:about#detail" );
		assertThat( a.compareTo( b ), is( lessThan( 0 ) ) );
	}

	@Test
	void testAddToPath() {
		assertThat(
			UriUtil.addToPath( URI.create( "https://host:74/path/to?parm1=a&parm2=b" ), "resource" ),
			is( URI.create( "https://host:74/path/to/resource?parm1=a&parm2=b" ) )
		);
		assertThat( UriUtil.addToPath( URI.create( "/path/to" ), "resource" ), is( URI.create( "/path/to/resource" ) ) );
		assertThat( UriUtil.addToPath( URI.create( "/path/of" ), "../to/resource" ), is( URI.create( "/path/to/resource" ) ) );
	}

	@Test
	void testParseName() {
		assertThat( UriUtil.parseName( URI.create( "file:///C:/" ) ), is( "C:" ) );
		assertThat( UriUtil.parseName( URI.create( "https://host:74" ) ), is( "/" ) );
		assertThat( UriUtil.parseName( URI.create( "https://host:74/" ) ), is( "/" ) );
		assertThat( UriUtil.parseName( URI.create( "https://host:74/path/to/resource" ) ), is( "resource" ) );
	}

	@Test
	void testRemoveQueryAndFragment() {
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "program:product#update" ) ), is( URI.create( "program:product" ) ) );
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "https://absolute/path?query" ) ), is( URI.create( "https://absolute/path" ) ) );
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "/absolute/path?query#fragment" ) ), is( URI.create( "/absolute/path" ) ) );
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "relative/path?query#fragment" ) ), is( URI.create( "relative/path" ) ) );
		assertThat( UriUtil.removeQueryAndFragment( URI.create( "https://host:74/path/to/resource?parm1=a&parm2=b" ) ), is( URI.create( "https://host:74/path/to/resource" ) ) );
	}

	@Test
	void testResolveWithString() throws Exception {
		assertThat( UriUtil.resolve( "" ), is( new File( "" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "." ), is( new File( "." ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "test" ), is( new File( "test" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "/test" ), is( new File( "/test" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "ssh://localhost" ), is( URI.create( "ssh://localhost" ) ) );
	}

	@Test
	void testResolveWithRelativeUri() {
		URI base = URI.create( "file:/test/folder/" );
		URI absolute = URI.create( "file:/test/folder/absolute" );
		URI relative = URI.create( "relative" );
		URI jar = URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/resource" );

		assertThat( UriUtil.resolve( null, null ), is( nullValue() ) );
		assertThat( UriUtil.resolve( base, null ), is( nullValue() ) );
		assertThat( UriUtil.resolve( null, relative ), is( URI.create( "relative" ) ) );
		assertThat( UriUtil.resolve( null, absolute ), is( URI.create( "file:/test/folder/absolute" ) ) );

		assertThat( UriUtil.resolve( base, absolute ), is( URI.create( "file:/test/folder/absolute" ) ) );
		assertThat( UriUtil.resolve( base, relative ), is( URI.create( "file:/test/folder/relative" ) ) );
		assertThat( UriUtil.resolve( absolute, relative ), is( URI.create( "file:/test/folder/relative" ) ) );

		assertThat( UriUtil.resolve( jar, relative ), is( URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/relative" ) ) );
	}

	@Test
	void testResolveWithAbsoluteUri() {
		URI jar = URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/resource" );
		URI icon = URI.create( "http://www.parallelsymmetry.com/images/icons/escape.png" );
		assertThat( UriUtil.resolve( jar, icon ), is( URI.create( "http://www.parallelsymmetry.com/images/icons/escape.png" ) ) );
	}

	@Test
	void testGetParent() {
		URI absolute = URI.create( "file:/test/folder/absolute" );
		URI opaque = URI.create( "jar:" + absolute.toString() );
		URI doubleOpaque = URI.create( "double:jar:" + absolute.toString() );

		assertThat( UriUtil.getParent( absolute ).toString(), is( "file:/test/folder/" ) );
		assertThat( UriUtil.getParent( opaque ).toString(), is( "jar:file:/test/folder/" ) );
		assertThat( UriUtil.getParent( doubleOpaque ).toString(), is( "double:jar:file:/test/folder/" ) );
	}

	@Test
	void testParseQueryWithUri() {
		assertThat( UriUtil.parseQuery( (URI)null ), is( nullValue() ) );

		URI uri = URI.create( "test:///path?attr1&attr2" );
		Map<String, String> parameters = UriUtil.parseQuery( uri );
		assertThat( parameters.get( "attr1" ), is( "true" ) );
		assertThat( parameters.get( "attr2" ), is( "true" ) );

		uri = URI.create( "test:///path?attr1=value1&attr2=value2" );
		parameters = UriUtil.parseQuery( uri );
		assertThat( parameters.get( "attr1" ), is( "value1" ) );
		assertThat( parameters.get( "attr2" ), is( "value2" ) );
	}

	@Test
	void testParseQueryWithString() {
		assertThat( UriUtil.parseQuery( (String)null ), is( nullValue() ) );

		Map<String, String> parameters = UriUtil.parseQuery( "attr1&attr2" );
		assertThat( parameters.get( "attr1" ), is( "true" ) );
		assertThat( parameters.get( "attr2" ), is( "true" ) );

		parameters = UriUtil.parseQuery( "attr1=value1&attr2=value2" );
		assertThat( parameters.get( "attr1" ), is( "value1" ) );
		assertThat( parameters.get( "attr2" ), is( "value2" ) );
	}

	@Test
	void testGetUriPartsWithOpaqueUri() {
		assertThat( UriUtil.getParts( URI.create( "program:about#details" ) ), contains( "program", "about", "details" ) );
	}

	@Test
	void testGetUriPartsWithHierarchicalUri() {
		assertThat(
			UriUtil.getParts( URI.create( "http://avereon.com/download/razor/product/card#latest" ) ),
			contains( "http", "avereon.com", "", "download", "razor", "product", "card", "latest" )
		);
		assertThat( UriUtil.getParts( URI.create( "/" ) ).size(), is( 2 ) );
		assertThat( UriUtil.getParts( URI.create( "" ) ), contains( "" ) );
	}

	@Test
	void testGetUriPartsWithRelativeUri() {
		assertThat( UriUtil.getParts( URI.create( "/download/razor/product" ) ), contains( "", "download", "razor", "product" ) );
	}

	@Test
	void testGetUriMatchScoreWithOpaqueUri() {
		URI a = URI.create( "program:about" );
		URI b = URI.create( "program:about#details" );
		URI c = URI.create( "program:settings" );
		URI d = URI.create( "program:settings#general" );

		assertThat( UriUtil.getMatchScore( a, a ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( b, b ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( c, c ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( d, d ), is( 0 ) );

		assertThat( UriUtil.getMatchScore( a, b ), is( 1 ) );
		assertThat( UriUtil.getMatchScore( b, a ), is( 1 ) );
		assertThat( UriUtil.getMatchScore( a, c ), is( 1 ) );
		assertThat( UriUtil.getMatchScore( c, a ), is( 1 ) );

		assertThat( UriUtil.getMatchScore( a, d ), is( 2 ) );
		assertThat( UriUtil.getMatchScore( d, a ), is( 2 ) );
		assertThat( UriUtil.getMatchScore( b, d ), is( 2 ) );
		assertThat( UriUtil.getMatchScore( d, b ), is( 2 ) );
	}

	@Test
	void testGetUriMatchScoreWithHierarchicalUri() {
		URI a = URI.create( "" );
		URI b = URI.create( "/" );
		URI c = URI.create( "ssh://user@sshhost.com" );
		URI d = URI.create( "http://avereon.com/download/xenon" );
		URI e = URI.create( "http://avereon.com/download/xenon/product/card" );
		URI f = URI.create( "http://avereon.com/download/xenon/product/card#latest" );

		// Same matches
		assertThat( UriUtil.getMatchScore( a, a ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( b, b ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( c, c ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( d, d ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( e, e ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( f, f ), is( 0 ) );

		// Empty and root checks
		assertThat( UriUtil.getMatchScore( a, c ), is( 4 ) );
		assertThat( UriUtil.getMatchScore( a, d ), is( 5 ) );
		assertThat( UriUtil.getMatchScore( a, e ), is( 7 ) );
		assertThat( UriUtil.getMatchScore( a, f ), is( 8 ) );

		assertThat( UriUtil.getMatchScore( b, c ), is( 4 ) );
		assertThat( UriUtil.getMatchScore( b, d ), is( 5 ) );
		assertThat( UriUtil.getMatchScore( b, e ), is( 7 ) );
		assertThat( UriUtil.getMatchScore( b, f ), is( 8 ) );

		// Different scheme
		assertThat( UriUtil.getMatchScore( c, d ), is( 5 ) );
		assertThat( UriUtil.getMatchScore( d, c ), is( 5 ) );

		// Different path
		assertThat( UriUtil.getMatchScore( d, e ), is( 2 ) );
		assertThat( UriUtil.getMatchScore( d, f ), is( 3 ) );
		assertThat( UriUtil.getMatchScore( e, f ), is( 1 ) );
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
		assertThat( UriUtil.getMatchScore( a, a ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( b, b ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( c, c ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( d, d ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( e, e ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( f, f ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( g, g ), is( 0 ) );
		assertThat( UriUtil.getMatchScore( h, h ), is( 0 ) );

		// Empty and root checks
		assertThat( UriUtil.getMatchScore( a, b ), is( 1 ) );
		assertThat( UriUtil.getMatchScore( a, c ), is( 1 ) );
		assertThat( UriUtil.getMatchScore( a, d ), is( 4 ) );
		assertThat( UriUtil.getMatchScore( a, f ), is( 4 ) );

		assertThat( UriUtil.getMatchScore( b, a ), is( 1 ) );
		assertThat( UriUtil.getMatchScore( b, c ), is( 1 ) );
		assertThat( UriUtil.getMatchScore( b, d ), is( 4 ) );
		assertThat( UriUtil.getMatchScore( b, f ), is( 4 ) );

		// Absolute path checks
		assertThat( UriUtil.getMatchScore( c, d ), is( 3 ) );
		assertThat( UriUtil.getMatchScore( c, e ), is( 2 ) );
		assertThat( UriUtil.getMatchScore( c, f ), is( 4 ) );

		assertThat( UriUtil.getMatchScore( d, c ), is( 3 ) );
		assertThat( UriUtil.getMatchScore( d, e ), is( 5 ) );
		assertThat( UriUtil.getMatchScore( d, f ), is( 5 ) );

		// Relative path checks
		assertThat( UriUtil.getMatchScore( e, f ), is( 3 ) );
		assertThat( UriUtil.getMatchScore( e, g ), is( 2 ) );
		assertThat( UriUtil.getMatchScore( e, h ), is( 5 ) );

		assertThat( UriUtil.getMatchScore( f, e ), is( 3 ) );
		assertThat( UriUtil.getMatchScore( f, g ), is( 4 ) );
		assertThat( UriUtil.getMatchScore( f, h ), is( 5 ) );
	}

}
