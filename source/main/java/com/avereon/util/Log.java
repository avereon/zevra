package com.avereon.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Log {

	public static final System.Logger.Level OFF = System.Logger.Level.OFF;

	public static final System.Logger.Level ERROR = System.Logger.Level.ERROR;

	public static final System.Logger.Level WARN = System.Logger.Level.WARNING;

	public static final System.Logger.Level INFO = System.Logger.Level.INFO;

	public static final System.Logger.Level DEBUG = System.Logger.Level.DEBUG;

	public static final System.Logger.Level TRACE = System.Logger.Level.TRACE;

	public static final System.Logger.Level ALL = System.Logger.Level.ALL;

	public static System.Logger get() {
		return new SystemLoggerWrapper( System.getLogger( JavaUtil.getCallingClassName( 1 ) ) );
	}

	public static void configureLogging( Object source, com.avereon.util.Parameters parameters ) {
		configureLogging( source, parameters, null, null );
	}

	/**
	 * Configure general logging for this JVM. This method does several things:
	 * <ol>
	 *   <li>Creates a ConsoleHandler set with the parameter log level</li>
	 *   <li>Creates a FileHandler set with the parameter log level</li>
	 *   <li>Sets the default log level to LogFlag.NONE</li>
	 *   <li>Sets the source package log level to the parameter log level</li>
	 * </ol>
	 *
	 * @param source
	 * @param parameters
	 * @param logFolder
	 * @param defaultFile
	 */
	public static void configureLogging( Object source, com.avereon.util.Parameters parameters, Path logFolder, String defaultFile ) {
		// Logging level conversion
		//
		// SLF4J -> Java
		// ---------------
		// ERROR -> SEVERE
		// WARN  -> WARNING
		// INFO  -> INFO
		// DEBUG -> FINE
		// TRACE -> FINEST

		StringBuilder builder = new StringBuilder();
		String level = convertToJavaLogLevel( parameters.get( LogFlag.LOG_LEVEL, LogFlag.INFO ) ).getName();
		String file = parameters.get( LogFlag.LOG_FILE, defaultFile );

		boolean nameOnly = file != null && !file.contains( File.separator );
		if( nameOnly && logFolder != null ) file = new File( logFolder.toFile(), file ).toString();

		// Configure the simple formatter
		// https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Formatter.html#syntax
		builder.append( ProgramFormatter.class.getName() ).append( ".format=%1$tF %1$tT.%1$tL %4$s %2$s: %5$s %6$s%n\n" );

		// Add the log console handler
		if( file == null ) {
			builder.append( "handlers=java.util.logging.ConsoleHandler\n" );
		} else {
			builder.append( "handlers=java.util.logging.ConsoleHandler,java.util.logging.FileHandler\n" );
		}

		// LOG HANDLER CONFIGURATIONS
		/*
		NOTE The level that filters out log events is the handler level.
		The handler level is specified in the handler configurations below.
		Also see logger level note below.
		*/

		// Configure the console handler
		builder.append( "java.util.logging.ConsoleHandler.level=" ).append( level ).append( "\n" );
		builder.append( "java.util.logging.ConsoleHandler.formatter=" ).append( ProgramFormatter.class.getName() ).append( "\n" );

		if( file != null ) {
			File folder = new File( file ).getParentFile();
			if( !folder.exists() && !folder.mkdirs() ) throw new RuntimeException( "Unable to create log folder: " + folder );

			file = getLogFilePattern( new File( file ).getAbsolutePath() );
			builder.append( "java.util.logging.FileHandler.level=" ).append( level ).append( "\n" );
			builder.append( "java.util.logging.FileHandler.pattern=" ).append( file ).append( "\n" );
			builder.append( "java.util.logging.FileHandler.encoding=utf-8\n" );
			builder.append( "java.util.logging.FileHandler.limit=50000\n" );
			builder.append( "java.util.logging.FileHandler.count=1\n" );
			builder.append( "java.util.logging.FileHandler.formatter=" ).append( ProgramFormatter.class.getName() ).append( "\n" );
			if( parameters.isSet( LogFlag.LOG_APPEND ) ) builder.append( "java.util.logging.FileHandler.append=true\n" );
		}

		// LOG LOGGER CONFIGURATIONS
		/*
		NOTE The level that loggers use to filter when creating log events is the logger level.
		The logger level is specified in the logger configurations below. There is
		no point in logging more than the handlers want to set them to the log level.
		Also see handler level note above.
		*/

		// Set the default logger level for all other loggers
		// Don't set this too low (debug, trace, all) because it can be noisy
		builder.append( ".level=" ).append( convertToJavaLogLevel( LogFlag.INFO ).getName() ).append( "\n" );

		// Set the logger level for all Avereon products
		builder.append( "com.avereon" ).append( ".level=" ).append( level ).append( "\n" );

		// NOTE For this to work as expected the slf4j-jdk14 artifact must be on the classpath
		// Initialize the logging
		try {
			InputStream input = new ByteArrayInputStream( builder.toString().getBytes( StandardCharsets.UTF_8 ) );
			LogManager.getLogManager().readConfiguration( input );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}
	}

	/**
	 * Updates the logging configuration by setting the log level for the
	 * package logger with the specified name.
	 *
	 * @param packageName The name of the package for the logger
	 * @param levelName The level name to use for the package logger
	 */
	public static void setPackageLogLevel( String packageName, String levelName ) {
		StringBuilder builder = new StringBuilder();
		String level = convertToJavaLogLevel( levelName ).getName();

		// Set the program logger level
		builder.append( packageName ).append( ".level=" ).append( level ).append( "\n" );

		try {
			InputStream input = new ByteArrayInputStream( builder.toString().getBytes( StandardCharsets.UTF_8 ) );
			LogManager.getLogManager().updateConfiguration( input, k -> ( o, n ) -> n == null ? o : n );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}
	}

	public static String getLogFile() {
		return LogManager.getLogManager().getProperty( "java.util.logging.FileHandler.pattern" );
	}

	private static String getLogFilePattern( String path ) {
		Path userHome = Paths.get( System.getProperty( "user.home" ) );
		return "%h/" + userHome.relativize( Paths.get( path ) ).toString().replace( '\\', '/' );
	}

	public static Level convertToJavaLogLevel( String level ) {
		switch( level == null ? LogFlag.NONE : level.toLowerCase() ) {
			case LogFlag.ERROR: {
				return Level.SEVERE;
			}
			case LogFlag.WARN: {
				return Level.WARNING;
			}
			case LogFlag.INFO: {
				return Level.INFO;
			}
			case LogFlag.DEBUG: {
				return Level.FINE;
			}
			case LogFlag.TRACE: {
				return Level.FINEST;
			}
			case LogFlag.ALL: {
				return Level.ALL;
			}
			default: {
				return Level.OFF;
			}
		}
	}

	/**
	 * This wrapper is specifically to address the situation where the
	 * {@link #log(Level, Object)} method receives a Throwable as the object.
	 */
	private static class SystemLoggerWrapper implements System.Logger {

		private System.Logger logger;

		public SystemLoggerWrapper( System.Logger logger ) {
			this.logger = logger;
		}

		@Override
		public String getName() {return logger.getName();}

		@Override
		public boolean isLoggable( Level level ) {return logger.isLoggable( level );}

		@Override
		public void log( Level level, String msg ) {logger.log( level, msg );}

		@Override
		public void log( Level level, Supplier<String> msgSupplier ) {logger.log( level, msgSupplier );}

		@Override
		public void log( Level level, Object obj ) {
			if( obj instanceof Throwable ) {
				logger.log( level, (String)null, (Throwable)obj );
			} else {
				logger.log( level, obj );
			}
		}

		@Override
		public void log( Level level, String msg, Throwable thrown ) {logger.log( level, msg, thrown );}

		@Override
		public void log( Level level, Supplier<String> msgSupplier, Throwable thrown ) {logger.log( level, msgSupplier, thrown );}

		@Override
		public void log( Level level, String format, Object... params ) {logger.log( level, format, params );}

		@Override
		public void log( Level level, ResourceBundle bundle, String msg, Throwable thrown ) {logger.log( level, bundle, msg, thrown );}

		@Override
		public void log( Level level, ResourceBundle bundle, String format, Object... params ) {logger.log( level, bundle, format, params );}
	}

}
