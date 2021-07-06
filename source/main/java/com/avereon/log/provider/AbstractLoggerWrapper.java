package com.avereon.log.provider;

import java.util.logging.Level;

public abstract class AbstractLoggerWrapper implements LoggerProvider{

	@Override
	public String getLoggerName() {
		return null;
	}

	@Override
	public boolean isLoggable( Level level ) {
		return false;
	}

}
