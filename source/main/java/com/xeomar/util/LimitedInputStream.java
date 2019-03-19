package com.xeomar.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of InputStream that will provide bytes up to a certain limit.
 */
public class LimitedInputStream extends InputStream {

	private static int EOF = -1;

	private final InputStream input;

	private final long limit;

	private long position = 0;

	private long mark = EOF;

	/**
	 * Creates a new <code>LimitedInputStream</code> that wraps the given input
	 * stream and limits it to a certain length.
	 *
	 * @param input The wrapped input stream
	 * @param limit The maximum number of bytes to return
	 */
	public LimitedInputStream( final InputStream input, final long limit ) {
		this.limit = limit;
		this.input = input;
	}

	/**
	 * Creates a new <code>LimitedInputStream</code> that wraps the given input
	 * stream and is not limited.
	 *
	 * @param input The wrapped input stream
	 */
	public LimitedInputStream( final InputStream input ) {
		this( input, EOF );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		if( limit >= 0 && position >= limit ) {
			return EOF;
		}
		final int result = input.read();
		position++;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read( final byte[] b ) throws IOException {
		return this.read( b, 0, b.length );
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read( final byte[] b, final int off, final int len ) throws IOException {
		if( limit >= 0 && position >= limit ) {
			return EOF;
		}
		final long maxRead = limit >= 0 ? Math.min( len, limit - position ) : len;
		final int bytesRead = input.read( b, off, (int)maxRead );

		if( bytesRead == EOF ) {
			return EOF;
		}

		position += bytesRead;
		return bytesRead;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long skip( final long n ) throws IOException {
		final long toSkip = limit >= 0 ? Math.min( n, limit - position ) : n;
		final long skippedBytes = input.skip( toSkip );
		position += skippedBytes;
		return skippedBytes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int available() throws IOException {
		if( limit >= 0 && position >= limit ) {
			return 0;
		}
		return input.available();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return input.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
			input.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void reset() throws IOException {
		input.reset();
		position = mark;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void mark( final int readlimit ) {
		input.mark( readlimit );
		mark = position;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean markSupported() {
		return input.markSupported();
	}

}
