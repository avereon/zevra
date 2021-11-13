package com.avereon.index;

import com.avereon.result.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The fuzzy search implementation will find hits based off the Levenshtein
 * distance algorithm to find matches of the search term on the index. This will
 * result in an ordered list of hits but it is up to the requester to sort the
 * resulting hit documents.
 */
public class FuzzySearch implements Search {

	/**
	 * Levenshtein distance cutoff distance (0-100). Using zero would return
	 * everything in the index (probably not what we want) and using 100 would
	 * return only exact matches.
	 */
	private static final int CUTOFF = 50;

	private final int cutoff;

	public FuzzySearch() {
		this( CUTOFF );
	}

	public FuzzySearch( int cutoff ) {
		this.cutoff = cutoff;
	}

	@Override
	public Result<List<Hit>> search( Index index, IndexQuery query ) {
		String term = query.text();
		Set<String> dictionary = index.getDictionary();

		List<Rank> ranks = new ArrayList<>();
		dictionary.forEach( w -> {
			int points = getRankPoints( term, w );
			if( points < cutoff ) return;
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
			return this.points - that.points;
		}

	}

}
