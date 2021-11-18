package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class NonBlockingReaderTest {

	private static final long delay = 50;

	@Test
	void testReadLine() throws Exception {
		String line1 = "I am first with five";
		String line2 = "Then seven in the middle";
		String line3 = "Five again to end";

		String content = line1 + "\n" + line2 + "\n" + line3;
		NonBlockingReader reader = new NonBlockingReader( new StringReader( content ) );

		long time = delay;
		assertThat( reader.readLine( time, TimeUnit.MILLISECONDS ) ).isEqualTo( line1 );
		assertThat( reader.readLine( time, TimeUnit.MILLISECONDS ) ).isEqualTo( line2 );
		assertThat( reader.readLine( time, TimeUnit.MILLISECONDS ) ).isEqualTo( line3 );
		assertThat( reader.readLine( time, TimeUnit.MILLISECONDS ) ).isNull();
	}

	@Test
	void testReadLineWithTimeout() {
		try {
			NonBlockingReader reader = null;
			try {
				reader = new NonBlockingReader( System.in );
				assertThat( reader.readLine( delay, TimeUnit.MILLISECONDS ) ).isNull();
			} finally {
				if( reader != null ) reader.close();
			}
		} catch( IOException exception ) {
			exception.printStackTrace();
		}
	}

	@Test
	void testClose() throws Exception {
		long time = delay;

		NonBlockingReader reader = new NonBlockingReader( System.in );

		// Setup a thread that will close the reader before the read times out
		new Thread( () -> {
			ThreadUtil.pause( time );
			try {
				reader.close();
			} catch( IOException exception ) {
				exception.printStackTrace();
			}
		} ).start();

		// Read a line with a timeout longer than the closing thread pause
		assertThat( reader.readLine( 2 * time, TimeUnit.MILLISECONDS ) ).isNull();
	}

}
