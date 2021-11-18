package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JavaUtilTest {

	@Test
	void testGetCallingClassName() {
		assertThat( JavaUtil.getCallingClassName() ).isEqualTo( "jdk.internal.reflect.NativeMethodAccessorImpl" );
	}

	@Test
	void testGetCallingMethodName() {
		assertThat( JavaUtil.getCallingMethodName() ).isEqualTo( "invoke0" );
	}

	@Test
	void testGetClassNameWithString() {
		assertThat( JavaUtil.getClassName( "java.lang.Object" ) ).isEqualTo( "Object" );
	}

	@Test
	void testGetClassNameWithClass() {
		assertThat( JavaUtil.getClassName( Object.class ) ).isEqualTo( "Object" );
	}

	@Test
	void testGetShortClassNameWithString() {
		assertThat( JavaUtil.getShortClassName( "java.lang.Object" ) ).isEqualTo( "j.l.Object" );
		assertThat( JavaUtil.getShortClassName( "com.avereon.xenon.Program" ) ).isEqualTo( "c.a.x.Program" );
	}

	@Test
	void testGetShortClassNameWithClass() {
		assertThat( JavaUtil.getShortClassName( Object.class ) ).isEqualTo( "j.l.Object" );
		assertThat( JavaUtil.getShortClassName( JavaUtil.class ) ).isEqualTo( "c.a.u.JavaUtil" );
	}

	@Test
	void testGetKeySafeClassNameWithString() {
		assertThat( JavaUtil.getKeySafeClassName( "com.avereon.util.JavaUtilTest$InternalClass" ) ).isEqualTo( "com.avereon.util.JavaUtilTest.InternalClass" );
	}

	@Test
	void testGetKeySafeClassNameWithClass() {
		assertThat( JavaUtil.getKeySafeClassName( JavaUtilTest.InternalClass.class ) ).isEqualTo( "com.avereon.util.JavaUtilTest.InternalClass" );
	}

	@Test
	void testGetPackageNameWithString() {
		assertThat( JavaUtil.getPackageName( "java.lang.Object" ) ).isEqualTo( "java.lang" );
	}

	@Test
	void testGetPackageNameWithClass() {
		assertThat( JavaUtil.getPackageName( Object.class ) ).isEqualTo( "java.lang" );
	}

	@Test
	void testGetPackagePathWithString() {
		assertThat( JavaUtil.getPackagePath( "java.lang.Object" ) ).isEqualTo( "/java/lang" );
	}

	@Test
	void testGetPackagePathWithClass() {
		assertThat( JavaUtil.getPackagePath( Object.class ) ).isEqualTo( "/java/lang" );
	}

	@Test
	void testParseClasspath() {
		List<URI> entries = JavaUtil.parsePropertyPaths( null );
		assertThat( entries.size() ).isEqualTo( 0 );

		String separator = ";";
		String classpath = "test1.jar";
		classpath += separator + "test2.jar";
		classpath += separator + URLEncoder.encode( "http://www.avereon.com/software/test3.jar", StandardCharsets.UTF_8 );
		entries = JavaUtil.parsePropertyPaths( classpath, separator );

		assertThat( entries.get( 0 ) ).isEqualTo( new File( "test1.jar" ).toURI() );
		assertThat( entries.get( 1 ) ).isEqualTo( new File( "test2.jar" ).toURI() );
		assertThat( entries.get( 2 ) ).isEqualTo( URI.create( "http://www.avereon.com/software/test3.jar" ) );

		separator = ":";
		classpath = "test1.jar";
		classpath += separator + "test2.jar";
		classpath += separator + URLEncoder.encode( "http://www.avereon.com/software/test3.jar", StandardCharsets.UTF_8 );
		entries = JavaUtil.parsePropertyPaths( classpath, separator );

		assertThat( entries.get( 0 ) ).isEqualTo( new File( "test1.jar" ).toURI() );
		assertThat( entries.get( 1 ) ).isEqualTo( new File( "test2.jar" ).toURI() );
		assertThat( entries.get( 2 ) ).isEqualTo( URI.create( "http://www.avereon.com/software/test3.jar" ) );
	}

	@Test
	void testParseManifestClasspath() throws Exception {
		File home = new File( "." ).getCanonicalFile();
		URI base = home.toURI();
		String classpath = "test1.jar test2.jar test%203.jar";

		List<URL> entries = JavaUtil.parseManifestClasspath( base, null );
		assertThat( entries.size() ).isEqualTo( 0 );

		entries = JavaUtil.parseManifestClasspath( null, classpath );
		assertThat( entries.size() ).isEqualTo( 0 );

		entries = JavaUtil.parseManifestClasspath( base, classpath );

		assertThat( entries.get( 0 ) ).isEqualTo( new File( home.getCanonicalFile(), "test1.jar" ).toURI().toURL() );
		assertThat( entries.get( 1 ) ).isEqualTo( new File( home.getCanonicalFile(), "test2.jar" ).toURI().toURL() );
		assertThat( entries.get( 2 ) ).isEqualTo( new File( home.getCanonicalFile(), "test 3.jar" ).toURI().toURL() );
	}

	@Test
	void testGetRootCause() {
		assertThat( JavaUtil.getRootCause( null ) ).isNull();

		Throwable one = new Throwable();
		Throwable two = new Throwable( one );
		Throwable three = new Throwable( two );

		assertThat( JavaUtil.getRootCause( one ) ).isEqualTo( one );
		assertThat( JavaUtil.getRootCause( two ) ).isEqualTo( one );
		assertThat( JavaUtil.getRootCause( three ) ).isEqualTo( one );
	}

	private static class InternalClass {}

}
