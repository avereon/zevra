package com.avereon.index;

import com.avereon.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

public class FuzzySearch implements Search {

	private static final int CUTOFF = 50;

	@Override
	public Result<List<Hit>> search( Index index, IndexQuery query ) {
		String term = query.text();
		Set<String> dictionary = index.getDictionary();

		List<Rank> ranks = new ArrayList<>();
		dictionary.forEach( w -> {
			int points = getRankPoints( term, w );
			if( points < CUTOFF ) return;
			ranks.add( new Rank( w, points ) );
		} );
		Collections.sort( ranks );

		List<Hit> hits = ranks.stream().flatMap( r -> index.getHits( r.word() ).stream() ).collect( Collectors.toList() );

		return Result.of( new ArrayList<>( hits ) );
	}

	/**
	 * Get a percent rank (0-100) of how close two strings match using the
	 * Levenshtein distance algorithm to calculate similarity between strings.
	 * A value of 100 means an exact match.
	 *
	 * @param term Search term
	 * @param word Word to check
	 * @return Percent rank
	 */
	int getRankPoints( String term, String word ) {
		return me.xdrop.fuzzywuzzy.FuzzySearch.ratio( term, word );
	}

	@Data
	@RequiredArgsConstructor
	@Accessors( fluent = true )
	private static class Rank implements Comparable<Rank> {

		private final String word;

		private final int points;

		@Override
		public int compareTo( Rank that ) {
			return that.points - this.points;
		}

	}

}
