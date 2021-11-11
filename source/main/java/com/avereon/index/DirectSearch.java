package com.avereon.index;

import com.avereon.result.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DirectSearch implements Search {

	@Override
	public Result<List<Hit>> search( Index index, IndexQuery query ) {
		Set<Hit> hits = index.getHits( query.text() );
		return Result.of( new ArrayList<>(hits) );
	}

}
