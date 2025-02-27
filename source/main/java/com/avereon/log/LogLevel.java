package com.avereon.log;

import java.util.logging.Level;

public class LogLevel extends Level {

	public static final LogLevel NONE = new LogLevel( "NONE", Level.OFF.intValue() );
	public static final LogLevel ERROR = new LogLevel( "ERROR", Level.SEVERE.intValue() );
	public static final LogLevel WARN = new LogLevel( "WARN", Level.WARNING.intValue() );
	public static final LogLevel INFO = new LogLevel( "INFO", Level.INFO.intValue() );
	public static final LogLevel DEBUG = new LogLevel( "DEBUG", Level.FINE.intValue() );
	public static final LogLevel TRACE = new LogLevel( "TRACE", Level.FINEST.intValue() );
	public static final LogLevel ALL = new LogLevel( "ALL", Level.ALL.intValue() );

	protected LogLevel( String name, int value ) {
		super( name, value );
	}

	protected LogLevel( String name, int value, String resourceBundleName ) {
		super( name, value, resourceBundleName );
	}

}
