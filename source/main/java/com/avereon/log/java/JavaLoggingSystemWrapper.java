package com.avereon.log.java;

import com.avereon.log.provider.AbstractSystemProvider;
import com.avereon.log.provider.LoggerProvider;

public class JavaLoggingSystemWrapper extends AbstractSystemProvider {

	public LoggerProvider getLoggerProvider( String name ) {
		return new JavaLoggingLoggerWrapper( java.util.logging.Logger.getLogger( name ) );
	}

}
