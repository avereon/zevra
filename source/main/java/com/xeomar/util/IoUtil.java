package com.xeomar.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IoUtil {

	public static String toString( InputStream input ) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		input.transferTo( output );
		return output.toString( StandardCharsets.UTF_8 );
	}

	public static long copy( InputStream input, OutputStream output ) throws IOException {
		return copy( input, output, 4096 );
	}

	public static long copy( InputStream input, OutputStream output, int bufferSize ) throws IOException {
		return copy( input, output, new byte[ bufferSize ] );
	}

	public static long copy( InputStream input, OutputStream output, byte[] buffer ) throws IOException {
		long total;
		int count;
		for( total = 0L; -1 != (count = input.read( buffer )); total += count ) {
			output.write( buffer, 0, count );
		}

		return total;
	}

	public static long copy( InputStream input, OutputStream output, LongCallback progressCallback ) throws IOException {
		return copy( input, output, 4096, progressCallback );
	}

	public static long copy( InputStream input, OutputStream output, int bufferSize, LongCallback progressCallback ) throws IOException {
		return copy( input, output, new byte[ bufferSize ], progressCallback );
	}

	public static long copy( InputStream input, OutputStream output, byte[] buffer, LongCallback progressCallback ) throws IOException {
		int count;
		long total;

		for( total = 0L; -1 != (count = input.read( buffer )); total += count ) {
			output.write( buffer, 0, count );
			progressCallback.call( total );
		}

		return total;
	}

}
