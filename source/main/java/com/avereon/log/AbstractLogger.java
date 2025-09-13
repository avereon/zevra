package com.avereon.log;

import com.avereon.log.provider.LoggerWrapper;

import java.util.Objects;
import java.util.logging.Level;

public abstract class AbstractLogger<API extends LogApi<API>> {

	private final LoggerWrapper provider;

	protected AbstractLogger( LoggerWrapper provider ) {
		this.provider = Objects.requireNonNull( provider );
	}

	public abstract API at( Level level );

	// Java Logging convenience methods ------------------------------------------

	/** A convenience method for at({@link Level#SEVERE}). */
	public final API atSevere() {
		return at( Level.SEVERE );
	}

	/** A convenience method for at({@link Level#SEVERE}). */
	public final API atError() {
		return at( Level.SEVERE );
	}

	/** A convenience method for at({@link Level#SEVERE}). */
	public final API atError( Throwable cause ) {
		return at( Level.SEVERE ).withCause( cause );
	}

	/** A convenience method for at({@link Level#WARNING}). */
	public final API atWarning() {
		return at( Level.WARNING );
	}

	/** A convenience method for at({@link Level#WARNING}). */
	public final API atWarn() {
		return at( Level.WARNING );
	}

	/** A convenience method for at({@link Level#WARNING}). */
	public final API atWarn( Throwable cause ) {
		return at( Level.WARNING ).withCause( cause );
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

	/** A convenience method for at({@link Level#FINE}). */
	public final API atDebug() {
		return at( Level.FINE );
	}

	/** A convenience method for at({@link Level#FINER}). */
	public final API atFiner() {
		return at( Level.FINER );
	}

	/** A convenience method for at({@link Level#FINER}). */
	public final API atTrace() {
		return at( Level.FINER );
	}

	/** A convenience method for at({@link Level#FINEST}). */
	@SuppressWarnings( "unused" )
	public final API atFinest() {
		return at( Level.FINEST );
	}

	/**
	 * Flushes any buffered log data.
	 */
	public void flush(){
		provider.flush();
	}

	/**
	 * Returns the name of this logger.
	 */
	protected String getName() {
		return provider.getLoggerName();
	}

	/**
	 * Returns whether the given level is enabled for this logger. Users wishing to guard code with a
	 * check for "loggability" should use {@code logger.atLevel().isEnabled()} instead.
	 */
	protected final boolean isLoggable( Level level ) {
		return provider.isLoggable( level );
	}

	final LoggerWrapper getProvider() {
		return provider;
	}

	final void write( LogData data ) {
		Objects.requireNonNull( data );
		try {
			provider.log( data );
		} catch( RuntimeException error ) {
			try {
				provider.handleError( data, error );
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
