package com.avereon.util;

public class NumberUtil {

	public static int parseInt( Object object, int defaultValue ) {
		try {
			return Integer.parseInt( String.valueOf( object ) );
		} catch( NumberFormatException exception ) {
			return defaultValue;
		}
	}

}
