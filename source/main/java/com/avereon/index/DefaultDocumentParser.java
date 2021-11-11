package com.avereon.index;

import com.avereon.result.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultDocumentParser implements DocumentParser {

	@Override
	public Result<Set<Hit>> parse( Document document ) {
		Set<Hit> hits = new HashSet<>();

		try( BufferedReader reader = new BufferedReader( document.content() ) ) {
			String text;
			int line = 0;
			while( (text = reader.readLine()) != null ) {
				hits.addAll( findHits( document, line++, text.trim() ) );
			}
		} catch( IOException exception ) {
			return Result.of( exception );
		}

		return Result.of( hits );
	}

	private List<Hit> findHits( Document document, int line, String text ) {
		List<Hit> hits = new ArrayList<>();

		char c;
		int index = 0;
		int start = -1;
		int length = text.length();
		int endIndex = length - 1;
		String hitText = text.toLowerCase();
		while( index < length ) {
			c = hitText.charAt( index );

			// Are we at the last character in the line?
			boolean lastChar = index == endIndex;
			// Is the character a word character?
			boolean wordChar = Character.isLetterOrDigit( c );
			// Is this character the start of a word?
			boolean startOfWord = wordChar && start < 0;
			// Is this character the end of a word?
			boolean endOfWord = start >= 0 && (!wordChar || lastChar);

			// At the start of a word just set the word start index
			if( startOfWord ) start = index;

			// At the end of a word store a hit and reset the word start index
			if( endOfWord ) {
				String word = hitText.substring( start, lastChar ? length : index );
				hits.add( Hit.builder().document( document ).context( text ).word( word ).line( line ).index( start ).length( word.length() ).build() );
				start = -1;
			}

			index++;
		}

		return hits;
	}
}
