package com.avereon.util;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogTest {

	private final String datePattern = "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]";

	private final String timePattern = "[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\.[0-9][0-9][0-9]";

	private final String timestampPattern = datePattern + " " + timePattern;

	@Test
	void testLog() throws IOException {
		PrintStream original = System.err;

		Log.configureLogging( this, Parameters.parse( LogFlag.LOG_LEVEL, LogFlag.ALL ) );
		Log.setPackageLogLevel( getClass().getPackageName(), LogFlag.ALL );

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream print = new PrintStream( output );
		System.setErr( print );

		System.Logger log = Log.get();
		log.log( Log.ERROR, "ERROR" );
		log.log( Log.WARN, "WARN" );
		log.log( Log.INFO, "INFO" );
		log.log( Log.DEBUG, "DEBUG" );
		log.log( Log.TRACE, "TRACE" );

		System.setErr( original );
		assertThat( System.err, is( original ) );

		String text = new String( output.toByteArray() );

		BufferedReader reader = new BufferedReader( new StringReader( text ) );
		assertThat( reader.readLine(), Matchers.matchesRegex( "^" + timestampPattern + " \\[E\\] .*ERROR $" ) );
		assertThat( reader.readLine(), Matchers.matchesRegex( "^" + timestampPattern + " \\[W\\] .*WARN $" ) );
		assertThat( reader.readLine(), Matchers.matchesRegex( "^" + timestampPattern + " \\[I\\] .*INFO $" ) );
		assertThat( reader.readLine(), Matchers.matchesRegex( "^" + timestampPattern + " \\[D\\] .*DEBUG $" ) );
		assertThat( reader.readLine(), Matchers.matchesRegex( "^" + timestampPattern + " \\[T\\] .*TRACE $" ) );
	}

	@Test
	void testConfigure() {
		String name = "test.%u.log";
		String home = System.getProperty( "user.home" );
		String expected = new File( name ).getAbsoluteFile().toString().replace( home, "%h" );

		Log.configureLogging( this, Parameters.parse( LogFlag.LOG_FILE, name ) );
		assertThat( Log.getLogFile(), is( expected ) );
	}

	@Test
	void testReduceFilePattern() {
		String home = System.getProperty( "user.home" );
		String name = "test.%u.log";
		String path = home + File.separator + name;

		assertThat( Log.reduceFilePattern( path ), is( "%h" + File.separator + name ) );
	}

	@Test
	void testExpandFilePattern() {
		String home = System.getProperty( "user.home" );
		String name = "test.%u.log";
		String path = home + File.separator + name;

		assertThat( Log.expandFilePattern( "%h" + File.separator + name ), is( path ) );
	}

}
