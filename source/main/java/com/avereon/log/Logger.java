package com.avereon.log;

import com.google.common.flogger.backend.Platform;

import java.util.logging.Level;

public abstract class Logger extends AbstractLogger<Logger.Api> {

	public interface Api extends LogApi<Logger.Api> {}

	private static final class NoOp extends LogApi.NoOp<Logger.Api> implements Logger.Api {}

	// Singleton instance of the no-op API. This variable is purposefully declared as an instance of
	// the NoOp type instead of the Api type. Do not change this to 'Api', or any less specific type.
	// VisibleForTesting
	static final NoOp NO_OP = new NoOp();

	// VisibleForTesting
	Logger( LoggerBackend backend) {
		super(backend);
	}

	@Override
	public Api at( Level level) {
		boolean isLoggable = isLoggable(level);
		boolean isForced = Platform.shouldForceLogging(getName(), level, isLoggable);
		//return (isLoggable || isForced) ? new FluentLogger.Context(level, isForced) : NO_OP;
		return NO_OP;
	}

}
