package com.avereon.index;

import com.avereon.result.Result;

import java.util.Set;

public interface DocumentParser {

	Result<Set<Hit>> parse( Document document );

}
