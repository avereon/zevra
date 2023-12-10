package com.avereon.index;

import com.avereon.result.Result;
import lombok.CustomLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Deprecated
@CustomLog
public class DefaultDocumentParser implements DocumentParser {

	@Override
	public Result<Set<Hit>> index( Document document ) {
		Set<Hit> hits = new HashSet<>();

		// FIXME Get title of HTML documents...somehow

		// Add tags
		hits.addAll( findHits( document, document.tags(), Hit.TAG_PRIORITY ) );

		// Add name
		hits.addAll( findHits( document, document.title(), Hit.TITLE_PRIORITY ) );

		// Add content
		try {
			hits.addAll( findHits( document, document.reader(), Hit.CONTENT_PRIORITY ) );
		} catch( IOException exception ) {
			log.atError().log( "Error finding index hits", exception );
			//throw new RuntimeException( exception );
		}

		return Result.of( hits );
	}

	private Set<Hit> findHits( Document document, Set<String> content, int priority ) {
		return content.stream().flatMap( t -> findHits( document, t, priority ).stream() ).collect( Collectors.toSet() );
	}

	private Set<Hit> findHits( Document document, String content, int priority ) {
		return findHits( document, new StringReader( content ), priority );
	}

	private Set<Hit> findHits( Document document, Reader content, int priority ) {
		Set<Hit> hits = new HashSet<>();
		try( BufferedReader reader = new BufferedReader( content ) ) {
			String text;
			int line = 0;
			while( (text = reader.readLine()) != null ) {
				final String trimText = text.trim();
				final int finalLine = line;

				// TODO Terms can come from lots of sources:
				// - Plain text
				// - HTML (but not the tags)
				// - XML (but not the tags)
				// - JSON (but not the keys)

				hits.addAll( Terms.split( trimText, ( start, end ) -> {
					int length = end - start;
					String word = trimText.substring( start, end ).toLowerCase();
					return Hit.builder().document( document ).context( trimText ).word( word ).line( finalLine ).index( start ).length( length ).priority( priority ).build();
				} ) );
				line++;
			}
		} catch( IOException exception ) {
			// Intentionally ignore this exception as this should never occur
			return Set.of();
		}
		return hits;
	}

}
