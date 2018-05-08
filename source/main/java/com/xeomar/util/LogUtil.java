package com.xeomar.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

public class LogUtil {

	public static Logger get( Class<?> clazz ) {
		return LoggerFactory.getLogger( clazz );
	}

	public static void configureLogging( Object source, com.xeomar.util.Parameters parameters ) {
		configureLogging( "java", source, parameters );
	}

	public static void configureLogging( String name, Object source, com.xeomar.util.Parameters parameters ) {
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
		String file = parameters.get( LogFlag.LOG_FILE);
		StringBuilder builder = new StringBuilder();

		// Configure the simple formatter
		// https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html#syntax
		builder.append( ProgramFormatter.class.getName() + ".format=%1$tF %1$tT.%1$tL %4$s %2$s: %5$s %6$s%n\n" );

		// Add the log console handler
		if( file == null ) {
			builder.append( "handlers=java.util.logging.ConsoleHandler\n" );
		} else {
			builder.append( "handlers=java.util.logging.ConsoleHandler,java.util.logging.FileHandler\n" );
		}

		// Configure the console handler
		builder.append( "java.util.logging.ConsoleHandler.level=FINEST\n" );
		builder.append( "java.util.logging.ConsoleHandler.formatter=" + ProgramFormatter.class.getName() + "\n" );

		if( file != null ) {
			// Configure the file handler
			// %h - Home folder
			// %u - Unique number
			//builder.append( "java.util.logging.FileHandler.pattern=%h/" + name + "%u.log\n" );
			builder.append( "java.util.logging.FileHandler.pattern=%h/" + file + "\n" );
			builder.append( "java.util.logging.FileHandler.level=FINEST\n" );
			builder.append( "java.util.logging.FileHandler.limit=50000\n" );
			builder.append( "java.util.logging.FileHandler.count=1\n" );
			//builder.append( "java.util.logging.FileHandler.formatter=" + XMLFormatter.class.getName() + "\n" );
			builder.append( "java.util.logging.FileHandler.formatter=" + ProgramFormatter.class.getName() + "\n" );
		}

		// Set the default log level
		builder.append( ".level=" );
		builder.append( getDefaultLogLevel( level ) );
		builder.append( "\n" );

		// Set the program log level
		builder.append( source.getClass().getPackage().getName() );
		builder.append( ".level=" );
		builder.append( getProgramLogLevel( level ) );
		builder.append( "\n" );

		// Initialize the logging
		try {
			InputStream input = new ByteArrayInputStream( builder.toString().getBytes( "utf-8" ) );
			LogManager.getLogManager().readConfiguration( input );
		} catch( IOException exception ) {
			exception.printStackTrace( System.err );
		}
	}

	private static String getDefaultLogLevel( String level ) {
		String result = level == null ? "info" : level.toUpperCase();

		switch( result ) {
			case "ERROR": {
				result = "SEVERE";
				break;
			}
			case "WARN": {
				result = "WARNING";
				break;
			}
			default: {
				result = "INFO";
				break;
			}
		}

		return result;
	}

	private static String getProgramLogLevel( String level ) {
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
				result = "INFO";
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
			default: {
				result = "ERROR";
				break;
			}
		}

		return result;
	}

}
