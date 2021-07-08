package com.avereon.log;

import com.avereon.util.LogFlag;
import com.avereon.util.Parameters;
import lombok.CustomLog;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CustomLog
public class LogTest {

	private final String datePattern = "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]";

	private final String timePattern = "[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\.[0-9][0-9][0-9]";

	private final String timestampPattern = datePattern + " " + timePattern;

	@Test
	void testLog() throws IOException {
		PrintStream original = System.err;

		Log.configureLogging( this, Parameters.parse( LogFlag.LOG_LEVEL, LogFlag.ALL ) );
		Log.setPackageLogLevel( getClass().getPackageName(), LogFlag.ALL );

		assertTrue( log.atError().isEnabled() );
		assertTrue( log.atWarn().isEnabled() );
		assertTrue( log.atInfo().isEnabled() );
		assertTrue( log.atDebug().isEnabled() );
		assertTrue( log.atTrace().isEnabled() );

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream print = new PrintStream( output );
		System.setErr( print );

		log.atError().log( "ERROR" );
		log.atWarn().log( "WARN" );
		log.atInfo().log( "INFO" );
		log.atDebug().log( "DEBUG" );
		log.atTrace().log( "TRACE" );

		System.setErr( original );
		assertThat( System.err, is( original ) );

		BufferedReader reader = new BufferedReader( new StringReader( output.toString() ) );
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
