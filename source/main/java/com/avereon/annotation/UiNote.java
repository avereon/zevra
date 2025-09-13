package com.avereon.annotation;

public interface UiNote {

	/**
	 * Indicates that any thread may safely access this method and assumptions
	 * about thread execution will be managed by the method implementation. This
	 * is common when working with UI implementations where any thread can request
	 * a UI change, but the change must be made on the UI thread. The method may
	 * note that this method is safe for any thread to call because it will ensure
	 * the state modifications will be delegated to the correct thread.
	 */
	String THREAD_SAFE = "any-thread";

}
