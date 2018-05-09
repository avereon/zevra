package com.xeomar.util;

public interface LogFlag {

	/**
	 * Turn file log append on. The default is off.
	 */
	String LOG_APPEND = "--log-append";

	/**
	 * Valid values are:
	 * <li>off</li>
	 * <li>error</li>
	 * <li>warn</li>
	 * <li>info</li>
	 * <li>debug</li>
	 * <li>trace</li>
	 * <li>all</li>
	 */
	String LOG_LEVEL = "--log-level";

	/**
	 * The log file pattern according to the {@link java.util.logging.FileHandler}.
	 */
	String LOG_FILE = "--log-file";

}
