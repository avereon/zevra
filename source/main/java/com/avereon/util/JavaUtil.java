package com.avereon.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public final class JavaUtil {

	private static final System.Logger log = Log.get();

	public static boolean isTest() {
		try {
			JavaUtil.class.getClassLoader().loadClass( "org.junit.Assert" );
			return true;
		} catch( Throwable throwable ) {
			return false;
		}
	}

	public static boolean isClassInStackTrace( Class<?> clazz ) {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		for( StackTraceElement element : stack ) {
			if( Objects.equals( element.getClassName(), clazz.getName() ) ) return true;
		}

		return false;
	}

	public static Class<?> getCallingClass() {
		return getCallingClass( 1 );
	}

	public static Class<?> getCallingClass( int level ) {
		try {
			return Class.forName( getCallingClassName( level ) );
		}catch( Exception exception ) {
			log.log( Log.WARN, exception.getMessage() );
		}
		return null;
	}

	public static String getCallingClassName() {
		return getCallingClassName( 1 );
	}

	public static String getCallingClassName( int level ) {
		return getElement( level ).getClassName();
	}

	public static String getCallingMethodName() {
		return getCallingMethodName( 1 );
	}

	public static String getCallingMethodName( int level ) {
		return getElement( level ).getMethodName();
	}

	public static String getCaller() {
		return getCaller( 1 );
	}

	public static String getCaller( int level ) {
		StackTraceElement element = getElement( level );
		return element.getClassName() + "." + element.getMethodName();
	}

	@SuppressWarnings( "OptionalGetWithoutIsPresent" )
	private static StackTraceElement getElement( int level ) {
		return StackWalker.getInstance().walk( s -> s.skip( level + 2 ).findFirst() ).get().toStackTraceElement();
	}

	/**
	 * Get the simple class name from a full class name.
	 *
	 * @param name The fully qualified class name
	 * @return The class name without the package
	 */
	public static String getClassName( String name ) {
		return name == null ? null : name.substring( name.lastIndexOf( '.' ) + 1 );
	}

	/**
	 * Get the simple class name from a full class name.
	 *
	 * @param type The class from which to derive the name
	 * @return The class name without the package
	 */
	public static String getClassName( Class<?> type ) {
		return type == null ? null : getClassName( type.getName() );
	}

	/**
	 * Get only the class name from an object.
	 *
	 * @param object The object from which to derive the class name
	 * @return The class name without the package
	 */
	public static String getClassName( Object object ) {
		return object == null ? null : getClassName( object.getClass().getName() );
	}

	public static String getShortClassName( String name ) {
		StringBuilder builder = new StringBuilder();
		int index = 0;
		boolean done = false;
		do {
			String letter = name.substring( index, index + 1 );
			index = name.indexOf( ".", index + 1 );
			if( index != -1 ) {
				builder.append( letter );
				builder.append( "." );
				index++;
			} else {
				done = true;
			}
		} while( !done );
		builder.append( getClassName( name ) );

		return builder.toString();
	}

	public static String getShortClassName( Class<?> type ) {
		return getShortClassName( type.getName() );
	}

	public static String getKeySafeClassName( String name ) {
		return name.replace( "$", "." );
	}

	public static String getKeySafeClassName( Class<?> type ) {
		return getKeySafeClassName( type.getName() );
	}

	public static String getPackageName( String name ) {
		return name.substring( 0, name.lastIndexOf( '.' ) );
	}

	public static String getPackageName( Class<?> type ) {
		return (getPackageName( type.getName() ));
	}

	public static String getPackagePath( String name ) {
		return "/" + getPackageName( name ).replace( '.', '/' );
	}

	public static String getPackagePath( Class<?> type ) {
		return (getPackagePath( type.getName() ));
	}

	public static List<URI> getClasspath() {
		return parsePropertyPaths( System.getProperty( "class.path" ) );
	}

	public static List<URI> getModulePath() {
		return parsePropertyPaths( System.getProperty( "jdk.module.path" ) );
	}

	public static List<URI> parsePropertyPaths( String classpath ) {
		return parsePropertyPaths( classpath, File.pathSeparator );
	}

	/**
	 * Parse relative URI strings from the specified path in system property format.
	 */
	public static List<URI> parsePropertyPaths( String paths, String separator ) {
		ArrayList<URI> list = new ArrayList<>();
		if( paths == null ) return list;

		URI uri = null;
		String token;
		StringTokenizer tokenizer = new StringTokenizer( paths, separator );
		while( tokenizer.hasMoreTokens() ) {
			token = tokenizer.nextToken();

			try {
				uri = new URI( URLDecoder.decode( token, "UTF-8" ) );
			} catch( URISyntaxException exception ) {
				uri = new File( token ).toURI();
			} catch( UnsupportedEncodingException exception ) {
				// Intentionally ignore exception because UTF-8 is always supported.
			}
			if( uri != null && uri.getScheme() == null ) uri = new File( token ).toURI();

			list.add( uri );
		}

		return list;
	}

	/**
	 * Parse the relative URLs from the specified classpath in JAR file manifest format. See <a href= "http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html#Main%20Attributes"
	 * >Setting the JAR Manifest Class-Path Attribute</a>
	 */
	public static List<URL> parseManifestClasspath( URI base, String classpath ) throws IOException, URISyntaxException {
		List<URL> urls = new ArrayList<>();

		if( base == null || classpath == null ) return urls;

		StringTokenizer tokenizer = new StringTokenizer( classpath, " " );
		while( tokenizer.hasMoreTokens() ) {
			String path = tokenizer.nextToken();
			urls.add( new URL( base.resolve( path ).toString() ) );
		}

		return urls;
	}

	/**
	 * Get the root cause of a throwable. This method calls getCause() until the
	 * cause without a cause (the root cause) is found.
	 *
	 * @param throwable The throwable from which to retrieve the root cause
	 * @return The root cause of a throwable
	 */
	public static Throwable getRootCause( Throwable throwable ) {
		Throwable cause = throwable;

		while( cause != null && cause.getCause() != null ) {
			cause = cause.getCause();
		}

		return cause;
	}

	public static void printClassLoader( Object object ) {
		if( object instanceof Class ) {
			log.log( Log.TRACE, "Class loader for " + getClassName( (Class<?>)object ) + ": " + ((Class<?>)object).getClassLoader() );
		} else {
			log.log( Log.TRACE, "Class loader for " + getClassName( object.getClass() ) + ": " + getClassLoader( object ) );
		}
	}

	public static ClassLoader getClassLoader( Object object ) {
		return object.getClass().getClassLoader();
	}

	public static void printSystemProperties() {
		Properties properties = System.getProperties();
		List<String> keys = new ArrayList<>();
		System.getProperties().keySet().forEach( entry -> keys.add( entry.toString() ) );
		Collections.sort( keys );
		keys.forEach( key -> System.out.println( key + "=" + properties.getProperty( key ) ) );
	}

}
