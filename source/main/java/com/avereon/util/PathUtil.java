package com.avereon.util;

import java.util.ArrayList;
import java.util.List;

/**
 * The PathUtil class provides utility methods for working with file paths.
 */
public class PathUtil {

	public static final String SEPARATOR = "/";

	public static final String PARENT = "..";

	public static final String SELF = ".";

	private static final String DOUBLE_SEPARATOR = SEPARATOR + SEPARATOR;

	public static final String EMPTY = "";

	public static final String ROOT = "/";

	/**
	 * Checks if the given path is an absolute path.
	 *
	 * @param path The path to check
	 * @return True if the path is an absolute path, otherwise false
	 */
	public static boolean isAbsolute( String path ) {
		return path != null && path.startsWith( SEPARATOR );
	}

	/**
	 * Checks if the given path is a relative path.
	 *
	 * @param path The path to check
	 * @return True if the path is a relative path, otherwise false
	 */
	public static boolean isRelative( String path ) {
		return path != null && !isAbsolute( path );
	}

	/**
	 * Checks if the given path is the root path.
	 *
	 * @param path The path to check
	 * @return True if the path is the root path, otherwise false
	 */
	public static boolean isRoot( String path ) {
		return SEPARATOR.equals( path );
	}

	/**
	 * Get the name (last element) in the path.
	 *
	 * @param path The path
	 * @return The name
	 */
	public static String getName( String path ) {
		if( path == null ) return null;
		String[] names = parseNames( path );
		return names[ names.length - 1 ];
	}

	/**
	 * <p>
	 * Resolve a new path string from a root with an additional path.
	 * </p>
	 * <p>
	 * Examples:
	 * <ul>
	 *   <li>resolve( "", "" ) -> ""</li>
	 *   <li>resolve( "/root", "path" ) -&gt; "/root/path"</li>
	 *   <li>resolve( "/root/", "path" ) -&gt; "/root/path"</li>
	 *   <li>resolve( "/root", "/path" ) -&gt; "/path"</li>
	 *   <li>resolve( "root", "path" ) -&gt; "root/path"</li>
	 * </ul>
	 * </p>
	 * <p>
	 * NOTE: This method does not normalize the path.
	 * </p>
	 *
	 * @param root The root path to start with
	 * @param path The path to resolve against the root
	 * @return The new resolved path
	 */
	public static String resolve( String root, String path ) {
		if( root == null || path == null ) return null;

		if( EMPTY.equals( root ) ) return path;
		if( EMPTY.equals( path ) ) return root;
		if( isAbsolute( path ) ) return path;

		return root.endsWith( SEPARATOR ) ? root + path : root + SEPARATOR + path;
	}

	/**
	 * Returns the parent directory path of the given path.
	 * <p>
	 * NOTE: This method does not normalize the path.
	 *
	 * @param path The path
	 * @return The parent directory path, or null if the input is null or the path has no parent
	 */
	public static String getParent( String path ) {
		path = cleanTrailingSeparator( path );
		if( path == null ) return null;
		if( isRoot( path ) ) return null;
		if( EMPTY.equals( path ) ) return null;
		int index = path.lastIndexOf( SEPARATOR );
		if( index == 0 ) return SEPARATOR;
		if( index < 0 ) return EMPTY;
		return path.substring( 0, index );
	}


	/**
	 * Normalize a given path by removing redundant separators, resolving parent
	 * references, and cleaning trailing separators.
	 *
	 * @param path The path to normalize
	 * @return The normalized path, or null if the input is null
	 */
	public static String normalize( String path ) {
		if( path == null ) return null;
		return cleanTrailingSeparator( normalizeSeparators( normalizeParents( path ) ) );
	}

	/**
	 * Calculates the relative path from a source path to a target path.
	 *
	 * @param source The source path
	 * @param target The target path
	 * @return The relative path from source to target, or null if either source or target is null
	 * @throws IllegalArgumentException If target is a different type of path (absolute or relative) than source
	 */
	public static String relativize( String source, String target ) {
		if( source == null || target == null ) return null;

		if( target.equals( source ) ) {
			return EMPTY;
		} else if( isAbsolute( source ) != isAbsolute( target ) ) {
			throw new IllegalArgumentException( "Target is different type of path" );
		} else if( EMPTY.equals( source ) ) {
			return target;
		} else {
			String[] sourceNames = parseNames( source );
			String[] targetNames = parseNames( target );
			int sourceCount = sourceNames.length;
			int targetCount = targetNames.length;
			int minimumCount = Math.min( sourceCount, targetCount );

			int matchCount = 0;
			while( matchCount < minimumCount && sourceNames[ matchCount ].equals( targetNames[ matchCount ] ) ) matchCount++;

			if( matchCount < targetCount ) {
				String subpath = subpath( targetNames, matchCount, targetCount );
				if( sourceCount == matchCount ) {
					return subpath;
				} else {
					int sourceIndex = matchCount;
					StringBuilder builder = new StringBuilder();
					while( sourceIndex < sourceCount ) {
						builder.append( PARENT );
						builder.append( SEPARATOR );
						sourceIndex++;
					}
					int targetIndex = matchCount;
					while( targetIndex < targetCount ) {
						if( targetIndex > matchCount ) builder.append( SEPARATOR );
						builder.append( targetNames[ targetIndex ] );
						targetIndex++;
					}

					return builder.toString();
				}
			} else {
				int index = matchCount;
				StringBuilder builder = new StringBuilder();
				while( index < sourceCount ) {
					if( index > matchCount ) builder.append( SEPARATOR );
					builder.append( PARENT );
					index++;
				}

				return builder.toString();
			}
		}
	}

	/**
	 * Returns the child path of a given root path and target path.
	 *
	 * @param root The root path
	 * @param path The target path
	 * @return The child path, or null if either root or path is null
	 */
	public static String getChild( String root, String path ) {
		if( root == null || path == null ) return null;
		String child = relativize( root, path );
		int index = child.indexOf( SEPARATOR );
		return index < 0 ? child : child.substring( 0, index );
	}

	/**
	 * Split a given path into an array of individual names.
	 *
	 * @param path The path to split
	 * @return An array of individual names in the path
	 */
	public static String[] split( String path ) {
		return parseNames( path );
	}

	/**
	 * Split a given path into an array of individual names.
	 *
	 * @param path The path to split
	 * @return An array of individual names in the path
	 */
	static String[] parseNames( String path ) {
		if( path == null ) return null;
		if( EMPTY.equals( path ) ) return new String[]{ EMPTY };

		List<String> names = new ArrayList<>();
		int separatorLength = SEPARATOR.length();

		int lastIndex = 0;
		int index = path.indexOf( SEPARATOR, lastIndex );
		if( index == 0 ) {
			names.add( SEPARATOR );
			lastIndex = index + separatorLength;
			index = path.indexOf( SEPARATOR, lastIndex );
		}
		while( index > -1 ) {
			names.add( path.substring( lastIndex, index ) );
			lastIndex = index + separatorLength;
			index = path.indexOf( SEPARATOR, lastIndex );
		}

		if( path.length() > lastIndex ) names.add( path.substring( lastIndex ) );

		return names.toArray( new String[ 0 ] );
	}

	/**
	 * Constructs a subpath from an array of names, starting from the startIndex
	 * (inclusive) and ending at the endIndex (exclusive).
	 *
	 * @param names      The array of names
	 * @param startIndex The starting index
	 * @param endIndex   The ending index (exclusive)
	 * @return The constructed subpath
	 */
	private static String subpath( String[] names, int startIndex, int endIndex ) {
		StringBuilder builder = new StringBuilder();
		for( int index = startIndex; index < endIndex; index++ ) {
			if( index > startIndex ) builder.append( SEPARATOR );
			builder.append( names[ index ] );
		}
		return builder.toString();
	}

	/**
	 * Cleans the trailing separator from a given path.
	 *
	 * @param path The path to clean
	 * @return The path with the trailing separator removed, or null if the input is null
	 */
	private static String cleanTrailingSeparator( String path ) {
		// Null path
		if( path == null ) return null;

		// Root path
		if( SEPARATOR.equals( path ) ) return path;

		// Path with trailing separator
		if( path.endsWith( SEPARATOR ) ) return path.substring( 0, path.length() - SEPARATOR.length() );

		return path;
	}

	/**
	 * Normalizes a given path by removing redundant separators.
	 *
	 * @param path The path to normalize
	 * @return The normalized path, or null if the input is null
	 */
	private static String normalizeSeparators( String path ) {
		if( path == null ) return null;
		while( path.contains( DOUBLE_SEPARATOR ) ) {
			path = path.replace( DOUBLE_SEPARATOR, SEPARATOR );
		}
		return path;
	}

	/**
	 * Normalize a given path by removing redundant separators, resolving parent
	 * references, and cleaning trailing separators.
	 *
	 * @param path The path to normalize
	 * @return The normalized path, or null if the input is null
	 */
	private static String normalizeParents( String path ) {
		if( path == null ) return null;
		String search = SEPARATOR + PARENT;
		int searchLength = search.length();
		int index = 0;
		String newPath = path;
		while( (index = newPath.indexOf( search, index )) > -1 ) {
			newPath = getParent( path.substring( 0, index ) );
			if( newPath == null ) return null;
			newPath += path.substring( index + searchLength );
		}
		return newPath;
	}

}
