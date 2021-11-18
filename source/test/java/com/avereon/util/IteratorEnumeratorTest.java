package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class IteratorEnumeratorTest {

	@Test
	void testEnumerator() {
		Set<String> set = Set.of( "red", "orange", "yellow", "green", "blue", "violet" );
		IteratorEnumerator<String> e = new IteratorEnumerator<>( set.iterator() );
		assertThat( set.contains( e.nextElement() ) ).isTrue();
		assertThat( set.contains( e.nextElement() ) ).isTrue();
		assertThat( set.contains( e.nextElement() ) ).isTrue();
		assertThat( set.contains( e.nextElement() ) ).isTrue();
		assertThat( set.contains( e.nextElement() ) ).isTrue();
		assertThat( set.contains( e.nextElement() ) ).isTrue();
		assertThat( e.hasMoreElements() ).isFalse();
	}

}
