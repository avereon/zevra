package com.avereon.log;

import com.avereon.util.LogFlag;
import com.avereon.util.OperatingSystem;
import com.avereon.util.Parameters;
import lombok.CustomLog;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.assertThat;

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

		assertThat( log.atError().isEnabled() ).isTrue();
		assertThat( log.atWarn().isEnabled() ).isTrue();
		assertThat( log.atInfo().isEnabled() ).isTrue();
		assertThat( log.atDebug().isEnabled() ).isTrue();
		assertThat( log.atTrace().isEnabled() ).isTrue();

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream print = new PrintStream( output );
		System.setErr( print );

		log.atError().log( "ERROR" );
		log.atWarn().log( "WARN" );
		log.atInfo().log( "INFO" );
		log.atDebug().log( "DEBUG" );
		log.atTrace().log( "TRACE" );

		System.setErr( original );
		assertThat( System.err ).isEqualTo( original );

		BufferedReader reader = new BufferedReader( new StringReader( output.toString() ) );
		assertThat( reader.readLine() ).matches( "^" + timestampPattern + " \\[E] .*ERROR $" );
		assertThat( reader.readLine() ).matches( "^" + timestampPattern + " \\[W] .*WARN $" );
		assertThat( reader.readLine() ).matches( "^" + timestampPattern + " \\[I] .*INFO $" );
		assertThat( reader.readLine() ).matches( "^" + timestampPattern + " \\[D] .*DEBUG $" );
		assertThat( reader.readLine() ).matches( "^" + timestampPattern + " \\[T] .*TRACE $" );
	}

	@Test
	void testConfigure() {
		String name = "test.%u.log";
		String home = System.getProperty( "user.home" );
		String expected = new File( name ).getAbsoluteFile().toString().replace( home, "%h" );
		expected = expected.replace( '\\', '/' );

		Log.configureLogging( this, Parameters.parse( LogFlag.LOG_FILE, name ) );
		assertThat( Log.getLogFile() ).isEqualTo( expected );
	}

	@Test
	void testReduceFilePattern() {
		String home = System.getProperty( "user.home" );
		String name = "test.%u.log";
		String path = home + File.separator + name;

		assertThat( Log.reduceFilePattern( path ) ).isEqualTo( "%h/" + name );
	}

	@Test
	void testExpandFilePattern() {
		String home = System.getProperty( "user.home" );
		String name = "test.%u.log";
		String path = home + File.separator + name;

		assertThat( Log.expandFilePattern( "%h" + File.separator + name ) ).isEqualTo( path.replace( '\\', '/' ) );
	}

}
