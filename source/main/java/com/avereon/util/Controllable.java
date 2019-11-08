package com.avereon.util;

/**
 * The Controllable interface represents classes that can be "controlled" by
 * starting and stopping.
 *
 * @param <T>
 */
@SuppressWarnings( { "unused", "UnusedReturnValue" } )
public interface Controllable<T> {

	boolean isRunning();

	T start();

//	T awaitStart( long timeout, TimeUnit unit ) throws InterruptedException;
//
//	T restart();
//
//	T awaitRestart( long timeout, TimeUnit unit ) throws InterruptedException;

	T stop();

//	T awaitStop( long timeout, TimeUnit unit ) throws InterruptedException;

}
