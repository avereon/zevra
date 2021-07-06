package com.avereon.log;

import com.avereon.log.provider.LoggerWrapper;

import java.util.logging.Level;

public final class Logger extends AbstractLogger<Logger.Api> {

	public interface Api extends LogApi<Api> {}

	private static final class NoOp extends LogApi.NoOp<Api> implements Api {}

	// Singleton instance of the no-op API. This variable is purposefully declared
	// as an instance of the NoOp type instead of the Api type. Do not change this
	// to 'Api', or any less specific type.
	// VisibleForTesting
	static final NoOp NO_OP = new NoOp();

	// VisibleForTesting
	Logger( LoggerWrapper provider ) {
		super( provider );
	}

	@Override
	public Api at( Level level ) {
		boolean isLoggable = isLoggable( level );
		boolean isForced = false;
		return (isLoggable || isForced) ? new Context( level, isForced ) : NO_OP;
	}

	final class Context extends LogContext<Logger, Api> implements Api {

		private Context( Level level, boolean isForced ) {
			super( level, isForced );
		}

		@Override
		protected Logger getLogger() {
			return Logger.this;
		}

		@Override
		protected Api api() {
			return this;
		}

		@Override
		protected Api noOp() {
			return NO_OP;
		}

	}

}
