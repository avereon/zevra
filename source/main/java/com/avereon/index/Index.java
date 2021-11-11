package com.avereon.index;

import com.avereon.result.Result;
import lombok.CustomLog;

import java.util.*;
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

	public Result<List<Hit>> search( IndexQuery query ) {
		Set<Hit> hits = index.get( query.text() );
		return Result.of( new ArrayList<>(hits) );
	}

	public void push( String word, Hit hit ) {
		index.computeIfAbsent( word, k -> new CopyOnWriteArraySet<>() ).add( hit );
	}

	public void push( Set<Hit> hits ) {
		hits.forEach( h -> index.computeIfAbsent( h.word(), k -> new CopyOnWriteArraySet<>() ).add(h) );
	}

}
