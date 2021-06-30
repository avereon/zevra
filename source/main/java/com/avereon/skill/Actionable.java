package com.avereon.skill;

/**
 * Just like {@link Runnable} except that managed exceptions are allowed.
 */
@FunctionalInterface
public interface Actionable {

	void act() throws Exception;

}
