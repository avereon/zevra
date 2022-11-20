package com.avereon.util;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public class TemplateReader extends Reader {

	private final Reader source;

	private final Map<String, String> values;

	public TemplateReader( Reader source, Map<String,String> values ) {
		this.source = source;
		this.values = values;
	}

	@Override
	public int read( char[] buffer, int offset, int length ) throws IOException {
		int result = source.read( buffer, offset, length );

		// TODO Replace tokens with values

		return result;
	}

	@Override
	public void close() throws IOException {
		source.close();
	}

}
