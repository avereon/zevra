package com.avereon.index;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FuzzySearchTest {

	private final FuzzySearch search = new FuzzySearch();

	@Test
	void testGetRankPoints() {
		assertThat( search.getRankPoints( "exact", "exact" ) ).isEqualTo( 100 );
		assertThat( search.getRankPoints( "exact", "exam" ) ).isEqualTo( 67 );
		assertThat( search.getRankPoints( "exact", "example" ) ).isEqualTo( 50 );
	}

}
