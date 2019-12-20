package com.avereon.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Implementation found at: https://stackoverflow.com/questions/7085990/java-enumeration-from-setstring
 */
class IteratorEnumeration<E> implements Enumeration<E> {

	private final Iterator<E> iterator;

	public IteratorEnumeration( Iterator<E> iterator ) {
		this.iterator = iterator;
	}

	public E nextElement() {
		return iterator.next();
	}

	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

}