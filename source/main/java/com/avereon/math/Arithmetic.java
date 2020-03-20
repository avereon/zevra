package com.avereon.math;

public class Arithmetic {

	public static final int DEFAULT_DIGITS = 12;

	public static final double DEFAULT_PRECISION = 1.0 / Math.pow( 10, DEFAULT_DIGITS );

	private static final double[] factors = new double[ DEFAULT_DIGITS << 1 ];

	static {
		for( int index = 0; index < factors.length; index++ ) {
			factors[ index ] = Math.pow( 10, index );
		}
	}

	public static double sign( double number ) {
		return number < 0 ? -1.0 : 1.0;
	}

	public static double trim( double value ) {
		return trim( value, DEFAULT_DIGITS );
	}

	public static double trim( double value, int digits ) {
		return Math.floor( value * factors[ digits ] + 0.5 ) / factors[ digits ];
	}

	public static double round( double value ) {
		return Math.floor( value + 0.5 );
	}

	public static double nearest( double value, double precision ) {
		return round( value / precision ) * precision;
	}

	public static double nearestAbove( double value, double precision ) {
		double newValue = nearest( value, precision );
		return newValue >= value ? newValue : newValue + precision;
	}

	public static double nearestBelow( double value, double precision ) {
		double newValue = nearest( value, precision );
		return newValue <= value ? newValue : newValue - precision;
	}

	/**
	 * Evaluate a 2x2 matrix determinant.
	 *
	 * @param a1
	 * @param a2
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static double determinant( double a1, double a2, double b1, double b2 ) {
		return a1 * b2 - a2 * b1;
	}

	/**
	 * Evaluate a 3x3 matrix determinant.
	 *
	 * @param a1
	 * @param a2
	 * @param a3
	 * @param b1
	 * @param b2
	 * @param b3
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return
	 */
	public static double determinant( double a1, double a2, double a3, double b1, double b2, double b3, double c1, double c2, double c3 ) {
		double a = a1 * b2 * c3;
		double b = a1 * b3 * c2;
		double c = a2 * b1 * c3;
		double d = a2 * b3 * c1;
		double e = a3 * b1 * c2;
		double f = a3 * b2 * c1;
		return a - b - c + d + e - f;
	}

}
