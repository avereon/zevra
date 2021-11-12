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

public class DefaultDocumentParser implements DocumentParser {

	private static final int TAG_PRIORITY = 0;

	private static final int NAME_PRIORITY = 1;

	private static final int CONTENT_PRIORITY = 2;

	private static final int HIGEST_PRIORITY = TAG_PRIORITY;

	private static final int LOWEST_PRIORITY = CONTENT_PRIORITY;

	@Override
	public Result<Set<Hit>> index( Document document ) {
		Set<Hit> hits = new HashSet<>();

		// Add tags
		document.tags().forEach( t -> hits.add( Hit.builder().word( t ).document( document ).priority( TAG_PRIORITY ).build() ) );

		// Add name
		hits.addAll( findHits( document, document.name(), NAME_PRIORITY ) );

		// Add content
		hits.addAll( findHits( document, document.content(), CONTENT_PRIORITY ) );

		return Result.of( hits );
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
