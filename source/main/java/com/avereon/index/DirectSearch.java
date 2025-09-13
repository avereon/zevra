package com.avereon.index;

import com.avereon.result.Result;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The direct search implementation will find hits on for an exact match of the
 * search term on the index. Since all hits are an exact match to the search
 * term, the hits are returned in undefined order, and it is up to the requester
 * to sort the resulting hit documents.
 */
public class DirectSearch implements Search {

	@Override
	public Result<List<Hit>> search( Index index, IndexQuery query ) {
		return Result.of( query.terms().stream().flatMap( t -> index.getHits( t ).stream() ).collect( Collectors.toList() ) );
	}

}
