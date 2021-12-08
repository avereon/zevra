package com.avereon.index;

import lombok.CustomLog;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@CustomLog
public class StandardIndex implements Index {

	private final Map<String, Set<Hit>> index;

	public StandardIndex() {
		this.index = new ConcurrentHashMap<>();
	}

	public Set<String> getDictionary() {
		return new HashSet<>( index.keySet() );
	}

	@Override
	public Set<Hit> getHits( String word ) {
		return index.getOrDefault( word, Set.of() );
	}

	public Index push( Collection<Hit> hits ) {
		hits.forEach( h -> index.computeIfAbsent( h.word(), k -> new CopyOnWriteArraySet<>() ).add( h ) );
		return this;
	}

	@Override
	public int hashCode() {
		return index.hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof StandardIndex) ) return false;
		return index.equals( ((StandardIndex)object).index );
	}

}
