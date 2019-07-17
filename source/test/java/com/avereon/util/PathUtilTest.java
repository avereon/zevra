package com.avereon.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

public class PathUtilTest {

	@Test
	public void testIsAbsolute() {
		assertThat( PathUtil.isAbsolute( "/" ), is( true ) );
		assertThat( PathUtil.isAbsolute( "/test" ), is( true ) );
		assertThat( PathUtil.isAbsolute( "/test/path" ), is( true ) );
		assertThat( PathUtil.isAbsolute( "/test/path/" ), is( true ) );

		assertThat( PathUtil.isAbsolute( null ), is( false ) );
		assertThat( PathUtil.isAbsolute( "" ), is( false ) );
		assertThat( PathUtil.isAbsolute( "test" ), is( false ) );
		assertThat( PathUtil.isAbsolute( "test/path" ), is( false ) );
	}

	@Test
	public void testIsRelative() {
		assertThat( PathUtil.isRelative( null ), is( false ) );
		assertThat( PathUtil.isRelative( "" ), is( true ) );
		assertThat( PathUtil.isRelative( "test" ), is( true ) );
		assertThat( PathUtil.isRelative( "test/path" ), is( true ) );

		assertThat( PathUtil.isRelative( "/" ), is( false ) );
		assertThat( PathUtil.isRelative( "/test" ), is( false ) );
		assertThat( PathUtil.isRelative( "/test/" ), is( false ) );
		assertThat( PathUtil.isRelative( "/test/path" ), is( false ) );
		assertThat( PathUtil.isRelative( "/test/path/" ), is( false ) );
	}

	@Test
	public void testGetParent() {
		assertThat( PathUtil.getParent( null ), is( nullValue() ) );
		assertThat( PathUtil.getParent( "" ), is( nullValue() ) );
		assertThat( PathUtil.getParent( "/" ), is( nullValue() ) );

		assertThat( PathUtil.getParent( "test" ), is( "" ) );
		assertThat( PathUtil.getParent( "test/path" ), is( "test" ) );
		assertThat( PathUtil.getParent( "test/path/" ), is( "test" ) );

		assertThat( PathUtil.getParent( "/test" ), is( "/" ) );
		assertThat( PathUtil.getParent( "/test/" ), is( "/" ) );
		assertThat( PathUtil.getParent( "/test/path" ), is( "/test" ) );
		assertThat( PathUtil.getParent( "/test/path/" ), is( "/test" ) );

		assertThat( PathUtil.getParent( "/../test" ), is( "/.." ) );
	}

	@Test
	public void testGetName() {
		assertThat( PathUtil.getName( null ), is( nullValue() ) );
		assertThat( PathUtil.getName( "" ), is( "" ) );
		assertThat( PathUtil.getName( "/" ), is( "/" ) );
		assertThat( PathUtil.getName( "/test" ), is( "test" ) );
		assertThat( PathUtil.getName( "/test/path" ), is( "path" ) );
	}

	@Test
	public void testNormalize() {
		// Normal paths
		assertThat( PathUtil.normalize( null ), is( nullValue() ) );
		assertThat( PathUtil.normalize( "" ), is( "" ) );
		assertThat( PathUtil.normalize( "/" ), is( "/" ) );
		assertThat( PathUtil.normalize( "/test" ), is( "/test" ) );
		assertThat( PathUtil.normalize( "/test/path" ), is( "/test/path" ) );

		// Trailing separators
		assertThat( PathUtil.normalize( "/test/" ), is( "/test" ) );
		assertThat( PathUtil.normalize( "/test/path/" ), is( "/test/path" ) );

		// Multiple separators
		assertThat( PathUtil.normalize( "/////test" ), is( "/test" ) );
		assertThat( PathUtil.normalize( "/test/////path" ), is( "/test/path" ) );

		// Parent references
		assertThat( PathUtil.normalize( "/.." ), is( nullValue() ) );
		assertThat( PathUtil.normalize( "/../test" ), is( nullValue() ) );
		assertThat( PathUtil.normalize( "/test/../path" ), is( "/path" ) );
	}

	@Test
	public void testResolve() {
		// Null paths
		assertThat( PathUtil.resolve( null, "" ), is( nullValue() ) );
		assertThat( PathUtil.resolve( "", null ), is( nullValue() ) );

		// Empty paths
		assertThat( PathUtil.resolve( "", "" ), is( "" ) );
		assertThat( PathUtil.resolve( "/", "" ), is( "/" ) );
		assertThat( PathUtil.resolve( "", "/" ), is( "/" ) );
		assertThat( PathUtil.resolve( "foo/bar", "" ), is( "foo/bar" ) );
		assertThat( PathUtil.resolve( "", "foo/bar" ), is( "foo/bar" ) );

		// Normal paths
		assertThat( PathUtil.resolve( "/", "foo" ), is( "/foo" ) );
		assertThat( PathUtil.resolve( "/foo", "bar" ), is( "/foo/bar" ) );
		assertThat( PathUtil.resolve( "/foo", "/bar" ), is( "/bar" ) );
		assertThat( PathUtil.resolve( "/foo/bar", "gus" ), is( "/foo/bar/gus" ) );
		assertThat( PathUtil.resolve( "/foo", "bar/gus" ), is( "/foo/bar/gus" ) );
		assertThat( PathUtil.resolve( "foo", "bar" ), is( "foo/bar" ) );
		assertThat( PathUtil.resolve( "foo", "/bar" ), is( "/bar" ) );
		assertThat( PathUtil.resolve( "foo/bar", "gus" ), is( "foo/bar/gus" ) );
		assertThat( PathUtil.resolve( "foo", "bar/gus" ), is( "foo/bar/gus" ) );
	}

	@Test
	public void testRelativize() {
		// Null paths
		assertThat( PathUtil.relativize( null, "" ), is( nullValue() ) );
		assertThat( PathUtil.relativize( null, "/" ), is( nullValue() ) );
		assertThat( PathUtil.relativize( null, "/foo" ), is( nullValue() ) );
		assertThat( PathUtil.relativize( "", null ), is( nullValue() ) );
		assertThat( PathUtil.relativize( "/", null ), is( nullValue() ) );
		assertThat( PathUtil.relativize( "/foo", null ), is( nullValue() ) );

		// Equals paths
		assertThat( PathUtil.relativize( "/", "/" ), is( "" ) );
		assertThat( PathUtil.relativize( "/foo", "/foo" ), is( "" ) );
		assertThat( PathUtil.relativize( "/foo/bar", "/foo/bar" ), is( "" ) );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo/bar/gus" ), is( "" ) );

		// Empty source
		assertThat( PathUtil.relativize( "", "" ), is( "" ) );
		assertThat( PathUtil.relativize( "", "foo" ), is( "foo" ) );
		assertThat( PathUtil.relativize( "", "foo/bar" ), is( "foo/bar" ) );

		// Root sources
		assertThat( PathUtil.relativize( "/", "/foo" ), is( "foo" ) );
		assertThat( PathUtil.relativize( "/", "/foo/bar" ), is( "foo/bar" ) );
		assertThat( PathUtil.relativize( "/", "/foo/bar/gus" ), is( "foo/bar/gus" ) );

		// Small sources
		assertThat( PathUtil.relativize( "/foo", "/foo/bar" ), is( "bar" ) );
		assertThat( PathUtil.relativize( "/foo", "/foo/bar/gus" ), is( "bar/gus" ) );

		// Root targets
		assertThat( PathUtil.relativize( "/foo", "/" ), is( ".." ) );
		assertThat( PathUtil.relativize( "/foo/bar", "/" ), is( "../.." ) );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/" ), is( "../../.." ) );

		// Small targets
		assertThat( PathUtil.relativize( "/foo/bar", "/foo" ), is( ".." ) );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo" ), is( "../.." ) );

		// Mismatch paths
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo/bar/sag" ), is( "../sag" ) );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo/gin/sag" ), is( "../../gin/sag" ) );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo/gus/bar" ), is( "../../gus/bar" ) );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/gus/bar/foo" ), is( "../../../gus/bar/foo" ) );
	}

	@Test
	public void testRelativizeWithMixedAbsoluteAndRelativePaths() {
		// Absolute and relative
		try {
			PathUtil.relativize( "/foo", "bar" );
			fail( "Expected an IllegalArgumentException to be thrown" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage(), is( "Target is different type of path" ) );
		}

		// Relative and absolute
		try {
			PathUtil.relativize( "foo", "/bar" );
			fail( "Expected an IllegalArgumentException to be thrown" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage(), is( "Target is different type of path" ) );
		}
	}

	@Test
	public void testParseNames() {
		assertThat( PathUtil.parseNames( "/foo/bar" ), is( new String[]{ "/", "foo", "bar" } ) );
		assertThat( PathUtil.parseNames( "/foo/bar/" ), is( new String[]{ "/", "foo", "bar" } ) );
		assertThat( PathUtil.parseNames( "foo/bar" ), is( new String[]{ "foo", "bar" } ) );
		assertThat( PathUtil.parseNames( "/" ), is( new String[]{ "/" } ) );
		assertThat( PathUtil.parseNames( " " ), is( new String[]{ " " } ) );
		assertThat( PathUtil.parseNames( "" ), is( new String[]{ "" } ) );
		assertThat( PathUtil.parseNames( null ), is( nullValue() ) );
	}

	@Test
	public void testGetChild() {
		assertThat( PathUtil.getChild( null, "" ), is( nullValue() ) );
		assertThat( PathUtil.getChild( "", null ), is( nullValue() ) );

		assertThat( PathUtil.getChild( "", "" ), is( "" ) );
		assertThat( PathUtil.getChild( "/foo", "/foo" ), is( "" ) );

		assertThat( PathUtil.getChild( "/foo", "/foo/bar" ), is( "bar" ) );
		assertThat( PathUtil.getChild( "/foo", "/foo/bar/gus" ), is( "bar" ) );
	}

}
