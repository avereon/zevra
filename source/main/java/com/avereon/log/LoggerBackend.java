package com.avereon.log;

import java.util.logging.Level;

public abstract class LoggerBackend {

	public abstract String getLoggerName();

	/**
	 * Returns whether logging is enabled for the given level for this backend. Different backends may
	 * return different values depending on the class with which they are associated.
	 */
	public abstract boolean isLoggable( Level lvl );

	/**
	 * Outputs the log statement represented by the given {@link LogData} instance.
	 *
	 * @param data user and logger supplied data to be rendered in a backend specific way.
	 */
	public abstract void log( LogData data );

	/**
	 * Handles an error in a log statement. Errors passed into this method are expected to have only
	 * three distinct causes:
	 * <ol>
	 *   <li>Bad format strings in log messages (e.g. {@code "foo=%Q"}. These will always be
	 *   instances of {@link com.google.common.flogger.parser.ParseException ParseException} and
	 *   contain human readable error messages describing the problem.
	 *   <li>A backend optionally choosing not to handle errors from user code during formatting.
	 *   This is not recommended (see below) but may be useful in testing or debugging.
	 *   <li>Runtime errors in the backend itself.
	 * </ol>
	 *
	 * <p>It is recommended that backend implementations avoid propagating exceptions in user code
	 * (e.g. calls to {@code toString()}), as the nature of logging means that log statements are
	 * often only enabled when debugging. If errors were propagated up into user code, enabling
	 * logging to look for the cause of one issue could trigger previously unknown bugs, which could
	 * then seriously hinder debugging the original issue.
	 *
	 * <p>Typically a backend would handle an error by logging an alternative representation of the
	 * "bad" log data, being careful not to allow any more exceptions to occur. If a backend chooses
	 * to propagate an error (e.g. when testing or debugging) it must wrap it in
	 * {@link LoggingException} to avoid it being re-caught.
	 *
	 * @param error the exception throw when {@code badData} was initially logged.
	 * @param badData the original {@code LogData} instance which caused an error. It is not expected
	 * that simply trying to log this again will succeed and error handlers must be careful in
	 * how they handle this instance, it's arguments and metadata.
	 * @throws LoggingException to indicate an error which should be propagated into user code.
	 */
	public abstract void handleError( RuntimeException error, LogData badData );

}
