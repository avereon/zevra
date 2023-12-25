package com.avereon.index;

import com.avereon.result.Result;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The fuzzy search implementation will find hits based off the Levenshtein
 * distance algorithm to find matches of the search term on the index. This will
 * result in an ordered list of hits, but it is up to the requester to sort the
 * resulting hit documents.
 */
public class FuzzySearch implements Search {

	/**
	 * Levenshtein distance cutoff value (0-100). Using zero would return
	 * everything in the index (probably not desired) and using 100 would
	 * return only exact matches.
	 */
	private static final int DEFAULT_CUTOFF = 80;

	private final int cutoff;

	public FuzzySearch() {
		this( DEFAULT_CUTOFF );
	}

	public FuzzySearch( int cutoff ) {
		this.cutoff = cutoff;
	}

	@Override
	public Result<List<Hit>> search( Index index, IndexQuery query ) {
		List<HitMatchWrapper> hits = new ArrayList<>();

		for( String term : query.terms() ) {
			List<HitMatchWrapper> termHits = HitMatchWrapper.wrap( search( index, term ).get() );
			if( hits.isEmpty() ) {
				hits.addAll( termHits );
			} else {
				hits.retainAll( termHits );
			}
		}

		return Result.of( HitMatchWrapper.unwrap( hits ) );
	}

	private Result<List<Hit>> search( Index index, String term ) {
		Set<String> dictionary = index.getDictionary();

		List<Hit> hits = new ArrayList<>();
		dictionary.forEach( word -> {
			int points = getRankPoints( term, word );
			if( points < cutoff ) return;
			hits.addAll( index.getHits( word ).stream().map( h -> h.points( points )).toList());
		} );
		hits.sort( new HitSort() );

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
		return me.xdrop.fuzzywuzzy.FuzzySearch.partialRatio( term, word );
	}

	private static class HitSort implements Comparator<Hit> {

		@Override
		public int compare( Hit hit1, Hit hit2 ) {
			int diff = hit2.points() - hit1.points();
			if( diff != 0 ) return diff;
			return hit2.priority() - hit1.priority();
		}

	}

	/**
	 * This hit wrapper allows fuzzy search to compare hits only on some fields
	 * of {@link Hit}, instead of all fields, which is the default.
	 */
	private static class HitMatchWrapper {

		private final Hit hit;

		private HitMatchWrapper( Hit hit ) {
			this.hit = hit;
		}

		public int hashCode() {
			return this.hit.document().hashCode();
		}

		public boolean equals( Object object ) {
			if( !(object instanceof HitMatchWrapper) ) return false;
			HitMatchWrapper that = (HitMatchWrapper)object;

			//if( !Objects.equals( this.hit.priority(), that.hit.priority() ) ) return false;
			//if( !Objects.equals( this.hit.line(), that.hit.line() ) ) return false;
			//if( !Objects.equals( this.hit.context(), that.hit.context() ) ) return false;
			return Objects.equals( this.hit.document(), that.hit.document() );
		}

		static List<HitMatchWrapper> wrap( List<Hit> hits ) {
			return hits.stream().map( HitMatchWrapper::new ).collect( Collectors.toList() );
		}

		static List<Hit> unwrap( List<HitMatchWrapper> wrappers ) {
			return wrappers.stream().map( w -> w.hit ).collect( Collectors.toList() );
		}
	}

}
