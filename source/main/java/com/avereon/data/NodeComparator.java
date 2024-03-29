package com.avereon.data;

import java.util.Comparator;
import java.util.List;

/**
 * A {@link Comparator} for {@link Node nodes}. The comparator is constructed
 * with the keys that should be compared.
 *
 * @param <T> The node type
 */
public class NodeComparator<T extends Node> implements Comparator<T> {

	private final List<String> keys;

	public NodeComparator( String... keys ) {
		this( List.of( keys ) );
	}

	public NodeComparator( List<String> keys ) {
		this.keys = keys;
	}

	@Override
	public int compare( T a, T b ) {
		for( String key : keys ) {
			Object valueA = a.getValue( key );
			Object valueB = b.getValue( key );
			if( valueA == null && valueB != null ) return 1;
			if( valueA != null && valueB == null ) return -1;

			int comparison = String.valueOf( valueA ).compareTo( String.valueOf( valueB ) );
			if( comparison != 0 ) return comparison;
		}
		return 0;
	}

}
