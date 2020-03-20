package com.avereon.math;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ArithmeticTest {

	@Test
	void testDefaultDigits() {
		assertThat( Arithmetic.DEFAULT_DIGITS, is( 12 ) );
	}

	@Test
	public void testDefaultPrecision() {
		assertThat( Arithmetic.DEFAULT_PRECISION, is( 0.000000000001 ) );
	}

	//private void assertThat( Object b, Object a ) {}

	@Test
	public void testSign() {
		assertThat( Arithmetic.sign( -Math.PI ), is( -1.0 ) );
		assertThat( Arithmetic.sign( 0 ), is( 1.0 ) );
		assertThat( Arithmetic.sign( Math.PI ), is( 1.0 ) );
	}

	@Test
	public void testTrim() {
		assertThat( Arithmetic.trim( 0.0000000000001 ), is( 0.000000000000 ) );
		assertThat( Arithmetic.trim( 0.0000000000004 ), is( 0.000000000000 ) );
		assertThat( Arithmetic.trim( 0.0000000000005 ), is( 0.000000000001 ) );
		assertThat( Arithmetic.trim( 0.0000000000010 ), is( 0.000000000001 ) );
	}

	@Test
	public void testTrimWithDigits() {
		assertThat( Arithmetic.trim( 2.24412, 2 ), is( 2.24 ) );
		assertThat( Arithmetic.trim( 2.24658, 2 ), is( 2.25 ) );
		assertThat( Arithmetic.trim( 2.25423, 2 ), is( 2.25 ) );
		assertThat( Arithmetic.trim( 2.25658, 2 ), is( 2.26 ) );

		assertThat( Arithmetic.trim( 0.0001, 3 ), is( 0.000 ) );
		assertThat( Arithmetic.trim( 0.0004, 3 ), is( 0.000 ) );
		assertThat( Arithmetic.trim( 0.0005, 3 ), is( 0.001 ) );
		assertThat( Arithmetic.trim( 0.0010, 3 ), is( 0.001 ) );
	}

	@Test
	public void testNearest() {
		assertThat( Arithmetic.nearest( 0.125, 0.5 ), is( 0.0 ) );
		assertThat( Arithmetic.nearest( 0.375, 0.5 ), is( 0.5 ) );
	}

	@Test
	public void testNearestAbove() {
		assertThat( Arithmetic.nearestAbove( 0.0, 0.5 ), is( 0.0 ) );
		assertThat( Arithmetic.nearestAbove( 0.125, 0.5 ), is( 0.5 ) );
		assertThat( Arithmetic.nearestAbove( 0.375, 0.5 ), is( 0.5 ) );
		assertThat( Arithmetic.nearestAbove( 0.5, 0.5 ), is( 0.5 ) );
	}

	@Test
	public void testNearestBelow() {
		assertThat( Arithmetic.nearestBelow( 0.0, 0.5 ), is( 0.0 ) );
		assertThat( Arithmetic.nearestBelow( 0.125, 0.5 ), is( 0.0 ) );
		assertThat( Arithmetic.nearestBelow( 0.375, 0.5 ), is( 0.0 ) );
		assertThat( Arithmetic.nearestBelow( 0.5, 0.5 ), is( 0.5 ) );
	}

	@Test
	public void testDeterminant2() {
		assertThat( Arithmetic.determinant( 1, 2, 3, 4 ), is( -2.0 ) );
	}

	@Test
	public void testDeterminant3() {
		assertThat( Arithmetic.determinant( 1, 2, 3, 4, 5, 6, 7, 8, 9 ), is( 0.0 ) );
	}

}
