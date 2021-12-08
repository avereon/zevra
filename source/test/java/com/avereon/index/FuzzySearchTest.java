package com.avereon.index;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FuzzySearchTest {

	private final FuzzySearch search = new FuzzySearch();

	@Test
	void testGetRankPoints() {
		assertThat( search.getRankPoints( "exact", "exact" ) ).isEqualTo( 100 );
		assertThat( search.getRankPoints( "exact", "exam" ) ).isEqualTo( 67 );
		assertThat( search.getRankPoints( "exact", "example" ) ).isEqualTo( 50 );
	}

	@Test
	void testSearchWithDuplicateSearchTerms() {
		Search search = new FuzzySearch( 80 );
		IndexQuery query = IndexQuery.builder().terms( List.of( "document", "document" ) ).build();
		Index index = new StandardIndex().push( Set.of( Hit.builder().word( "document" ).build() ) );
		List<Hit> hits = search.search( index, query ).get();
		assertThat( hits.size() ).isEqualTo( 1 );
	}

}
