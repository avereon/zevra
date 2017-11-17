package com.xeomar.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.*;

public final class JavaUtil {

	private static final Logger log = LoggerFactory.getLogger( JavaUtil.class );

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

	public static String getCallingClassName() {
		return getCallingClassName( 3 );
	}

	public static String getCallingClassName( int level ) {
		return Thread.currentThread().getStackTrace()[ level ].getClassName();
	}

	/**
	 * Get the simple class name from a full class name.
	 *
	 * @param name
	 * @return
	 */
	public static String getClassName( String name ) {
		return name.substring( name.lastIndexOf( '.' ) + 1 );
	}

	/**
	 * Get the simple class name from a full class name.
	 *
	 * @param type
	 * @return
	 */
	public static String getClassName( Class<?> type ) {
		return (getClassName( type.getName() ));
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
			return parseClasspath( System.getProperty( "class.path" ) );
	}

	public static List<URI> parseClasspath( String classpath ) {
		return parseClasspath( classpath, File.pathSeparator );
	}

	/**
	 * Parse the relative URI strings from the specified classpath in system property format. See <a href= "http://java.sun.com/javase/6/docs/technotes/tools/windows/classpath.html" >Setting the Windows Classpath</a> or <a href=
	 * "http://java.sun.com/javase/6/docs/technotes/tools/solaris/classpath.html" >Setting the Unix Classpath</a>
	 */
	public static List<URI> parseClasspath( String classpath, String separator ) {
		ArrayList<URI> list = new ArrayList<>();
		if( classpath == null ) return list;

		URI uri = null;
		String token;
		StringTokenizer tokenizer = new StringTokenizer( classpath, separator );
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
	 * Parse the relative URLs from the specified classpath in JAR file manifest format. See <a href= "http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html#Main%20Attributes" >Setting the JAR Manifest Class-Path Attribute</a>
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
	 * Get the root cause of a throwable.
	 *
	 * @param throwable
	 * @return
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
			log.trace( "Class loader for " + getClassName( (Class<?>)object ) + ": " + ((Class<?>)object).getClassLoader() );
		} else {
			log.trace( "Class loader for " + getClassName( object.getClass() ) + ": " + getClassLoader( object ) );
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
