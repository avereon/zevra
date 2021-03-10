package com.avereon.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class MapUtil {

	public static <K, V> Map<V, K> mirror( Map<K, V> map ) {
		Map<V, K> mirror = new HashMap<>();

		for( K key : map.keySet() ) {
			mirror.put( map.get( key ), key );
		}

		return mirror;
	}

	@SuppressWarnings( "unchecked" )
	public static <T> Stream<T> flatten( Map<String, ?> map, String valueKey, String treeKey ) {
		return MapUtil.flatten( map, m -> {
			T value = (T)m.get( valueKey );
			return value == null ? Stream.of() : 	Stream.of( value );
		}, m -> {
			Map<String, ?> children = (Map<String, ?>)m.get( treeKey );
			return children == null ? Map.of() : children;
		} );
	}

	@SuppressWarnings( "unchecked" )
	public static <T> Stream<T> flatten( Map<String, ?> map, Function<Map<String, ?>, Stream<T>> extractor, Function<Map<String, ?>, Map<String, ?>> flattener ) {
		return Stream.concat( extractor.apply( map ), flattener.apply( map ).values().stream().flatMap( v -> flatten( (Map<String, ?>)v, extractor, flattener ) ) );
	}

}
