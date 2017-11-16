package com.xeomar.util;

import junit.framework.TestCase;

import java.io.File;
import java.net.URI;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class UriUtilTest extends TestCase {

	public void testResolveWithString() throws Exception {
		assertThat( UriUtil.resolve( "" ), is( new File( "" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "." ), is( new File( "." ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "test" ), is( new File( "test" ).getCanonicalFile().toURI() ) );
		assertThat( UriUtil.resolve( "/test" ), is( new File( "/test" ).getCanonicalFile().toURI() ) );

		assertThat( UriUtil.resolve( "ssh://localhost" ), is( URI.create( "ssh://localhost" ) ) );
	}

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

	public void testResolveWithAbsoluteUri() {
		URI jar = URI.create( "jar:file:/test/folder%20with%20spaces/file.jar!/path/to/resource" );
		URI icon = URI.create( "http://www.parallelsymmetry.com/images/icons/escape.png" );
		assertThat( UriUtil.resolve( jar, icon ), is( URI.create( "http://www.parallelsymmetry.com/images/icons/escape.png" ) ) );
	}

	public void testGetParent() {
		URI absolute = URI.create( "file:/test/folder/absolute" );
		URI opaque = URI.create( "jar:" + absolute.toString() );
		URI doubleOpaque = URI.create( "double:jar:" + absolute.toString() );

		assertThat( UriUtil.getParent( absolute ).toString(), is( "file:/test/folder/" ) );
		assertThat( UriUtil.getParent( opaque ).toString(), is( "jar:file:/test/folder/" ) );
		assertThat( UriUtil.getParent( doubleOpaque ).toString(), is( "double:jar:file:/test/folder/" ) );
	}

	public void testParseQueryWithUri() {
		assertNull( UriUtil.parseQuery( (URI)null ) );

		URI uri = URI.create( "test:///path?attr1&attr2" );
		Map<String, String> parameters = UriUtil.parseQuery( uri );
		assertThat( parameters.get( "attr1" ), is( "true" ) );
		assertThat( parameters.get( "attr2" ), is( "true" ) );

		uri = URI.create( "test:///path?attr1=value1&attr2=value2" );
		parameters = UriUtil.parseQuery( uri );
		assertThat( parameters.get( "attr1" ), is( "value1" ) );
		assertThat( parameters.get( "attr2" ), is( "value2" ) );
	}

	public void testParseQueryWithString() {
		assertNull( UriUtil.parseQuery( (String)null ) );

		Map<String, String> parameters = UriUtil.parseQuery( "attr1&attr2" );
		assertThat( parameters.get( "attr1" ), is( "true" ) );
		assertThat( parameters.get( "attr2" ), is( "true" ) );

		parameters = UriUtil.parseQuery( "attr1=value1&attr2=value2" );
		assertThat( parameters.get( "attr1" ), is( "value1" ) );
		assertThat( parameters.get( "attr2" ), is( "value2" ) );
	}

}
