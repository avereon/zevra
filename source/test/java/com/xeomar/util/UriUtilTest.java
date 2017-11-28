package com.xeomar.util;

import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class UriUtilTest {

	@Test
	public void understandUriParts() {
		URI uri1 = URI.create( "program:product#updates" );
		assertThat( uri1.getScheme(), is( "program" ) );
		assertThat( uri1.getPath(), is( nullValue() ) );
		assertThat( uri1.getQuery(), is( nullValue() ) );
		assertThat( uri1.getFragment(), is( "updates" ) );
		assertThat( uri1.getAuthority(), is( nullValue() ) );
		assertThat( uri1.getSchemeSpecificPart(), is( "product" ) );

		URI uri2 = URI.create( "http://xeomar.com/download/xenon/product/card?version=latest&refresh=false" );
		assertThat( uri2.getScheme(), is( "http" ) );
		assertThat( uri2.getAuthority(), is( "xeomar.com" ) );
		assertThat( uri2.getPath(), is( "/download/xenon/product/card" ) );
		assertThat( uri2.getQuery(), is( "version=latest&refresh=false" ) );
		assertThat( uri2.getFragment(), is( nullValue() ) );
		assertThat( uri2.getSchemeSpecificPart(), is( "//xeomar.com/download/xenon/product/card?version=latest&refresh=false" ) );
	}

	@Test
	public void testResolveWithString() throws Exception {
		assertThat( UriUtil.resolve( "" ), is( new File( "" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "." ), is( new File( "." ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "test" ), is( new File( "test" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "/test" ), is( new File( "/test" ).getCanonicalFile().toURI() ) );

		assertThat( UriUtil.resolve( "ssh://localhost" ), is( URI.create( "ssh://localhost" ) ) );
	}

	@Test
	public void testResolveWithRelativeUri() {
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
	public void testResolveWithAbsoluteUri() {
		URI jar = URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/resource" );
		URI icon = URI.create( "http://www.parallelsymmetry.com/images/icons/escape.png" );
		assertThat( UriUtil.resolve( jar, icon ), is( URI.create( "http://www.parallelsymmetry.com/images/icons/escape.png" ) ) );
	}

	@Test
	public void testGetParent() {
		URI absolute = URI.create( "file:/test/folder/absolute" );
		URI opaque = URI.create( "jar:" + absolute.toString() );
		URI doubleOpaque = URI.create( "double:jar:" + absolute.toString() );

		assertThat( UriUtil.getParent( absolute ).toString(), is( "file:/test/folder/" ) );
		assertThat( UriUtil.getParent( opaque ).toString(), is( "jar:file:/test/folder/" ) );
		assertThat( UriUtil.getParent( doubleOpaque ).toString(), is( "double:jar:file:/test/folder/" ) );
	}

	@Test
	public void testParseQueryWithUri() {
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
	public void testParseQueryWithString() {
		assertThat( UriUtil.parseQuery( (String)null ), is( nullValue() ) );

		Map<String, String> parameters = UriUtil.parseQuery( "attr1&attr2" );
		assertThat( parameters.get( "attr1" ), is( "true" ) );
		assertThat( parameters.get( "attr2" ), is( "true" ) );

		parameters = UriUtil.parseQuery( "attr1=value1&attr2=value2" );
		assertThat( parameters.get( "attr1" ), is( "value1" ) );
		assertThat( parameters.get( "attr2" ), is( "value2" ) );
	}

}
