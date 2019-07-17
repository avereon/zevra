package com.avereon.util;

import org.junit.Test;

//import java.awt.geom.Rectangle2D;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class JavaUtilTest {

	@Test
	public void testGetCallingClassName() {
		assertThat( JavaUtil.getCallingClassName(), is( JavaUtilTest.class.getName() ) );
	}

	@Test
	public void testGetCallingClassNameWithLevel() {
		assertThat( JavaUtil.getCallingClassName( 0 ), is( Thread.class.getName() ) );
		assertThat( JavaUtil.getCallingClassName( 1 ), is( JavaUtil.class.getName() ) );
	}

	@Test
	public void testGetClassNameWithString() {
		assertThat( JavaUtil.getClassName( "java.lang.Object" ), is( "Object" ) );
	}

	@Test
	public void testGetClassNameWithClass() {
		assertThat( JavaUtil.getClassName( Object.class ), is( "Object" ) );
	}

	@Test
	public void testGetShortClassNameWithString() {
		assertThat( JavaUtil.getShortClassName( "java.lang.Object" ), is( "j.l.Object" ) );
		assertThat( JavaUtil.getShortClassName( "com.avereon.xenon.Program" ), is( "c.a.x.Program" ) );
	}

	@Test
	public void testGetShortClassNameWithClass() {
		assertThat( JavaUtil.getShortClassName( Object.class ), is( "j.l.Object" ) );
		assertThat( JavaUtil.getShortClassName( JavaUtil.class ), is( "c.a.u.JavaUtil" ) );
	}

	@Test
	public void testGetKeySafeClassNameWithString() {
		assertThat( JavaUtil.getKeySafeClassName( "com.avereon.util.JavaUtilTest$InternalClass" ), is( "com.avereon.util.JavaUtilTest.InternalClass" ) );
	}

	@Test
	public void testGetKeySafeClassNameWithClass() {
		assertThat( JavaUtil.getKeySafeClassName( JavaUtilTest.InternalClass.class ), is( "com.avereon.util.JavaUtilTest.InternalClass" ) );
	}

	@Test
	public void testGetPackageNameWithString() {
		assertThat( JavaUtil.getPackageName( "java.lang.Object" ), is( "java.lang" ) );
	}

	@Test
	public void testGetPackageNameWithClass() {
		assertThat( JavaUtil.getPackageName( Object.class ), is( "java.lang" ) );
	}

	@Test
	public void testGetPackagePathWithString() {
		assertThat( JavaUtil.getPackagePath( "java.lang.Object" ), is( "/java/lang" ) );
	}

	@Test
	public void testGetPackagePathWithClass() {
		assertThat( JavaUtil.getPackagePath( Object.class ), is( "/java/lang" ) );
	}

	@Test
	public void testParseClasspath() throws Exception {
		List<URI> entries = JavaUtil.parsePropertyPaths( null );
		assertThat( entries.size(), is( 0 ) );

		String separator = ";";
		String classpath = "test1.jar";
		classpath += separator + "test2.jar";
		classpath += separator + URLEncoder.encode( "http://www.avereon.com/software/test3.jar", "UTF-8" );
		entries = JavaUtil.parsePropertyPaths( classpath, separator );

		assertThat( entries.get( 0 ), is( new File( "test1.jar" ).toURI() ) );
		assertThat( entries.get( 1 ), is( new File( "test2.jar" ).toURI() ) );
		assertThat( entries.get( 2 ), is( URI.create( "http://www.avereon.com/software/test3.jar" ) ) );

		separator = ":";
		classpath = "test1.jar";
		classpath += separator + "test2.jar";
		classpath += separator + URLEncoder.encode( "http://www.avereon.com/software/test3.jar", "UTF-8" );
		entries = JavaUtil.parsePropertyPaths( classpath, separator );

		assertThat( entries.get( 0 ), is( new File( "test1.jar" ).toURI() ) );
		assertThat( entries.get( 1 ), is( new File( "test2.jar" ).toURI() ) );
		assertThat( entries.get( 2 ), is( URI.create( "http://www.avereon.com/software/test3.jar" ) ) );
	}

	@Test
	public void testParseManifestClasspath() throws Exception {
		File home = new File( "." ).getCanonicalFile();
		URI base = home.toURI();
		String classpath = "test1.jar test2.jar test%203.jar";

		List<URL> entries = JavaUtil.parseManifestClasspath( base, null );
		assertThat( entries.size(), is( 0 ) );

		entries = JavaUtil.parseManifestClasspath( null, classpath );
		assertThat( entries.size(), is( 0 ) );

		entries = JavaUtil.parseManifestClasspath( base, classpath );

		assertThat( entries.get( 0 ), is( new File( home.getCanonicalFile(), "test1.jar" ).toURI().toURL() ) );
		assertThat( entries.get( 1 ), is( new File( home.getCanonicalFile(), "test2.jar" ).toURI().toURL() ) );
		assertThat( entries.get( 2 ), is( new File( home.getCanonicalFile(), "test 3.jar" ).toURI().toURL() ) );
	}

	@Test
	public void testGetRootCause() {
		assertThat( JavaUtil.getRootCause( null ), is( nullValue() ) );

		Throwable one = new Throwable();
		Throwable two = new Throwable( one );
		Throwable three = new Throwable( two );

		assertThat( JavaUtil.getRootCause( one ), is( one ) );
		assertThat( JavaUtil.getRootCause( two ), is( one ) );
		assertThat( JavaUtil.getRootCause( three ), is( one ) );
	}

	private static class InternalClass {}

}
