package com.avereon.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class LogUtil {

	public static Logger get( Class<?> clazz ) {
		return LoggerFactory.getLogger( clazz );
	}

	public static void configureLogging( Object source, com.avereon.util.Parameters parameters ) {
		configureLogging( source, parameters, null, null );
	}

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

		String level = getLogLevel( parameters.get( LogFlag.LOG_LEVEL ) );
		String file = parameters.get( LogFlag.LOG_FILE, defaultFile );
		if( file == null ) file = "program.log";

		if( file.endsWith( ".log" ) && !file.contains( "%u" ) ) {
			String name = FileUtil.removeExtension( file );
			file = name + ".%u.log";
		}

		boolean nameOnly = !file.contains( File.separator );
		if( nameOnly && logFolder != null ) file = new File( logFolder.toFile(), file ).toString();

		// Configure the simple formatter
		// https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Formatter.html#syntax
		StringBuilder builder = new StringBuilder( ProgramFormatter.class.getName() + ".format=%1$tF %1$tT.%1$tL %4$s %2$s: %5$s %6$s%n\n" );

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

		// Set the javafx logger level
		builder.append( "javafx" ).append( ".level=" ).append( level ).append( "\n" );

		// Set the program logger level
		String sourcePackage = source.getClass().getPackage().getName();
		builder.append( sourcePackage ).append( ".level=" ).append( level ).append( "\n" );

		// Set the default logger level for all other loggers
		// Don't set this too low (debug, trace, all) because it can be noisy
		builder.append( ".level=" ).append( getLogLevel( LogFlag.NONE ) ).append( "\n" );

		// NOTE For this to work as expected the slf4j-jdk14 artifact must be on the classpath
		// Initialize the logging
		try {
			InputStream input = new ByteArrayInputStream( builder.toString().getBytes( StandardCharsets.UTF_8 ) );
			LogManager.getLogManager().readConfiguration( input );
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

	private static String getLogLevel( String level ) {
		String result = level == null ? LogFlag.INFO : level.toLowerCase();

		switch( result ) {
			case LogFlag.ERROR: {
				result = Level.SEVERE.getName();
				break;
			}
			case LogFlag.WARN: {
				result = Level.WARNING.getName();
				break;
			}
			case LogFlag.INFO: {
				result = Level.INFO.getName();
				break;
			}
			case LogFlag.DEBUG: {
				result = Level.FINE.getName();
				break;
			}
			case LogFlag.TRACE: {
				result = Level.FINEST.getName();
				break;
			}
			case LogFlag.ALL: {
				result = Level.ALL.getName();
				break;
			}
			default: {
				result = Level.OFF.getName();
				break;
			}
		}

		return result;
	}

}
