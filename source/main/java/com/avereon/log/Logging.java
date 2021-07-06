package com.avereon.log;

import com.avereon.log.java.JavaLoggingLoggingProvider;
import com.avereon.log.provider.LoggingProvider;

public class Logging {

	private static LoggingProvider provider = new JavaLoggingLoggingProvider();

	public static LoggingProvider getLoggingProvider() {
		return provider;
	}

	public static void setLoggingProvider( LoggingProvider provider ) {
		Logging.provider = provider;
	}

	public static Logger create(java.lang.Class<?> clazz ) {
		return new Logger( getLoggingProvider().getLoggerWrapper( clazz.getName() ) );
	}

}
