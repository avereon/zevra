package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArrayUtilTest {

	@Test
	void testAppend() {
		Integer[] source = new Integer[] {1,2,3};
		Integer four = 4;
		Integer five = 5;

		Integer[] check0 = new Integer[] {source[0], source[1], source[2]};
		Integer[] check1 = new Integer[] {source[0], source[1], source[2], four};
		Integer[] check2 = new Integer[] {source[0], source[1], source[2], four, five};

		assertThat( ArrayUtil.append( source ), is( check0 ) );
		assertThat( ArrayUtil.append( source, four ), is( check1 ) );
		assertThat( ArrayUtil.append( source, four, five ), is( check2 ) );
	}

	@Test
	void testConcat() {
		Integer[] source = new Integer[] {1,2,3};
		Integer four = 4;
		Integer five = 5;

		Integer[] check0 = new Integer[] {source[0], source[1], source[2]};
		Integer[] check1 = new Integer[] {source[0], source[1], source[2], four};
		Integer[] check2 = new Integer[] {source[0], source[1], source[2], four, five};

		assertThat( ArrayUtil.concat( source ), is( check0 ) );
		assertThat( ArrayUtil.concat( source, four ), is( check1 ) );
		assertThat( ArrayUtil.concat( source, four, five ), is( check2 ) );
	}

}
