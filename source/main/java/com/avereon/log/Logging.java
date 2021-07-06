package com.avereon.log;

import com.avereon.log.provider.LoggingProvider;

public class Logging {

	private static LoggingProvider provider;

	public static LoggingProvider getLoggingProvider() {
		return provider;
	}

	public static void setLoggingProvider( LoggingProvider provider ) {
		Logging.provider = provider;
	}

}
