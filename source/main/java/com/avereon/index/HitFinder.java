package com.avereon.index;

import com.avereon.result.Result;
import lombok.CustomLog;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLog
public class HitFinder {

	public Result<Set<Hit>> find( Document document, TermSource source ) {
		Set<Hit> hits = new HashSet<>();

		// Add tags
		hits.addAll( findHits( document, document.tags(), Hit.TAG_PRIORITY ) );

		// Add name
		hits.addAll( findHits( document, document.title(), Hit.TITLE_PRIORITY ) );

		// Add content
		hits.addAll( findHits( document, source, Hit.CONTENT_PRIORITY ) );

		return Result.of( hits );

	}

	@SuppressWarnings("SameParameterValue")
	private Set<Hit> findHits( Document document, Set<String> content, int priority ) {
		return content.stream().flatMap( t -> findHits( document, t, priority ).stream() ).collect( Collectors.toSet() );
	}

	private Set<Hit> findHits( Document document, String content, int priority ) {
		return findHits( document, new TextTermSource( document, content ), priority );
	}

	public Set<Hit> findHits( Document document, TermSource source, int priority ) {
		Set<Hit> hits = new HashSet<>();

		try {
			hits.addAll( source
				.index()
				.map( t -> new Hit().setDocument( document ).setContext( t.context() ).setWord( t.word() ).setLength( t.length() ).coordinates( t.coordinates() ).setPriority( priority ) )
				.toList() );
		} catch( IOException exception ) {
			log.atWarn( exception );
		}

		return hits;
	}

}
