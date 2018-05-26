package com.xeomar.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;

public class LogUtil {

	public static Logger get( Class<?> clazz ) {
		return LoggerFactory.getLogger( clazz );
	}

	public static void configureLogging( Object source, com.xeomar.util.Parameters parameters ) {
		configureLogging( source, parameters, null );
	}

	public static void configureLogging( Object source, com.xeomar.util.Parameters parameters, Path programDataFolder ) {
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
		String file = parameters.get( LogFlag.LOG_FILE );

		boolean nameOnly = file != null && new File( file ).toString().equals( new File( file ).getName() );
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

		// Set the program log level
		builder.append( source.getClass().getPackage().getName() );
		builder.append( ".level=" );
		builder.append( getLogLevel( level ) );
		builder.append( "\n" );

		// Initialize the logging
		try {
			InputStream input = new ByteArrayInputStream( builder.toString().getBytes( "utf-8" ) );
			LogManager.getLogManager().readConfiguration( input );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}
	}

	private static String getLogFilePattern( String path ) {
		Path userHome = Paths.get( System.getProperty( "user.home" ) );
		return "%h/" + userHome.relativize( Paths.get( path ) ).toString().replace( '\\', '/' );
	}

	private static String getLogLevel( String level ) {
		String result = level == null ? "INFO" : level.toUpperCase();

		switch( result ) {
			case "NONE": {
				result = "OFF";
				break;
			}
			case "ERROR": {
				result = "SEVERE";
				break;
			}
			case "WARN": {
				result = "WARNING";
				break;
			}
			case "INFO": {
				//result = "INFO";
				break;
			}
			case "DEBUG": {
				result = "FINE";
				break;
			}
			case "TRACE": {
				result = "FINEST";
				break;
			}
			case "ALL": {
				//result = "ALL";
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
