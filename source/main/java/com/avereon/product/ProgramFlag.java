package com.avereon.product;

import com.avereon.util.LogFlag;

import java.util.Set;

public interface ProgramFlag extends LogFlag {

	/**
	 * Start the program without showing the workspaces. Combine this with the
	 * NOSPLASH flag to start the program "in the background".
	 */
	String DAEMON = "--daemon";

	/**
	 * Start the program normally even if the daemon flag is specified. This is
	 * most commonly used when restarting after an update.
	 */
	String NODAEMON = "--nodaemon";

	/**
	 * Disable a specific mod. May be specified more than once.
	 */
	String DISABLE_MOD = "--disable-mod";

	/**
	 * Enable a specific mod. May be specified more than once. Overrides {@link #DISABLE_MOD}.
	 */
	String ENABLE_MOD = "--enable-mod";

	/**
	 * Send greeting to already running instance.
	 */
	String HELLO = "--hello";

	/**
	 * Print the help information and exit.
	 */
	String HELP = "--help";

	/**
	 * Specify a different program home than the default.
	 */
	String HOME = "--home";

	/**
	 * Don't show the splash screen at startup. Useful in combination with the
	 * DAEMON flag.
	 */
	String NOSPLASH = "--nosplash";

	/**
	 * Explicitly turn off automated updates.
	 */
	String NOUPDATE = "--noupdate";

	/**
	 * Specify the execution profile. Special values are 'dev' and 'test'.
	 * Developers can use the 'dev' value to run the program as a different
	 * instance than their production instance. The 'test' value is used during
	 * unit and integration tests.
	 */
	String PROFILE = "--profile";

	/**
	 * Print the status information and exit.
	 */
	String STATUS = "--status";

	/**
	 * Request the program stop.
	 */
	String STOP = "--stop";

	/**
	 * Print the version information and exit.
	 */
	String VERSION = "--version";

	/**
	 * Watch the host instance
	 */
	String WATCH = "--watch";

	/**
	 * Reset the program settings to defaults.
	 */
	String RESET = "--reset";

	/**
	 * Flags that a host will respond to without showing the program.
	 */
	Set<String> QUIET_ACTIONS = Set.of( HELLO, STATUS, STOP, WATCH );

	/**
	 * All flags that a host will respond to.
	 */
	Set<String> ACTIONS = QUIET_ACTIONS;

}
