package com.avereon.util;

import java.util.ArrayList;
import java.util.List;

/**
 * The SimpleTokenizer class provides a simple tokenization functionality for a given string.
 */
public class SimpleTokenizer {

	private final String string;

	private final int count;

	private int index;

	private boolean quoted;

	public SimpleTokenizer( String string ) {
		this.string = string;
		this.count = string.length();
	}

	public List<String> getTokens() {
		List<String> tokens = new ArrayList<>();

		String token;
		while( (token = nextToken()) != null ) {
			tokens.add( token );
		}

		return tokens;
	}

	public String nextToken() {
		int start = seekTokenStart();
		if( start == count ) return null;
		int end = seekTokenEnd();

		return cleanToken( string.substring( start, end ) );
	}

	private int seekTokenStart() {
		while( index < count && Character.isWhitespace( string.charAt( index ) ) ) {
			index++;
		}

		if( index < count && string.charAt( index ) == '"' ) {
			quoted = true;
			index++;
		}

		return index;
	}

	private int seekTokenEnd() {
		while( index < count && (!Character.isWhitespace( string.charAt( index ) ) || quoted) ) {
			if( string.charAt( index ) == '\\' ) {
				index++;
			} else if( string.charAt( index ) == '"' ) {
				quoted = false;
				index++;
				return index - 1;
			}
			index++;
		}

		return index;
	}

	private String cleanToken( String token ) {
		return token.replace( "\\\"", "\"" );
	}

}
