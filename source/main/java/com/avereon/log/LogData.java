package com.avereon.log;

import com.google.common.flogger.LogSite;
import com.google.common.flogger.backend.Metadata;
import com.google.common.flogger.backend.TemplateContext;

import java.util.logging.Level;

public interface LogData {

	Level getLevel();

	/**
	 * @deprecated Use getTimestampNanos()
	 */
	@Deprecated
	long getTimestampMicros();

	/** Returns a nanosecond timestamp for the current log statement. */
	long getTimestampNanos();

	/**
	 * Returns the logger name (which is usually a canonicalized class name) or {@code null} if not
	 * given.
	 */
	String getLoggerName();

	/**
	 * Returns the log site data for the current log statement.
	 *
	 * @throws IllegalStateException if called prior to the postProcess() method being called.
	 */
	LogSite getLogSite();

	/**
	 * Returns any additional metadata for this log statement. If no additional metadata is present,
	 * the immutable empty metadata instance is returned.
	 */
	Metadata getMetadata();

	/**
	 * Returns whether this log statement should be emitted regardless of its log level or any other
	 * properties.
	 * <p>
	 * This allows extensions of {@code LogContext} or {@code LoggingBackend} which implement
	 * additional filtering or rate-limiting fluent methods to easily check whether a log statement
	 * was forced. Forced log statements should behave exactly as if none of the filtering or
	 * rate-limiting occurred, including argument validity checks.
	 * <p>
	 * Thus the idiomatic use of {@code wasForced()} is:
	 * <pre>{@code
	 * public API someFilteringMethod(int value) {
	 *   if (wasForced()) {
	 *     return api();
	 *   }
	 *   if (value < 0) {
	 *     throw new IllegalArgumentException("Bad things ...");
	 *   }
	 *   // rest of method...
	 * }
	 * }</pre>
	 * <p>
	 * Checking for forced log statements before checking the validity of arguments provides a
	 * last-resort means to mitigate cases in which syntactically incorrect log statements are only
	 * discovered when they are enabled.
	 */
	boolean wasForced();

	/**
	 * Returns a template key for this log statement, or {@code null} if the statement does not
	 * require formatting (in which case the message to be logged can be determined by calling
	 * {@link #getLiteralArgument()}).
	 */
	TemplateContext getTemplateContext();

	/**
	 * Returns the arguments to be formatted with the message. Arguments exist when a {@code log()}
	 * method with a format message and separate arguments was invoked.
	 *
	 * @throws IllegalStateException if no arguments are available (ie, when there is no template
	 *     context).
	 */
	Object[] getArguments();

	/**
	 * Returns the single argument to be logged directly when no arguments were provided.
	 *
	 * @throws IllegalStateException if no single literal argument is available (ie, when a template
	 *     context exists).
	 */
	Object getLiteralArgument();

}
