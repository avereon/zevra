package com.avereon.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Implementation found at: https://stackoverflow.com/questions/7085990/java-enumeration-from-setstring
 */
public class IteratorEnumerator<E> implements Enumeration<E> {

	private final Iterator<E> iterator;

	public IteratorEnumerator( Iterator<E> iterator ) {
		this.iterator = iterator;
	}

	public E nextElement() {
		return iterator.next();
	}

	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

}