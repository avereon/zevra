package com.avereon.log.java;

import com.avereon.log.LogData;
import com.avereon.log.provider.LoggerWrapper;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class JavaLoggingLoggerWrapper implements LoggerWrapper {

	private static final long ONE_BILLION = 1000000000;

	/**
	 * The Java logger instance.
	 */
	private final Logger logger;

	public JavaLoggingLoggerWrapper( java.util.logging.Logger logger ) {
		this.logger = logger;
	}

	@Override
	public String getLoggerName() {
		return logger.getName();
	}

	@Override
	public boolean isLoggable( Level level ) {
		return logger.isLoggable( level );
	}

	@Override
	public void log( LogData data ) {
		LogRecord record = new LogRecord( data.getLevel(), null );

		// FIXME These need to be set correctly
		record.setSourceClassName( getLoggerName() );
		record.setSourceMethodName( "greet" );

		record.setInstant( Instant.ofEpochSecond( data.getTimestampNanos() / ONE_BILLION, data.getTimestampNanos() % ONE_BILLION ) );
		record.setMessage( data.getMessage() );
		record.setParameters( data.getMessageParameters() );
		record.setThrown( (Throwable)data.getMetadata().get( LogData.CAUSE ) );

		logger.log( record );
	}

	@Override
	public void handleError( LogData data, RuntimeException error ) {
		// This Java logging wrapper does not currently support this feature
	}

}
