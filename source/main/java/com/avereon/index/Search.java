package com.avereon.index;

import com.avereon.result.Result;

import java.util.List;

public interface Search {

	Result<List<Hit>> search( Index index, IndexQuery query );

}
