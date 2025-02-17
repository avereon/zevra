package com.avereon.util;

import lombok.CustomLog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class for working with URIs.
 */
@CustomLog
public final class UriUtil {

	/**
	 * Convenience method to URL encode a string using UTF-8 encoding.
	 *
	 * @param string The string to encode
	 * @return The encoded string
	 */
	public static String encode( String string ) {
		return URLEncoder.encode( string, StandardCharsets.UTF_8 );
	}

	/**
	 * Convenience method to URL decode a string using UTF-8 encoding.
	 *
	 * @param string The string to decode
	 * @return The decoded string
	 */
	public static String decode( String string ) {
		return URLDecoder.decode( string, StandardCharsets.UTF_8 );
	}

	/**
	 * Appends a path segment to the given URI and returns a new normalized URI.
	 *
	 * @param uri The original URI to which the path segment is to be appended.
	 * @param path The path segment to be appended to the URI.
	 * @return A new normalized URI with the path segment appended. If the URI
	 * cannot be parsed, the original URI is returned.
	 */
	public static URI addToPath( URI uri, String path ) {
		String newPath = uri.getPath() + "/" + path;
		try {
			return new URI( uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), newPath, uri.getQuery(), uri.getFragment() ).normalize();
		} catch( URISyntaxException exception ) {
			return uri;
		}
	}

	/**
	 * Parses the name from the path of the given URI.
	 *
	 * @param uri the URI whose path's name needs to be parsed
	 * @return the parsed name from the path of the given URI
	 */
	public static String parseName( URI uri ) {
		String path = PathUtil.getName( uri.getPath() );
		return path.isEmpty() ? "/" : path;
	}

	/**
	 * Removes the fragment data from the given URI.
	 *
	 * @param uri The URI object from which the fragment should be removed.
	 * @return A new URI object without the fragment, or null if the input URI is
	 * null or an error occurred while removing the fragment.
	 */
	public static URI removeFragment( URI uri ) {
		if( uri == null ) return null;

		// Return a URI without query or fragment data
		try {
			if( uri.isOpaque() ) {
				return new URI( uri.getScheme(), uri.getSchemeSpecificPart(), null );
			} else {
				return new URI( uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), null );
			}
		} catch( URISyntaxException exception ) {
			log.atSevere().withCause( exception ).log( "Error resolving asset URI: %s", uri );
		}

		return null;
	}

	/**
	 * Removes the query and fragment from the given URI.
	 *
	 * @param uri The URI from which to remove the query and fragment. Cannot be null.
	 * @return The modified URI without the query and fragment. Returns null if
	 * the input URI is null or if there is an error resolving the URI.
	 */
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
			uri = new URI( string.replace( " ", "%20" ) );
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

	/**
	 * Convenience method to resolve a string against a URI. This method URL
	 * encodes the string before resolving it.
	 *
	 * @param uri The URI to resolve against
	 * @param string The string to resolve
	 * @return The resolved URI
	 */
	public static URI resolve( URI uri, String string ) {
		return uri.resolve( encode( string ) );
	}

	/**
	 * Convenience method to resolve a string against a URI and return the result
	 * as a string. This method URL encodes the string before resolving it and
	 * URL decodes the result before returning it.
	 *
	 * @param uri The URI to resolve against
	 * @param string The string to resolve
	 * @return The resolved URI as a string
	 */
	public static String resolveToString( URI uri, String string ) {
		return decode( resolve( uri, string ).toString() );
	}

	/**
	 * Resolves the given relative URI against the given base URI. The base URI
	 * can be either absolute or relative. If the reference URI is null, null is
	 * returned. If the base URI is null, the reference URI is returned without
	 * any change. This method supports resolving URIs with the "jar" scheme. If
	 * the base URI has the "jar" scheme, it resolves the URI by adding the scheme
	 * to a queue, removing the scheme from the URI, and then using the resolved
	 * URI for further resolution. After resolving the URI against the base URI,
	 * if the resulting URI has the "file" scheme, it prepends the schemes from
	 * the queue in reverse order to the resulting URI and returns it.
	 *
	 * @param uri the base URI against which the reference URI is resolved
	 * @param ref the reference URI to be resolved against the base URI
	 * @return the resolved URI
	 */
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
				uri = URI.create( scheme + ":" + uri );
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

	/**
	 * Checks whether the given URI has a parent.
	 *
	 * @param uri The URI to be checked for parent.
	 * @return {@code true} if the URI has a parent, {@code false} otherwise.
	 */
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

		// Re-stack the schemes
		Collections.reverse( schemes );
		for( String scheme : schemes ) {
			uri = URI.create( scheme + ":" + uri );
		}

		return uri;
	}

	/**
	 * Parses the fragment data of a given URI.
	 *
	 * @param uri The URI from which to parse the fragment data.
	 * @return The fragment data of the URI, or null if the URI is null or does not have a fragment.
	 */
	public static String parseFragment( URI uri ) {
		if( uri == null ) return null;
		return uri.getFragment();
	}

	/**
	 * Parses the query from the given URI.
	 *
	 * @param uri the URI from which to parse the query
	 * @return the query string, or null if the URI is null
	 */
	public static String parseQuery( URI uri ) {
		if( uri == null ) return null;
		return uri.getQuery();
	}

	/**
	 * Parses the given query string and returns a map of query parameters.
	 *
	 * @param query the query string to parse
	 * @return a map of query parameters
	 */
	public static Map<String, String> parseQuery( String query ) {
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

	/**
	 * Returns a list of parts extracted from the given URI.
	 *
	 * @param uri the URI from which to extract the parts
	 * @return a list of parts extracted from the URI
	 */
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
