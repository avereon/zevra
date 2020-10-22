package com.avereon.util;

import java.util.Arrays;

public class ArrayUtil {

	@SafeVarargs
	public static <T> T[] append( T[] source, T... other ) {
		return concat( source, other );
	}

	@SafeVarargs
	public static <T> T[] concat( T[] source, T... other ) {
		T[] concat = Arrays.copyOf( source, source.length + other.length );
		System.arraycopy( other, 0, concat, source.length, other.length );
		return concat;
	}

}
