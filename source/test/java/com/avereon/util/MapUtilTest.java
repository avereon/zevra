package com.avereon.util;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;

public class MapUtilTest {

	@Test
	void testFlatten() {
		Map<String, Object> a = new HashMap<>();
		Map<String, Object> b = new HashMap<>();
		Map<String, Object> c = new HashMap<>();
		Map<String, Object> d = new HashMap<>();
		Map<String, Object> e = new HashMap<>();

		a.put( "value", "a" );
		b.put( "value", "b" );
		c.put( "value", "c" );
		d.put( "value", "d" );
		e.put( "value", "e" );

		a.put( "children", Map.of( "b", b, "c", c ) );
		c.put( "children", Map.of( "d", d, "e", e ) );

		Set<Object> values = MapUtil.flatten( a, "value", "children" ).collect( Collectors.toSet() );

		MatcherAssert.assertThat( values, containsInAnyOrder( "a", "b", "c", "d", "e" ) );
	}

}
