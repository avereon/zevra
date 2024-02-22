package com.avereon.util;

import lombok.CustomLog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@CustomLog
public final class JavaUtil {

	public static int getVersion() {
		String version = System.getProperty( "java.version" );
		String[] parts = version.split( "\\." );
		int majorVersion = Integer.parseInt( parts[ 0 ] );
		if( majorVersion == 1 ) 			majorVersion = Integer.parseInt( parts[ 1 ] );
		return majorVersion;
	}

	public static boolean isTest() {
		try {
			JavaUtil.class.getClassLoader().loadClass( "org.junit.Assert" );
			return true;
		} catch( Throwable throwable ) {
			return false;
		}
	}

	public static boolean isClassInStackTrace( Class<?> clazz ) {
		String className = clazz.getName();
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		for( StackTraceElement element : stack ) {
			if( Objects.equals( element.getClassName(), className ) ) return true;
		}

		return false;
	}

	public static String getCallingClassName() {
		return getCallerElement().getClassName();
	}

	public static String getCallingModuleName() {
		return getCallerElement().getModuleName();
	}

	public static String getCallingMethodName() {
		return getCallerElement().getMethodName();
	}

	private static StackTraceElement getCallerElement() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		return elements[ getCallerIndex( elements ) ];
	}

	private static int getCallerIndex( StackTraceElement[] elements ) {
		int index = 0;

		// Skip the initial elements of Thread class
		String skipClass = elements[ index ].getClassName();
		while( Objects.equals( elements[ index ].getClassName(), skipClass ) ) {
			index++;
		}

		// Skip the next elements of JavaUtil class
		skipClass = elements[ index ].getClassName();
		while( Objects.equals( elements[ index ].getClassName(), skipClass ) ) {
			index++;
		}

		// Skip the next elements of the asking class
		skipClass = elements[ index ].getClassName();
		while( Objects.equals( elements[ index ].getClassName(), skipClass ) ) {
			index++;
		}

		// Return the element with the class calling the asking class
		return index;
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
				uri = new URI( URLDecoder.decode( token, StandardCharsets.UTF_8 ) );
			} catch( URISyntaxException exception ) {
				uri = new File( token ).toURI();
			}
			if( uri.getScheme() == null ) uri = new File( token ).toURI();

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
			log.atFiner().log( "Class loader for %s:%s", getClassName( (Class<?>)object ), ((Class<?>)object).getClassLoader() );
		} else {
			log.atFiner().log( "Class loader for %s:%s", getClassName( object.getClass() ), getClassLoader( object ) );
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
