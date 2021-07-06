package com.avereon.log;

import java.util.Objects;
import java.util.logging.Level;

public abstract class AbstractLogger<API extends LogApi<API>> {

	private final LoggerBackend backend;

	protected AbstractLogger( LoggerBackend backend ) {
		this.backend = Objects.requireNonNull( backend );
	}

	public abstract API at( Level level );

	public final API atSevere() {
		return at( Level.SEVERE );
	}

	/** A convenience method for at({@link Level#WARNING}). */
	public final API atWarning() {
		return at( Level.WARNING );
	}

	/** A convenience method for at({@link Level#INFO}). */
	public final API atInfo() {
		return at( Level.INFO );
	}

	/** A convenience method for at({@link Level#CONFIG}). */
	public final API atConfig() {
		return at( Level.CONFIG );
	}

	/** A convenience method for at({@link Level#FINE}). */
	public final API atFine() {
		return at( Level.FINE );
	}

	/** A convenience method for at({@link Level#FINER}). */
	public final API atFiner() {
		return at( Level.FINER );
	}

	/** A convenience method for at({@link Level#FINEST}). */
	public final API atFinest() {
		return at( Level.FINEST );
	}

	/**
	 * Returns the name of this logger.
	 */
	protected String getName() {
		return backend.getLoggerName();
	}

	/**
	 * Returns whether the given level is enabled for this logger. Users wishing to guard code with a
	 * check for "loggability" should use {@code logger.atLevel().isEnabled()} instead.
	 */
	protected final boolean isLoggable( Level level ) {
		return backend.isLoggable( level );
	}

	final LoggerBackend getBackend() {
		return backend;
	}

	final void write( LogData data ) {
		Objects.requireNonNull( data );
		try {
			backend.log( data );
		} catch( RuntimeException error ) {
			try {
				backend.handleError( error, data );
			} catch( LoggingException allowed ) {
				// Bypass the catch-all if the exception is deliberately created during error handling.
				throw allowed;
			} catch( RuntimeException runtimeException ) {
				System.err.println( "logging error: " + runtimeException.getMessage() );
				runtimeException.printStackTrace( System.err );
			}
		}
	}

}
