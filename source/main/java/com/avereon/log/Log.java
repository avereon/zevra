package com.avereon.log;

import com.avereon.util.FileUtil;
import com.avereon.util.LogFlag;
import com.avereon.util.ProgramFormatter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class Log {

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
	 * @param source The object configuring the logging
	 * @param parameters Command line parameters that may contain log parameters
	 * @param logFolder The folder where to publish log files
	 * @param defaultFile The default log file name pattern
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
		String level = LogFlag.toLogLevel( parameters.get( LogFlag.LOG_LEVEL, LogFlag.INFO ) ).getName();
		String filePattern = parameters.get( LogFlag.LOG_FILE, defaultFile );

		boolean nameOnly = filePattern != null && !filePattern.contains( File.separator );
		if( nameOnly && logFolder != null ) filePattern = new File( logFolder.toFile(), filePattern ).toString();

		// Configure the simple formatter
		// https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Formatter.html#syntax
		builder.append( ProgramFormatter.class.getName() ).append( ".format=%1$tF %1$tT.%1$tL %4$s %2$s: %5$s %6$s%n\n" );

		// Add the log console handler
		if( filePattern == null ) {
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

		if( filePattern != null ) {
			filePattern = reduceFilePattern( new File( filePattern ).getAbsolutePath() );

			File file = new File( expandFilePattern( filePattern ) );
			File folder = file.getAbsoluteFile().getParentFile();
			if( !folder.exists() && !folder.mkdirs() ) throw new RuntimeException( "Unable to create log folder: " + folder );

			builder.append( "java.util.logging.FileHandler.level=" ).append( level ).append( "\n" );
			builder.append( "java.util.logging.FileHandler.pattern=" ).append( filePattern ).append( "\n" );
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
		builder.append( ".level=" ).append( LogFlag.toLogLevel( LogFlag.WARN ).getName() ).append( "\n" );

		// NOTE Log levels can be customized with Log.setPackageLogLevel()

		// The final configuration
		String configuration = builder.toString();
		try {
			if( logFolder != null ) FileUtil.save( configuration, logFolder.resolve( "log.config.properties" ) );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}

		// Initialize the logging
		try {
			InputStream input = new ByteArrayInputStream( configuration.getBytes( StandardCharsets.UTF_8 ) );
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
		String level = LogFlag.toLogLevel( levelName ).getName();

		// Set the program logger level
		builder.append( packageName ).append( ".level=" ).append( level ).append( "\n" );

		try {
			InputStream input = new ByteArrayInputStream( builder.toString().getBytes( StandardCharsets.UTF_8 ) );
			LogManager.getLogManager().updateConfiguration( input, k -> ( o, n ) -> n == null ? o : n );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}
	}

	public static void setBaseLogLevel( Level level ) {
		java.util.logging.Logger.getLogger( "" ).setLevel( level );
	}

	public static void setBaseLogLevel( String levelName ) {
		setBaseLogLevel( LogFlag.toLogLevel( levelName ) );
	}

	public static String getLogFile() {
		return LogManager.getLogManager().getProperty( "java.util.logging.FileHandler.pattern" );
	}

	public static void flush() {
		LogManager manager = LogManager.getLogManager();
		manager.getLoggerNames().asIterator().forEachRemaining( name -> {
			Handler[] handlers = manager.getLogger( name ).getHandlers();
			Arrays.stream( handlers ).forEach( Handler::flush );
		} );
	}

	public static void reset() {
		LogManager.getLogManager().reset();
	}

	static String reduceFilePattern( String path ) {
		if( path.startsWith( "%h/" ) ) path = path.substring( 3 );
		Path userHome = Paths.get( System.getProperty( "user.home" ) );
		return "%h/" + userHome.relativize( Paths.get( path ) ).toString().replace( '\\', '/' );
	}

	static String expandFilePattern( String path ) {
		return path.replace( "%h", System.getProperty( "user.home" ) ).replace( '\\', '/' );
	}

}
