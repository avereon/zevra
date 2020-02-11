package com.avereon.data;

public interface DataFilter<T> {

	/**
	 * Test the specified node.
	 *
	 * @param node The node to test.
	 * @return True if the node should be accepted, false otherwise.
	 */
	boolean accept( T node );

}
