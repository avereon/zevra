package com.avereon.skill;

/**
 * The Controllable interface provides an API for classes that are "controlled"
 * by {@link #start()} and {@link #stop()} methods.
 * <p/>
 * Implementations of this interface are expected to be synchronous. This means
 * that when execution exits the {@link #start()} method the Controllable is
 * started and this {@link #isRunning()} method will return true. Similarly,
 * when execution exits the {@link #stop()} method the Controllable is stopped
 * and the {@link #isRunning()} method will return false.
 * <p/>
 * No expectation whether {@link #start()} can be called again after
 * {@link #stop()} is called is not defined by this interface.
 *
 * @param <T> The type of the controllable
 */
@SuppressWarnings( { "unused", "UnusedReturnValue" } )
public interface Controllable<T> {

	boolean isRunning();

	T start();

	T stop();

}
