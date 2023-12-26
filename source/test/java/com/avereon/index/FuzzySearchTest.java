package com.avereon.index;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FuzzySearchTest {

	private final FuzzySearch search = new FuzzySearch( 75 );

	@Test
	void testGetRankPoints() {
		assertThat( search.getRankPoints( "exact", "exact" ) ).isEqualTo( 100 );
		assertThat( search.getRankPoints( "exact", "ex" ) ).isEqualTo( 100 );
		assertThat( search.getRankPoints( "exact", "exam" ) ).isEqualTo( 75 );
		assertThat( search.getRankPoints( "exact", "example" ) ).isEqualTo( 60 );
	}

	@Test
	void ratios() {
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "exact", "exact" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "exact", "ex" ) ).isEqualTo( 57 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "exact", "exam" ) ).isEqualTo( 67 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "exact", "example" ) ).isEqualTo( 50 );

		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "exact", "exact" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "exact", "ex" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "exact", "exam" ) ).isEqualTo( 75 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "exact", "example" ) ).isEqualTo( 60 );

		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "exact", "exact" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "exact", "ex" ) ).isEqualTo( 90 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "exact", "exam" ) ).isEqualTo( 67 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "exact", "example" ) ).isEqualTo( 50 );
	}

	@Test
	void testSearchWithDuplicateSearchTerms() {
		Search search = new FuzzySearch( 80 );
		IndexQuery query = IndexQuery.builder().terms( List.of( "document", "document" ) ).build();
		Index index = new StandardIndex().push( Set.of( new Hit().setWord( "document" ) ) );
		List<Hit> hits = search.search( index, query ).get();
		assertThat( hits.size() ).isEqualTo( 1 );
	}

}
