package com.avereon.data;

/**
 * A functional interface for filtering data nodes.
 *
 * @param <T> The data node type
 */
public interface DataFilter<T> {

	/**
	 * Test the specified node.
	 *
	 * @param node The node to test.
	 * @return True if the node should be accepted, false otherwise.
	 */
	boolean accept( T node );

}
