package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

class PathUtilTest {

	@Test
	@SuppressWarnings( "ConstantConditions" )
	void testIsAbsolute() {
		assertThat( PathUtil.isAbsolute( "/" ) ).isTrue();
		assertThat( PathUtil.isAbsolute( "/test" ) ).isTrue();
		assertThat( PathUtil.isAbsolute( "/test/path" ) ).isTrue();
		assertThat( PathUtil.isAbsolute( "/test/path/" ) ).isTrue();

		assertThat( PathUtil.isAbsolute( null ) ).isFalse();
		assertThat( PathUtil.isAbsolute( "" ) ).isFalse();
		assertThat( PathUtil.isAbsolute( "test" ) ).isFalse();
		assertThat( PathUtil.isAbsolute( "test/path" ) ).isFalse();
	}

	@Test
	@SuppressWarnings( "ConstantConditions" )
	void testIsRelative() {
		assertThat( PathUtil.isRelative( null ) ).isFalse();
		assertThat( PathUtil.isRelative( "" ) ).isTrue();
		assertThat( PathUtil.isRelative( "test" ) ).isTrue();
		assertThat( PathUtil.isRelative( "test/path" ) ).isTrue();

		assertThat( PathUtil.isRelative( "/" ) ).isFalse();
		assertThat( PathUtil.isRelative( "/test" ) ).isFalse();
		assertThat( PathUtil.isRelative( "/test/" ) ).isFalse();
		assertThat( PathUtil.isRelative( "/test/path" ) ).isFalse();
		assertThat( PathUtil.isRelative( "/test/path/" ) ).isFalse();
	}

	@Test
	void testGetParent() {
		assertThat( PathUtil.getParent( null ) ).isNull();
		assertThat( PathUtil.getParent( "" ) ).isNull();
		assertThat( PathUtil.getParent( "/" ) ).isNull();

		assertThat( PathUtil.getParent( "test" ) ).isEqualTo( "" );
		assertThat( PathUtil.getParent( "test/path" ) ).isEqualTo( "test" );
		assertThat( PathUtil.getParent( "test/path/" ) ).isEqualTo( "test" );

		assertThat( PathUtil.getParent( "/test" ) ).isEqualTo( "/" );
		assertThat( PathUtil.getParent( "/test/" ) ).isEqualTo( "/" );
		assertThat( PathUtil.getParent( "/test/path" ) ).isEqualTo( "/test" );
		assertThat( PathUtil.getParent( "/test/path/" ) ).isEqualTo( "/test" );

		assertThat( PathUtil.getParent( "/../test" ) ).isEqualTo( "/.." );
	}

	@Test
	void testGetName() {
		assertThat( PathUtil.getName( null ) ).isNull();
		assertThat( PathUtil.getName( "" ) ).isEqualTo( "" );
		assertThat( PathUtil.getName( "/" ) ).isEqualTo( "/" );
		assertThat( PathUtil.getName( "/test" ) ).isEqualTo( "test" );
		assertThat( PathUtil.getName( "/test/path" ) ).isEqualTo( "path" );
		assertThat( PathUtil.getName( "/test/path/" ) ).isEqualTo( "path" );
	}

	@Test
	void testNormalize() {
		// Normal paths
		assertThat( PathUtil.normalize( null ) ).isNull();
		assertThat( PathUtil.normalize( "" ) ).isEqualTo( "" );
		assertThat( PathUtil.normalize( "/" ) ).isEqualTo( "/" );
		assertThat( PathUtil.normalize( "/test" ) ).isEqualTo( "/test" );
		assertThat( PathUtil.normalize( "/test/path" ) ).isEqualTo( "/test/path" );

		// Trailing separators
		assertThat( PathUtil.normalize( "/test/" ) ).isEqualTo( "/test" );
		assertThat( PathUtil.normalize( "/test/path/" ) ).isEqualTo( "/test/path" );

		// Multiple separators
		assertThat( PathUtil.normalize( "/////test" ) ).isEqualTo( "/test" );
		assertThat( PathUtil.normalize( "/test/////path" ) ).isEqualTo( "/test/path" );

		// Parent references
		assertThat( PathUtil.normalize( "/.." ) ).isNull();
		assertThat( PathUtil.normalize( "/../test" ) ).isNull();
		assertThat( PathUtil.normalize( "/test/../path" ) ).isEqualTo( "/path" );
	}

	@Test
	void testResolve() {
		// Null paths
		assertThat( PathUtil.resolve( null, "" ) ).isNull();
		assertThat( PathUtil.resolve( "", null ) ).isNull();

		// Empty paths
		assertThat( PathUtil.resolve( "", "" ) ).isEqualTo( "" );
		assertThat( PathUtil.resolve( "/", "" ) ).isEqualTo( "/" );
		assertThat( PathUtil.resolve( "", "/" ) ).isEqualTo( "/" );
		assertThat( PathUtil.resolve( "foo/bar", "" ) ).isEqualTo( "foo/bar" );
		assertThat( PathUtil.resolve( "", "foo/bar" ) ).isEqualTo( "foo/bar" );

		// Normal paths
		assertThat( PathUtil.resolve( "/", "foo" ) ).isEqualTo( "/foo" );
		assertThat( PathUtil.resolve( "/foo", "bar" ) ).isEqualTo( "/foo/bar" );
		assertThat( PathUtil.resolve( "/foo", "/bar" ) ).isEqualTo( "/bar" );
		assertThat( PathUtil.resolve( "/foo/bar", "gus" ) ).isEqualTo( "/foo/bar/gus" );
		assertThat( PathUtil.resolve( "/foo", "bar/gus" ) ).isEqualTo( "/foo/bar/gus" );
		assertThat( PathUtil.resolve( "foo", "bar" ) ).isEqualTo( "foo/bar" );
		assertThat( PathUtil.resolve( "foo", "/bar" ) ).isEqualTo( "/bar" );
		assertThat( PathUtil.resolve( "foo/bar", "gus" ) ).isEqualTo( "foo/bar/gus" );
		assertThat( PathUtil.resolve( "foo", "bar/gus" ) ).isEqualTo( "foo/bar/gus" );
	}

	@Test
	void testRelativize() {
		// Null paths
		assertThat( PathUtil.relativize( null, "" ) ).isNull();
		assertThat( PathUtil.relativize( null, "/" ) ).isNull();
		assertThat( PathUtil.relativize( null, "/foo" ) ).isNull();
		assertThat( PathUtil.relativize( "", null ) ).isNull();
		assertThat( PathUtil.relativize( "/", null ) ).isNull();
		assertThat( PathUtil.relativize( "/foo", null ) ).isNull();

		// Equals paths
		assertThat( PathUtil.relativize( "/", "/" ) ).isEqualTo( "" );
		assertThat( PathUtil.relativize( "/foo", "/foo" ) ).isEqualTo( "" );
		assertThat( PathUtil.relativize( "/foo/bar", "/foo/bar" ) ).isEqualTo( "" );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo/bar/gus" ) ).isEqualTo( "" );

		// Empty source
		assertThat( PathUtil.relativize( "", "" ) ).isEqualTo( "" );
		assertThat( PathUtil.relativize( "", "foo" ) ).isEqualTo( "foo" );
		assertThat( PathUtil.relativize( "", "foo/bar" ) ).isEqualTo( "foo/bar" );

		// Root sources
		assertThat( PathUtil.relativize( "/", "/foo" ) ).isEqualTo( "foo" );
		assertThat( PathUtil.relativize( "/", "/foo/bar" ) ).isEqualTo( "foo/bar" );
		assertThat( PathUtil.relativize( "/", "/foo/bar/gus" ) ).isEqualTo( "foo/bar/gus" );

		// Small sources
		assertThat( PathUtil.relativize( "/foo", "/foo/bar" ) ).isEqualTo( "bar" );
		assertThat( PathUtil.relativize( "/foo", "/foo/bar/gus" ) ).isEqualTo( "bar/gus" );

		// Root targets
		assertThat( PathUtil.relativize( "/foo", "/" ) ).isEqualTo( ".." );
		assertThat( PathUtil.relativize( "/foo/bar", "/" ) ).isEqualTo( "../.." );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/" ) ).isEqualTo( "../../.." );

		// Small targets
		assertThat( PathUtil.relativize( "/foo/bar", "/foo" ) ).isEqualTo( ".." );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo" ) ).isEqualTo( "../.." );

		// Mismatch paths
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo/bar/sag" ) ).isEqualTo( "../sag" );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo/gin/sag" ) ).isEqualTo( "../../gin/sag" );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/foo/gus/bar" ) ).isEqualTo( "../../gus/bar" );
		assertThat( PathUtil.relativize( "/foo/bar/gus", "/gus/bar/foo" ) ).isEqualTo( "../../../gus/bar/foo" );
	}

	@Test
	void testRelativizeWithMixedAbsoluteAndRelativePaths() {
		// Absolute and relative
		try {
			PathUtil.relativize( "/foo", "bar" );
			fail( "Expected an IllegalArgumentException to be thrown" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage() ).isEqualTo( "Target is different type of path" );
		}

		// Relative and absolute
		try {
			PathUtil.relativize( "foo", "/bar" );
			fail( "Expected an IllegalArgumentException to be thrown" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage() ).isEqualTo( "Target is different type of path" );
		}
	}

	@Test
	void testParseNames() {
		assertThat( PathUtil.parseNames( "/foo/bar" ) ).isEqualTo( new String[]{ "/", "foo", "bar" } );
		assertThat( PathUtil.parseNames( "/foo/bar/" ) ).isEqualTo( new String[]{ "/", "foo", "bar" } );
		assertThat( PathUtil.parseNames( "foo/bar" ) ).isEqualTo( new String[]{ "foo", "bar" } );
		assertThat( PathUtil.parseNames( "/" ) ).isEqualTo( new String[]{ "/" } );
		assertThat( PathUtil.parseNames( " " ) ).isEqualTo( new String[]{ " " } );
		assertThat( PathUtil.parseNames( "" ) ).isEqualTo( new String[]{ "" } );
		assertThat( PathUtil.parseNames( null ) ).isNull();
	}

	@Test
	void testGetChild() {
		assertThat( PathUtil.getChild( null, "" ) ).isNull();
		assertThat( PathUtil.getChild( "", null ) ).isNull();

		assertThat( PathUtil.getChild( "", "" ) ).isEqualTo( "" );
		assertThat( PathUtil.getChild( "/foo", "/foo" ) ).isEqualTo( "" );

		assertThat( PathUtil.getChild( "/foo", "/foo/bar" ) ).isEqualTo( "bar" );
		assertThat( PathUtil.getChild( "/foo", "/foo/bar/gus" ) ).isEqualTo( "bar" );
	}

}
