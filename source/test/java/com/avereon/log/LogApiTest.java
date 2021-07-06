package com.avereon.log;

import com.avereon.log.provider.AbstractLoggerWrapper;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LogApiTest {

	@Test
	void testAt() {
		TestWrapper wrapper = new TestWrapper();
		Logger logger = new Logger( wrapper );

		logger.atInfo().log();

		assertThat( wrapper.getData().getLevel(), is( Level.INFO ) );
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
