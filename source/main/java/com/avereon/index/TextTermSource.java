package com.avereon.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TextTermSource implements TermSource {

	private final Document document;

	private final String text;

	public TextTermSource( Document document ) {
		this( document, null );
	}

	public TextTermSource( Document document, String text ) {
		this.document = document;
		this.text = text;
	}

	@Override
	public Stream<Term> index() throws IOException {
		return getTerms( text == null ? document.reader() : new StringReader( text ) ).stream();
	}

	private List<Term> getTerms( Reader source ) throws IOException {
		List<Term> terms = new ArrayList<>();

		try( BufferedReader reader = new BufferedReader( source ) ) {
			String text;
			int lineIndex = 0;
			while( (text = reader.readLine()) != null ) {
				final String line = text.trim();
				final int finalLineIndex = lineIndex;

				terms.addAll( Terms.split( line, ( start, end ) -> {
					int length = end - start;
					String word = line.substring( start, end ).toLowerCase();
					return new Term( line, word, length, List.of( finalLineIndex, start ) );
				} ) );

				lineIndex++;
			}
		}

		return terms;
	}

}
