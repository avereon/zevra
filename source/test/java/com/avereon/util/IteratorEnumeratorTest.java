package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IteratorEnumeratorTest {

	@Test
	void testEnumerator() {
		Set<String> set = Set.of( "red", "orange", "yellow", "green", "blue", "violet" );
		IteratorEnumerator<String> e = new IteratorEnumerator<>( set.iterator() );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertTrue( set.contains( e.nextElement() ) );
		assertFalse( e.hasMoreElements() );
	}

}
