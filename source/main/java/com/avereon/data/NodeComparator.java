package com.avereon.data;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class NodeComparator<T extends Node> implements Comparator<T> {

	private List<String> keys;

	public NodeComparator( String... keys ) {
		this.keys = Arrays.asList( keys );
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
