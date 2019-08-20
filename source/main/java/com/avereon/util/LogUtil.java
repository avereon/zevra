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
import java.util.logging.LogManager;

public class LogUtil {

	public static Logger get( Class<?> clazz ) {
		return LoggerFactory.getLogger( clazz );
	}

	public static void configureLogging( Object source, com.avereon.util.Parameters parameters ) {
		configureLogging( source, parameters, null, null );
	}

	public static void configureLogging( Object source, com.avereon.util.Parameters parameters, Path programDataFolder, String defaultFile ) {
		// Logging level conversion
		//
		// SLF4J -> Java
		// ---------------
		// ERROR -> SEVERE
		// WARN  -> WARNING
		// INFO  -> INFO
		// DEBUG -> FINE
		// TRACE -> FINEST

		String level = parameters.get( LogFlag.LOG_LEVEL );
		String file = parameters.get( LogFlag.LOG_FILE, defaultFile );

		boolean nameOnly = file != null && !file.contains( File.separator );
		if( nameOnly && programDataFolder != null ) file = new File( programDataFolder.toFile(), file ).toString();

		// Configure the simple formatter
		// https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html#syntax
		StringBuilder builder = new StringBuilder( ProgramFormatter.class.getName() + ".format=%1$tF %1$tT.%1$tL %4$s %2$s: %5$s %6$s%n\n" );

		// Add the log console handler
		if( file == null ) {
			builder.append( "handlers=java.util.logging.ConsoleHandler\n" );
		} else {
			builder.append( "handlers=java.util.logging.ConsoleHandler,java.util.logging.FileHandler\n" );
		}

		// Configure the console handler
		builder.append( "java.util.logging.ConsoleHandler.level=ALL\n" );
		builder.append( "java.util.logging.ConsoleHandler.formatter=" ).append( ProgramFormatter.class.getName() ).append( "\n" );

		if( file != null ) {
			file = getLogFilePattern( new File( file ).getAbsolutePath() );
			builder.append( "java.util.logging.FileHandler.pattern=" ).append( file ).append( "\n" );
			builder.append( "java.util.logging.FileHandler.encoding=utf-8\n" );
			builder.append( "java.util.logging.FileHandler.level=ALL\n" );
			builder.append( "java.util.logging.FileHandler.limit=50000\n" );
			builder.append( "java.util.logging.FileHandler.count=1\n" );
			builder.append( "java.util.logging.FileHandler.formatter=" ).append( ProgramFormatter.class.getName() ).append( "\n" );
			if( parameters.isSet( LogFlag.LOG_APPEND ) ) builder.append( "java.util.logging.FileHandler.append=true\n" );
		}

		// Set the default log level
		builder.append( ".level=" );
		builder.append( getLogLevel( level ) );
		builder.append( "\n" );

		// Set the javafx.scene log level
		builder.append( "javafx" );
		builder.append( ".level=" );
		builder.append( getLogLevel( "info" ) );
		builder.append( "\n" );

		// Set the program log level
		builder.append( source.getClass().getPackage().getName() );
		builder.append( ".level=" );
		builder.append( getLogLevel( level ) );
		builder.append( "\n" );

		// Initialize the logging
		try {
			// NOTE For this to work as expected the slf4j-jdk14 artifact must be on the classpath
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
			case LogFlag.NONE: {
				result = "OFF";
				break;
			}
			case LogFlag.ERROR: {
				result = "SEVERE";
				break;
			}
			case LogFlag.WARN: {
				result = "WARNING";
				break;
			}
			case LogFlag.INFO: {
				result = "INFO";
				break;
			}
			case LogFlag.DEBUG: {
				result = "FINE";
				break;
			}
			case LogFlag.TRACE: {
				result = "FINEST";
				break;
			}
			case LogFlag.ALL: {
				result = "ALL";
				break;
			}
			default: {
				result = "INFO";
				break;
			}
		}

		return result;
	}

}