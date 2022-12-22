package com.avereon.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Map;

/**
 * Replaces tokens marked by ${tokenName} with values provided in a value map.
 * <p>
 * For example, using the reader with the following string:
 * <br/>
 * <code>&quot;Hello ${name}!&quot;</code>
 * <br/>
 * ...and the following value map:
 * <table>
 *   <tr>
 *     <th>Name</th>
 *     <th>Value</th>
 *   </tr>
 *   <tr>
 *     <td>name</td>
 *     <td>World</td>
 *   </tr>
 * </table>
 * Would result in: <code>Hello World!</code>
 * </p>
 * <p>
 * Code used and modified from: <a href="https://github.com/jjenkov/TokenReplacingReader">TokenReplacingReader by JJenkov</a>
 * </p>
 */
public class TokenReplacingReader extends Reader {

	private final PushbackReader source;

	private final Map<String, String> values;

	private final StringBuilder tokenName;

	private int tokenValueIndex;

	private String tokenValue;

	public TokenReplacingReader( Reader source, Map<String, String> values ) {
		this.source = new PushbackReader( source );
		this.tokenName = new StringBuilder();
		this.values = values == null ? Map.of() : values;
	}

	public int read() throws IOException {
		// If there is replacement value continue sending it
		if( this.tokenValue != null ) {
			// Not at the end of the replacement value
			if( this.tokenValueIndex < this.tokenValue.length() ) {
				return this.tokenValue.charAt( this.tokenValueIndex++ );
			}

			// At the end of the replacement value
			if( this.tokenValueIndex == this.tokenValue.length() ) {
				this.tokenValue = null;
				this.tokenValueIndex = 0;
			}
		}

		// Read the next character from the source
		int data = this.source.read();

		// If not the start of a token, just return the character
		if( data != '$' ) return data;

		// Read the next character from the source
		data = this.source.read();

		// If not the start of a token, return the prior character
		if( data != '{' ) {
			this.source.unread( data );
			return '$';
		}

		// Clear the token name
		this.tokenName.delete( 0, this.tokenName.length() );

		// Read the token name
		data = this.source.read();
		while( data != '}' ) {
			this.tokenName.append( (char)data );
			data = this.source.read();
		}

		// Get the replacement value
		this.tokenValue = this.values.get( this.tokenName.toString() );

		// Check for null replacement values
		if( this.tokenValue == null ) {
			this.tokenValue = "${" + this.tokenName + "}";
		}

		// Check for empty replacement values
		if( this.tokenValue.length() == 0 ) {
			return read();
		}

		// Return the replacement value's next character
		return this.tokenValue.charAt( this.tokenValueIndex++ );
	}

	public int read( char[] buffer ) throws IOException {
		return read( buffer, 0, buffer.length );
	}

	@Override
	public int read( char[] buffer, int offset, int length ) throws IOException {
		int result = 0;

		for( int i = 0; i < length; i++ ) {
			int next = read();
			if( next == -1 ) {
				if( result == 0 ) result = -1;
				break;
			}
			result = i + 1;
			buffer[ offset + i ] = (char)next;
		}

		return result;
	}

	public int read( CharBuffer target ) throws IOException {
		throw new UnsupportedOperationException();
	}

	public boolean ready() throws IOException {
		return this.source.ready();
	}

	@Override
	public void close() throws IOException {
		this.source.close();
	}

	public boolean markSupported() {
		return false;
	}

	public void mark( int readAheadLimit ) throws IOException {
		throw new UnsupportedOperationException();
	}

	public long skip( long n ) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

}
