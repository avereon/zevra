package com.avereon.util;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {

	public static <K, V> Map<V, K> mirror( Map<K, V> map ) {
		Map<V, K> mirror = new HashMap<>();

		for( K key : map.keySet() ) {
			mirror.put( map.get( key ), key );
		}

		return mirror;
	}

}
