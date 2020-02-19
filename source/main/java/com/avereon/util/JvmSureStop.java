package com.avereon.util;

import java.util.Arrays;
import java.util.Map;

/**
 * This shutdown hook is used to ensure that the program eventually terminates
 * given a specific amount of time. There are times when misbehaving threads
 * cause the program not to exit cleanly. This hook waits for the program to
 * terminate for a specific amount of time. Once that time expires this hook
 * prints the list of running non-daemon threads and forces the program to stop
 * by calling Runtime.halt(). Unfortunately this also causes other shutdown
 * hooks to not execute if they have not started.
 *
 * @author soderquistmv
 */
public class JvmSureStop extends Thread {

	private static final System.Logger log = Log.get();

	/**
	 * The amount of time to give the JVM to exit cleanly. After this amount of
	 * time the JVM is halted by calling Runtime.getRuntime().halt().
	 */
	public static final int DEFAULT_WAIT_DELAY = 10000;

	private int delay;

	public JvmSureStop() {
		this( DEFAULT_WAIT_DELAY );
	}

	public JvmSureStop( int delay ) {
		super( "JVM Sure Stop" );
		this.delay = delay;
		setDaemon( true );
	}

	@Override
	public void run() {
		if( TestUtil.isTest() ) return;

		try {
			Thread.sleep( delay );
		} catch( InterruptedException exception ) {
			return;
		}

		log.log( Log.ERROR, "JVM did not exit cleanly. Here are the running non-daemon threads:" );
		Map<Thread, StackTraceElement[]> threadStacks = Thread.getAllStackTraces();
		threadStacks.keySet().stream().filter( t -> !t.isDaemon() ).forEach( thread -> {
			log.log( Log.WARN, "Thread: " + thread.getId() + " " + thread.getName() + "[g=" + thread.getThreadGroup() + "]" );
			Arrays.stream( threadStacks.get( thread ) ).forEach( e -> log.log( Log.INFO, "  " + e ) );
		} );

		log.log( Log.ERROR, "Halting now!" );
		ThreadUtil.pause( 200 );

		Runtime.getRuntime().halt( -1 );
	}

}
