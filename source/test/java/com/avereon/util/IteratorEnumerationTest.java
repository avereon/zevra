package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IteratorEnumerationTest {

	@Test
	void testEnumerator() {
		Set<String> set = Set.of( "red", "orange", "yellow", "green", "blue", "violet" );
		IteratorEnumeration<String> e = new IteratorEnumeration<>( set.iterator() );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertFalse( e.hasMoreElements() );
	}

}
