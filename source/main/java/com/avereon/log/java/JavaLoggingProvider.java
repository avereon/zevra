package com.avereon.log.java;

import com.avereon.log.provider.AbstractLoggingProvider;
import com.avereon.log.provider.LoggerWrapper;

public class JavaLoggingProvider extends AbstractLoggingProvider {

	public LoggerWrapper getLoggerWrapper( String name ) {
		return new JavaLoggingLoggerWrapper( java.util.logging.Logger.getLogger( name ) );
	}

}
