package com.avereon.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.LongConsumer;

public class IoUtil {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	public static String toString( InputStream input ) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		input.transferTo( output );
		return output.toString( StandardCharsets.UTF_8 );
	}

	public static long copy( InputStream input, OutputStream output ) throws IOException {
		return copy( input, output, DEFAULT_BUFFER_SIZE );
	}

	public static long copy( InputStream input, OutputStream output, int bufferSize ) throws IOException {
		return copy( input, output, new byte[ bufferSize ] );
	}

	public static long copy( InputStream input, OutputStream output, byte[] buffer ) throws IOException {
		return copy( input, output, buffer, null );
	}

	public static long copy( InputStream input, OutputStream output, LongConsumer progressCallback ) throws IOException {
		return copy( input, output, DEFAULT_BUFFER_SIZE, progressCallback );
	}

	public static long copy( InputStream input, OutputStream output, int bufferSize, LongConsumer progressCallback ) throws IOException {
		return copy( input, output, new byte[ bufferSize ], progressCallback );
	}

	@SuppressWarnings( "Duplicates" )
	public static long copy( InputStream input, OutputStream output, byte[] buffer, LongConsumer progressCallback ) throws IOException {
		int count;
		long total;

		for( total = 0L; -1 != (count = input.read( buffer )); total += count ) {
			output.write( buffer, 0, count );
			if( progressCallback != null ) progressCallback.accept( total );
		}

		return total;
	}

	public static long copy( InputStream input, Writer writer, String encoding ) throws IOException {
		return copy( new InputStreamReader( input, Charset.forName( encoding )), writer, DEFAULT_BUFFER_SIZE );
	}

	public static long copy( Reader reader, Writer writer, int bufferSize ) throws IOException {
		return copy( reader, writer, new char[bufferSize]);
	}

	public static long copy( Reader reader, Writer writer, char[] buffer ) throws IOException {
		return copy( reader, writer, buffer, null );
	}

	@SuppressWarnings( "Duplicates" )
	public static long copy( Reader reader, Writer writer, char[] buffer, LongConsumer progressCallback ) throws IOException {
		int count;
		long total;

		for( total = 0L; -1 != (count = reader.read( buffer )); total += count ) {
			writer.write( buffer, 0, count );
			if( progressCallback != null ) progressCallback.accept( total );
		}

		return total;
	}

	public static void write( char[] data, OutputStream output, Charset encoding ) throws IOException {
		if( data != null ) output.write( new String( data ).getBytes( encoding ) );
	}

	public static void write( String data, OutputStream output, String encoding ) throws IOException {
		write( data.toCharArray(), output, Charset.forName( encoding ) );
	}

	public static String toString( InputStream input, String encoding ) throws IOException {
		try( final StringWriter sw = new StringWriter() ) {
			copy( input, sw, encoding );
			return sw.toString();
		}
	}

}
