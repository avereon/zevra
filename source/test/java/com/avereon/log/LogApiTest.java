package com.avereon.log;

import com.avereon.log.provider.AbstractLoggerWrapper;
import com.avereon.log.provider.AbstractLoggingProvider;
import com.avereon.log.provider.LoggerWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogApiTest {

	private static final TestWrapper wrapper;

	private static final Logger log;

	static {
		Logging.setLoggingProvider( new TestProvider() );
		wrapper = (TestWrapper)Logging.getLoggingProvider().getLoggerWrapper( "" );
		log = new Logger( wrapper );
	}

	@BeforeEach
	void setup() {
		Logging.setLoggingProvider( new TestProvider() );
	}

	@Test
	void testAtInfo() {
		log.atInfo().log();
		assertThat( wrapper.getData().getLevel(), is( Level.INFO ) );
		assertThat( wrapper.getData().getMessage(), is( "" ) );
	}

	@Test
	void testLogWithMessage() {
		log.atInfo().log( "Hello World!" );
		assertThat( wrapper.getData().getLevel(), is( Level.INFO ) );
		assertThat( wrapper.getData().getMessage(), is( "Hello World!" ) );
	}

	@Test
	void testLogWithMessageAndParameter() {
		log.atInfo().log( "Hello %s!", "World" );
		assertThat( wrapper.getData().getLevel(), is( Level.INFO ) );
		assertThat( wrapper.getData().getMessage(), is( "Hello World!" ) );
	}

	@Test
	void testLogWithMessageAndLazyParameter() {
		log.atInfo().log( "Hello %s!", LazyEval.of( () -> "World" ) );
		assertThat( wrapper.getData().getLevel(), is( Level.INFO ) );
		assertThat( wrapper.getData().getMessage(), is( "Hello World!" ) );
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

		public LogData getData() {
			return data;
		}

		public RuntimeException getException() {
			return exception;
		}

	}

}
