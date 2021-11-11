package com.avereon.index;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class FuzzySearchTest {

	private final FuzzySearch search = new FuzzySearch();

	@Test
	void testGetRankPoints() {
		assertThat( search.getRankPoints( "exact", "exact" ), is( 100 ) );
		assertThat( search.getRankPoints( "exact", "exam" ), is( 67 ) );
		assertThat( search.getRankPoints( "exact", "example" ), is( 50 ) );
	}

}
