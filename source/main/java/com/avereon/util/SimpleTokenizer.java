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

	/**
	 * The SimpleTokenizer class provides a simple tokenization functionality for a given string.
	 */
	public SimpleTokenizer( String string ) {
		this.string = string;
		this.count = string.length();
	}

	/**
	 * Retrieves a list of tokens from the string.
	 *
	 * @return A list of tokens extracted from the string, or an empty list if no tokens are found.
	 */
	public List<String> getTokens() {
		List<String> tokens = new ArrayList<>();

		String token;
		while( (token = nextToken()) != null ) {
			tokens.add( token );
		}

		return tokens;
	}

	/**
	 * Retrieves the next token from the string.
	 * Tokens are delimited by whitespace unless enclosed in double quotes.
	 * If there are no more tokens, null is returned.
	 *
	 * @return The next token as a String, or null if there are no more tokens.
	 */
	public String nextToken() {
		int start = seekTokenStart();
		if( start == count ) return null;
		int end = seekTokenEnd();

		return cleanToken( string.substring( start, end ) );
	}

	/**
	 * Seeks the start index of the next token in the string.
	 *
	 * @return The index of the start of the next token.
	 */
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

	/**
	 * Seeks the end index of the current token in the string.
	 *
	 * @return The index of the end of the current token.
	 */
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

	/**
	 * Cleans a token by replacing escaped double quotes with actual double quotes.
	 *
	 * @param token The token to be cleaned.
	 * @return The cleaned token with escaped double quotes replaced by actual double quotes.
	 */
	private String cleanToken( String token ) {
		return token.replace( "\\\"", "\"" );
	}

}
