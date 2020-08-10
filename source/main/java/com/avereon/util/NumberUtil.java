package com.avereon.util;

public class NumberUtil {

	public static int parseInt( Object object, int defaultValue ) {
		try {
			return Integer.parseInt( String.valueOf( object ) );
		} catch( NumberFormatException exception ) {
			return defaultValue;
		}
	}

	public static long parseLong( Object object, long defaultValue ) {
		try {
			return Long.parseLong( String.valueOf( object ) );
		} catch( NumberFormatException exception ) {
			return defaultValue;
		}
	}

	public static float parseFloat( Object object, float defaultValue ) {
		try {
			return Float.parseFloat( String.valueOf( object ) );
		} catch( NumberFormatException exception ) {
			return defaultValue;
		}
	}

	public static double parseDouble( Object object, double defaultValue ) {
		try {
			return Double.parseDouble( String.valueOf( object ) );
		} catch( NumberFormatException exception ) {
			return defaultValue;
		}
	}

}
