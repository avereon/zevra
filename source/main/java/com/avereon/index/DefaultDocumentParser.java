package com.avereon.index;

import com.avereon.result.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultDocumentParser implements DocumentParser {

	@Override
	public Result<Set<Hit>> index( Document document ) {
		Set<Hit> hits = new HashSet<>();

		// Add tags
		hits.addAll( findHits( document, document.tags(), Hit.TAG_PRIORITY ) );

		// Add name
		hits.addAll( findHits( document, document.title(), Hit.TITLE_PRIORITY ) );

		// Add content
		hits.addAll( findHits( document, document.content(), Hit.CONTENT_PRIORITY ) );

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
				hits.addAll( findContentHits( document, line++, text.trim(), priority ) );
			}
		} catch( IOException exception ) {
			// Intentionally ignore this exception as this should never occur
			return Set.of();
		}
		return hits;
	}

	private List<Hit> findContentHits( Document document, int line, String text, int priority ) {
		List<Hit> hits = new ArrayList<>();

		int point;
		int index = 0;
		int start = -1;
		int length = text.length();
		int endIndex = length - 1;
		String hitText = text.toLowerCase();
		while( index < length ) {
			point = hitText.codePointAt( index );

			// Are we at the last character in the line?
			boolean lastChar = index == endIndex;
			// Is the character a word character?
			boolean wordChar = Character.isLetterOrDigit( point );
			// Is this character the start of a word?
			boolean startOfWord = wordChar && start < 0;
			// Is this character the end of a word?
			boolean endOfWord = start >= 0 && (!wordChar || lastChar);

			// At the start of a word just set the word start index
			if( startOfWord ) start = index;

			// At the end of a word store a hit and reset the word start index
			if( endOfWord ) {
				String word = hitText.substring( start, (lastChar && wordChar) ? length : index );
				hits.add( Hit.builder().document( document ).context( text ).word( word ).line( line ).index( start ).length( word.length() ).priority( priority ).build() );
				start = -1;
			}

			index++;
		}

		return hits;
	}
}
