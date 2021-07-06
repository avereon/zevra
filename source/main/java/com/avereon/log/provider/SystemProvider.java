package com.avereon.log.provider;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public interface SystemProvider {

	LoggerProvider getLoggerProvider( String name );

	static long getCurrentTimeNanos() {
		return MILLISECONDS.toNanos( System.currentTimeMillis() );
	}

}
