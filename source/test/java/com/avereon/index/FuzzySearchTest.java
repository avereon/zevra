package com.avereon.index;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FuzzySearchTest {

	@Test
	void testGetRankPoints() {
		//		FuzzySearch search = new FuzzySearch( 75 );
		//		assertThat( search.getRankPoints( "exact", "exact" ) ).isEqualTo( 100 );
		//		assertThat( search.getRankPoints( "exact", "ex" ) ).isEqualTo( 100 );
		//		assertThat( search.getRankPoints( "exact", "exam" ) ).isEqualTo( 75 );
		//		assertThat( search.getRankPoints( "exact", "example" ) ).isEqualTo( 60 );
		//
		//		assertThat( search.getRankPoints( "line", "in" )).isEqualTo( 100 );
		//		assertThat( search.getRankPoints( "in", "line" )).isEqualTo( 100 );
	}

	@Test
	void ratios() {
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "exact", "exact" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "exact", "ex" ) ).isEqualTo( 57 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "exact", "exam" ) ).isEqualTo( 67 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "exact", "example" ) ).isEqualTo( 50 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "line", "in" ) ).isEqualTo( 67 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "in", "line" ) ).isEqualTo( 67 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "line", "linea" ) ).isEqualTo( 89 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "line", "línea" ) ).isEqualTo( 67 );

		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "exact", "exact" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "exact", "ex" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "exact", "exam" ) ).isEqualTo( 75 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "exact", "example" ) ).isEqualTo( 60 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "line", "in" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "in", "line" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "line", "linea" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "line", "línea" ) ).isEqualTo( 75 );

		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "exact", "exact" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "exact", "ex" ) ).isEqualTo( 90 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "exact", "exam" ) ).isEqualTo( 67 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "exact", "example" ) ).isEqualTo( 50 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "line", "in" ) ).isEqualTo( 90 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "in", "line" ) ).isEqualTo( 90 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "line", "linea" ) ).isEqualTo( 89 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "line", "línea" ) ).isEqualTo( 67 );

		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.ratio( "arc", "marcador" ) ).isEqualTo( 55 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( "arc", "marcador" ) ).isEqualTo( 100 );
		assertThat( me.xdrop.fuzzywuzzy.FuzzySearch.weightedRatio( "arc", "marcador" ) ).isEqualTo( 90 );
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
