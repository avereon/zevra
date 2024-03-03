package com.avereon.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * The ProgramFormatter class is a custom formatter for log records that formats
 * the log messages according to a specific format. It extends the Formatter
 * class provided by the Java logging framework. The format of the log messages
 * can be customized by changing the value of the FORMAT_PROPERTY_KEY property
 * in the logging configuration, or by setting a system property with the same
 * name. If no format is specified, the default format is used.
 * <p>
 * The format of the log messages is specified using a format string containing
 * placeholders for the different log record attributes.
 * The supported placeholders are:<br>
 * - %1$tF: The date and time of the log record formatted as "yyyy-MM-dd".<br>
 * - %1$tT: The time of the log record formatted as "HH:mm:ss".<br>
 * - %1$tL: The milliseconds of the log record.<br>
 * - %4$s: The logger name.<br>
 * - %2$s: The source of the log record (either the source class name or the logger name if the source class name is not specified).</br>
 * - %5$s: The log level.<br>
 * - %6$s: The log message.<br>
 * - %n: A platform-dependent line separator.
 * <p>
 * The format string can be customized by overriding the getSimpleFormat() method.
 * <p>
 * Example usage:
 * <pre>{@code
 *   Logger logger = Logger.getLogger("myLogger");
 *   Handler handler = new ConsoleHandler();
 *   handler.setFormatter(new ProgramFormatter());
 *   logger.addHandler(handler);
 *   logger.log(Level.INFO, "This is a log message.");
 * }</pre>
 */
public class ProgramFormatter extends Formatter {

	private static final String FORMAT_PROPERTY_KEY = ProgramFormatter.class.getName() + ".format";

	private static final String DEFAULT_FORMAT = "%1$tF %1$tT.%1$tL %4$s %2$s %5$s %6$s%n";

	private final String format = getSimpleFormat( ProgramFormatter::getLoggingProperty );

	@Override
	public String format( LogRecord record ) {
		// Timestamp
		Instant instant = Instant.ofEpochMilli( record.getMillis() );
		ZonedDateTime timestamp = ZonedDateTime.ofInstant( instant, ZoneId.systemDefault() );

		// Source
		String source = record.getSourceClassName();
		if( source == null ) {
			source = record.getLoggerName();
		} else {
			source = JavaUtil.getShortClassName( source );
			if( record.getSourceMethodName() != null ) source += "." + record.getSourceMethodName();
		}

		// Logger
		String logger = record.getLoggerName();

		// Level
		String level = getLevel( record.getLevel() );
		//String level = record.getLevel().getLocalizedName();

		// Message
		String message = formatMessage( record );

		// Throwable
		String throwable = "";
		if( record.getThrown() != null ) {
			StringWriter writer = new StringWriter();
			PrintWriter printer = new PrintWriter( writer );
			printer.println();
			record.getThrown().printStackTrace( printer );
			printer.close();
			throwable = writer.toString();
		}

		return String.format( format, timestamp, source, logger, level, message, throwable );
	}

	private String getLevel( Level level ) {
		String result;

		switch( level.getName() ) {
			case ("SEVERE"): {
				result = "[E]";
				break;
			}
			case ("WARNING"): {
				result = "[W]";
				break;
			}
			case ("INFO"): {
				result = "[I]";
				break;
			}
			case ("CONFIG"): {
				result = "[C]";
				break;
			}
			case ("FINE"): {
				result = "[D]";
				break;
			}
			case ("FINER"): {
				result = "[T]";
				break;
			}
			default: {
				result = "[" + level.getName().charAt( 0 ) + "]";
			}
		}

		return result;
	}

	private static String getSimpleFormat( Function<String, String> defaultPropertyGetter ) {
		String format = defaultPropertyGetter.apply( FORMAT_PROPERTY_KEY );
		if( format == null ) format = getLoggingProperty( FORMAT_PROPERTY_KEY );
		if( format == null ) format = DEFAULT_FORMAT;
		return format;
	}

	private static String getLoggingProperty( String name ) {
		return LogManager.getLogManager().getProperty( name );
	}

}
