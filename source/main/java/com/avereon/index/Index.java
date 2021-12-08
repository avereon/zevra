package com.avereon.index;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public interface Index {

	String DEFAULT = "default";

	Set<String> getDictionary();

	Set<Hit> getHits( String word );

	Index push( Collection<Hit> hits );

	default Set<Hit> getHits() {
		return getDictionary().stream().flatMap( t -> getHits( t ).stream() ).collect( Collectors.toSet() );
	}

	static Index merge( Collection<Index> indexes ) {
		return indexes.stream().reduce( new StandardIndex(), Index::merge );
	}

	static StandardIndex merge( Index a, Index b ) {
		StandardIndex merged = new StandardIndex();
		merged.push( a.getHits() );
		merged.push( b.getHits() );
		return merged;
	}

}
