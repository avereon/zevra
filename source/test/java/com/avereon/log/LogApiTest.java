package com.avereon.log;

import com.avereon.log.java.JavaLoggingProvider;
import com.avereon.log.provider.AbstractLoggerWrapper;
import com.avereon.log.provider.AbstractLoggingProvider;
import com.avereon.log.provider.LoggerWrapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

public class LogApiTest {

	private static TestWrapper wrapper;

	private static Logger log;

	static {
	}

	@BeforeAll
	static void setup() {
		Logging.setLoggingProvider( new TestProvider() );
		wrapper = (TestWrapper)Logging.getLoggingProvider().getLoggerWrapper( "" );
		log = new Logger( wrapper );
	}

	@AfterAll
	static void teardown() {
		Logging.setLoggingProvider( new JavaLoggingProvider() );
	}

	@Test
	void testAtInfo() {
		log.atInfo().log();
		assertThat( wrapper.getData().getLevel() ).isEqualTo( Level.INFO );
		assertThat( wrapper.getData().getMessage() ).isEqualTo( "" );
	}

	@Test
	void testLogWithMessage() {
		log.atInfo().log( "Hello World!" );
		assertThat( wrapper.getData().getLevel() ).isEqualTo( Level.INFO );
		assertThat( wrapper.getData().getMessage() ).isEqualTo( "Hello World!" );
	}

	@Test
	void testLogWithMessageAndParameter() {
		log.atInfo().log( "Hello %s!", "World" );
		assertThat( wrapper.getData().getLevel() ).isEqualTo( Level.INFO );
		assertThat( wrapper.getData().getMessage() ).isEqualTo( "Hello World!" );
	}

	@Test
	void testLogWithMessageAndLazyParameter() {
		log.atInfo().log( "Hello %s!", LazyEval.of( () -> "World" ) );
		assertThat( wrapper.getData().getLevel() ).isEqualTo( Level.INFO );
		assertThat( wrapper.getData().getMessage() ).isEqualTo( "Hello World!" );
	}

	private static class TestProvider extends AbstractLoggingProvider {

		private final TestWrapper wrapper = new TestWrapper();

		@Override
		public LoggerWrapper getLoggerWrapper( String name ) {
			return wrapper;
		}

	}

	private static class TestWrapper extends AbstractLoggerWrapper {

		private LogData data;

		private RuntimeException exception;

		@Override
		public String getLoggerName() {
			return "test";
		}

		@Override
		public boolean isLoggable( Level level ) {
			return true;
		}

		@Override
		public void log( LogData data ) {
			this.data = data;
		}

		@Override
		public void handleError( LogData data, RuntimeException exception ) {
			this.data = data;
			this.exception = exception;
		}

		@Override
		public void flush() {}

		public LogData getData() {
			return data;
		}

		public RuntimeException getException() {
			return exception;
		}

	}

}
