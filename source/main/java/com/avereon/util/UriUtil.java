package com.avereon.util;

import lombok.CustomLog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@CustomLog
public final class UriUtil {

	public static URI addToPath( URI uri, String path ) {
		String newPath = uri.getPath() + "/" + path;
		try {
			return new URI( uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), newPath, uri.getQuery(), uri.getFragment() ).normalize();
		} catch( URISyntaxException exception ) {
			return uri;
		}
	}

	public static String parseName( URI uri ) {
		//		PathUtil.getName( uri.getPath() );
		//		String path = uri.getPath();
		//		if( path.endsWith( "/" ) ) path = path.substring( 0, path.length() - 1 );
		String path = PathUtil.getName( uri.getPath() );
		return path.isEmpty() ? "/" : path;
	}

	public static URI removeQueryAndFragment( URI uri ) {
		if( uri == null ) return null;

		// Return a URI without query or fragment data
		try {
			if( uri.isOpaque() ) {
				return new URI( uri.getScheme(), uri.getSchemeSpecificPart(), null );
			} else {
				return new URI( uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, null );
			}
		} catch( URISyntaxException exception ) {
			log.atSevere().withCause( exception ).log( "Error resolving asset URI: %s", uri );
		}

		return null;
	}

	/**
	 * Resolve an absolute URI from a string. The string may be in any of the following formats: <ul> <li>Absolute URI</li> <li>Relative URI</li> <li>Windows Path
	 * (Windows only)</li> <li>Windows UNC (Windows only)</li> </ul> Every reasonable
	 * attempt is made to create a valid URI from the string. If a valid absolute URI cannot be created directly from the string then a File object is used to
	 * generate a URI based on the string under the following situations: <ul> <li>The URI
	 * is malformed</li> <li>The URI is relative because scheme is missing</li> <li>The URI is a drive letter because the scheme is only one character long </li>
	 * </ul>
	 *
	 * @param string A string to resolve into a URI
	 * @return A new URI based on the specified string.
	 */
	public static URI resolve( String string ) {
		URI uri = null;

		// Try to create a URI directly from the string.
		try {
			uri = new URI( string );
		} catch( URISyntaxException exception ) {
			// Intentionally ignore exception.
		}

		// Catch common URI issues.
		boolean nullUri = uri == null;
		boolean relativeUri = uri != null && !uri.isAbsolute();
		boolean windowsDrive = uri != null && uri.getScheme() != null && uri.getScheme().length() == 1;

		if( nullUri || relativeUri || windowsDrive ) uri = new File( string ).toURI();

		// Canonicalize file URIs.
		if( "file".equals( uri.getScheme() ) ) {
			try {
				uri = new File( uri ).getCanonicalFile().toURI();
			} catch( IOException exception ) {
				log.atSevere().withCause( exception ).log( "Error resolving file URI: %s", uri );
			}
		}

		return uri;
	}

	public static URI resolve( URI uri, URI ref ) {
		if( ref == null ) return null;
		if( uri == null ) return ref;

		Deque<String> queue = new LinkedList<>();

		if( "jar".equals( uri.getScheme() ) ) {
			while( uri.isOpaque() ) {
				queue.add( uri.getScheme() );
				uri = URI.create( uri.getRawSchemeSpecificPart() );
			}
		}

		uri = uri.resolve( ref );

		if( "file".equals( uri.getScheme() ) ) {
			String scheme;
			while( (scheme = queue.pollLast()) != null ) {
				uri = URI.create( scheme + ":" + uri.toString() );
			}
		}

		return uri;
	}

	/**
	 * Does the URI represent a root path.
	 */
	public static boolean isRoot( URI uri ) {
		return PathUtil.isRoot( uri.normalize().getPath() );
	}

	/**
	 * Check if the URI is in normalized form, meaning all "." and ".." parts have
	 * been resolved.
	 *
	 * @param uri The URI to check
	 * @return True if the URI is normalized, false otherwise
	 */
	public static boolean isNormalized( URI uri ) {
		if( uri.isOpaque() ) return true;
		PathUtil.parseNames( uri.getPath() );
		for( String item : PathUtil.parseNames( uri.getPath() ) ) {
			if( PathUtil.PARENT.equals( item ) || PathUtil.SELF.equals( item ) ) return false;
		}
		return true;
	}

	public static boolean hasParent( URI uri ) {
		return !uri.isOpaque() && isNormalized( uri ) && !(uri.equals( getParent( uri ) ));
	}

	/**
	 * Get the parent URI taking into account opaque URI's.
	 *
	 * @param uri The URI from which to get the parent URI
	 * @return The parent URI
	 */
	public static URI getParent( URI uri ) {
		List<String> schemes = new ArrayList<>();

		// Normalize and check for failed normalization
		uri = uri.normalize();
		if( !isNormalized( uri ) ) return uri;

		// Unstack the schemes
		while( uri.isOpaque() ) {
			schemes.add( uri.getScheme() );
			uri = URI.create( uri.getRawSchemeSpecificPart() );
		}

		// Resolve the parent
		if( !isRoot( uri ) ) uri = uri.resolve( uri.getPath().endsWith( PathUtil.SEPARATOR ) ? PathUtil.PARENT : "" );

		// Restack the schemes
		Collections.reverse( schemes );
		for( String scheme : schemes ) {
			uri = URI.create( scheme + ":" + uri.toString() );
		}

		return uri;
	}

	public static String parseFragment( URI uri ) {
		if( uri == null ) return null;
		return parseFragment( uri.getFragment() );
	}

	public static String parseFragment( String fragment ) {
		if( fragment == null ) return null;

		int index = fragment.indexOf( "?" );
		if( index < 0 ) return fragment;

		return fragment.substring( 0, index );
	}

	public static String parseQuery( URI uri ) {
		if( uri == null ) return null;
		return uri.getQuery();
	}

	public static Map<String, String> parseQuery( String query ) {
		System.err.println( "query=" + query );
		if( query == null ) return Map.of();

		Map<String, String> parameters = new HashMap<>();

		String[] values = query.split( "&" );

		for( String value : values ) {
			int index = value.indexOf( "=" );
			if( index < 0 ) {
				parameters.put( value, "true" );
			} else {
				parameters.put( value.substring( 0, index ), value.substring( index + 1 ) );
			}
		}

		return parameters;
	}

	/**
	 * Get a match score between two URIs. The lower the score, the better the
	 * match. A score of 0 is an exact match.
	 *
	 * @param a A URI to compare
	 * @param b A URI to compare
	 * @return A match score with 0 being an exact match and higher number a worse match
	 */
	public static int getMatchScore( URI a, URI b ) {
		List<String> partsA = getParts( a );
		List<String> partsB = getParts( b );

		int min = Math.min( partsA.size(), partsB.size() );
		int max = Math.max( partsA.size(), partsB.size() );

		int index = 0;
		for( ; index < min; index++ ) {
			if( !Objects.equals( partsA.get( index ), partsB.get( index ) ) ) return max - index;
		}

		return max - index;
	}

	static List<String> getParts( URI uri ) {
		List<String> parts = new ArrayList<>();

		if( uri.getScheme() != null ) parts.add( uri.getScheme() );
		if( uri.isOpaque() ) {
			if( uri.getSchemeSpecificPart() != null ) parts.add( uri.getSchemeSpecificPart() );
		} else {
			if( uri.getUserInfo() != null ) parts.add( uri.getUserInfo() );
			if( uri.getHost() != null ) parts.add( uri.getHost() );
			if( uri.getPath() != null ) parts.addAll( Arrays.asList( uri.getPath().split( "/", -1 ) ) );
		}
		if( uri.getFragment() != null ) parts.add( uri.getFragment() );

		return parts;
	}

}
