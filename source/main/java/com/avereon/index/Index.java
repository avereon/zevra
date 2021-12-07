package com.avereon.index;

import lombok.CustomLog;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@CustomLog
public class Index {

	public static final String DEFAULT = "default";

	private final Map<String, Set<Hit>> index;

	public Index() {
		this.index = new ConcurrentHashMap<>();
	}

	public Set<String> getDictionary() {
		return new HashSet<>( index.keySet() );
	}

	public Set<Hit> getHits( String word ) {
		return index.getOrDefault( word, Set.of() );
	}

	public void push( String word, Hit hit ) {
		index.computeIfAbsent( word, k -> new CopyOnWriteArraySet<>() ).add( hit );
	}

	public void push( Set<Hit> hits ) {
		hits.forEach( h -> index.computeIfAbsent( h.word(), k -> new CopyOnWriteArraySet<>() ).add( h ) );
	}

	public static Index merge( Index a, Index b ) {
		Index merged = new Index();

		// Go through a and add
		for( String term : a.index.keySet() ) {
			merged.index.put( term, new HashSet<>( a.index.get( term ) ) );
		}

		// Go through b and merge
		for( String term : b.index.keySet() ) {
			Set<Hit> hits = merged.index.get( term );
			if( hits == null ) {
				merged.index.put( term, new HashSet<>( b.index.get( term ) ) );
			} else {
				hits.addAll( b.index.get( term ) );
			}
		}

		return merged;
	}

}
