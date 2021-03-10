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

	@SuppressWarnings( { "unchecked" } )
	public static <K, V> Stream<V> flatten( Map<K, ?> map, K valueKey, K treeKey ) {
		return MapUtil.flatTreeMap( map, m -> {
			V value = (V)m.get( valueKey );
			return value == null ? Stream.of() : Stream.of( value );
		}, m -> {
			Map<K, ?> children = (Map<K, ?>)m.get( treeKey );
			return children == null ? Map.of() : children;
		} );
	}

	@SuppressWarnings( "unchecked" )
	public static <K, V> Stream<V> flatTreeMap( Map<K, ?> map, Function<Map<K, ?>, Stream<V>> extractor, Function<Map<K, ?>, Map<K, ?>> flattener ) {
		return Stream.concat( extractor.apply( map ), flattener.apply( map ).values().stream().flatMap( v -> flatTreeMap( (Map<K, ?>)v, extractor, flattener ) ) );
	}

}
