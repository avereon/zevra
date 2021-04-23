package com.avereon.util;

/**
 * Just like {@link Runnable} except that managed exceptions are allowed.
 */
@FunctionalInterface
public interface Actable {

	void act() throws Exception;

}
