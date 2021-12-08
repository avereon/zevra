package com.avereon.index;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TermsTest {

	@Test
	void testSplit() {
		assertThat( Terms.split( "a b c" ) ).contains( "a", "b", "c" );
	}

	@Test
	void testSplitWithNull() {
		assertThatThrownBy( () -> Terms.split( null ) ).isInstanceOf( NullPointerException.class );
	}

	@Test
	void testSplitWithEmpty() {
		assertThat( Terms.split( "" ) ).isEmpty();
	}

	@Test
	void testSplitWithWhitespace() {
		assertThat( Terms.split( " a, b, c\n" ) ).contains( "a", "b", "c" );
	}

}
