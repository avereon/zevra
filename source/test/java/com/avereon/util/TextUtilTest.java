package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class TextUtilTest {

	@Test
	void testIsEmpty() {
		assertThat( TextUtil.isEmpty( null ), is( true ) );
		assertThat( TextUtil.isEmpty( "" ), is( true ) );
		assertThat( TextUtil.isEmpty( " " ), is( true ) );
		assertThat( TextUtil.isEmpty( "." ), is( false ) );
	}

	@Test
	@SuppressWarnings( "ConstantConditions" )
	void testAreEqual() {
		assertThat( TextUtil.areEqual( null, null ), is( true ) );
		assertThat( TextUtil.areEqual( "", "" ), is( true ) );
		assertThat( TextUtil.areEqual( " ", " " ), is( true ) );
		assertThat( TextUtil.areEqual( "a", "a" ), is( true ) );

		assertThat( TextUtil.areEqual( null, "" ), is( false ) );
		assertThat( TextUtil.areEqual( "", null ), is( false ) );
		assertThat( TextUtil.areEqual( "a", "b" ), is( false ) );
		assertThat( TextUtil.areEqual( "b", "a" ), is( false ) );
	}

	@Test
	@SuppressWarnings( "ConstantConditions" )
	void testAreEqualIgnoreCase() {
		assertThat( TextUtil.areEqualIgnoreCase( null, null ), is( true ) );
		assertThat( TextUtil.areEqualIgnoreCase( "", "" ), is( true ) );
		assertThat( TextUtil.areEqualIgnoreCase( " ", " " ), is( true ) );
		assertThat( TextUtil.areEqualIgnoreCase( "A", "a" ), is( true ) );
		assertThat( TextUtil.areEqualIgnoreCase( "a", "A" ), is( true ) );

		assertThat( TextUtil.areEqualIgnoreCase( null, "" ), is( false ) );
		assertThat( TextUtil.areEqualIgnoreCase( "", null ), is( false ) );
		assertThat( TextUtil.areEqual( "A", "b" ), is( false ) );
		assertThat( TextUtil.areEqual( "B", "a" ), is( false ) );
	}

	@Test
	void testAreSame() {
		assertThat( TextUtil.areSame( null, null ), is( true ) );
		assertThat( TextUtil.areSame( "", "" ), is( true ) );
		assertThat( TextUtil.areSame( " ", " " ), is( true ) );
		assertThat( TextUtil.areSame( "a", "a" ), is( true ) );

		assertThat( TextUtil.areSame( null, "" ), is( true ) );
		assertThat( TextUtil.areSame( "", null ), is( true ) );
		assertThat( TextUtil.areSame( null, " " ), is( true ) );

		assertThat( TextUtil.areSame( null, "a" ), is( false ) );
		assertThat( TextUtil.areSame( "", "a" ), is( false ) );
		assertThat( TextUtil.areSame( " ", "a" ), is( false ) );
	}

	@Test
	void testCompare() {
		assertEquals( 0, TextUtil.compare( null, null ) );
		assertEquals( -1, TextUtil.compare( null, "" ) );
		assertEquals( 1, TextUtil.compare( "", null ) );
		assertEquals( 0, TextUtil.compare( "", "" ) );

		assertEquals( 0, TextUtil.compare( "a", "a" ) );
		assertEquals( -1, TextUtil.compare( "a", "b" ) );
		assertEquals( 1, TextUtil.compare( "b", "a" ) );
	}

	@Test
	void testCompareIgnoreCase() {
		assertEquals( 0, TextUtil.compareIgnoreCase( null, null ) );
		assertEquals( -1, TextUtil.compareIgnoreCase( null, "" ) );
		assertEquals( 1, TextUtil.compareIgnoreCase( "", null ) );
		assertEquals( 0, TextUtil.compareIgnoreCase( "", "" ) );

		assertEquals( 0, TextUtil.compareIgnoreCase( "A", "a" ) );
		assertEquals( 0, TextUtil.compareIgnoreCase( "a", "A" ) );
		assertEquals( -1, TextUtil.compareIgnoreCase( "a", "B" ) );
		assertEquals( -1, TextUtil.compareIgnoreCase( "A", "b" ) );
		assertEquals( 1, TextUtil.compareIgnoreCase( "B", "a" ) );
		assertEquals( 1, TextUtil.compareIgnoreCase( "b", "A" ) );
	}

	@Test
	void testCleanNull() {
		assertNull( TextUtil.cleanNull( null ) );
		assertNull( TextUtil.cleanNull( "" ) );
		assertNull( TextUtil.cleanNull( " " ) );
		assertEquals( "a", TextUtil.cleanNull( " a " ) );
	}

	@Test
	void testCleanEmpty() {
		assertEquals( "", TextUtil.cleanEmpty( null ) );
		assertEquals( "", TextUtil.cleanEmpty( "" ) );
		assertEquals( "", TextUtil.cleanEmpty( " " ) );
		assertEquals( "a", TextUtil.cleanEmpty( " a " ) );
	}

	@Test
	void testConcatenate() {
		assertEquals( "Count: 10", TextUtil.concatenate( "Count: ", 10 ) );
		assertEquals( "Flag: false", TextUtil.concatenate( "Flag: ", false ) );
		assertEquals( "Test String", TextUtil.concatenate( "Test", " ", "String" ) );
	}

	@Test
	void testToPrintableStringUsingByte() {
		assertEquals( "[0]", TextUtil.toPrintableString( (byte)0 ) );
		assertEquals( "[27]", TextUtil.toPrintableString( (byte)27 ) );
		assertEquals( "[31]", TextUtil.toPrintableString( (byte)31 ) );
		assertEquals( " ", TextUtil.toPrintableString( (byte)32 ) );
		assertEquals( "A", TextUtil.toPrintableString( (byte)65 ) );
		assertEquals( "~", TextUtil.toPrintableString( (byte)126 ) );
		assertEquals( "[127]", TextUtil.toPrintableString( (byte)127 ) );
		assertEquals( "[128]", TextUtil.toPrintableString( (byte)128 ) );
		assertEquals( "[255]", TextUtil.toPrintableString( (byte)255 ) );

		assertEquals( "[255]", TextUtil.toPrintableString( (byte)-1 ) );
		assertEquals( "[0]", TextUtil.toPrintableString( (byte)256 ) );
	}

	@Test
	void testToPrintableString() {
		assertEquals( "[0]", TextUtil.toPrintableString( (char)0 ) );
		assertEquals( "[27]", TextUtil.toPrintableString( (char)27 ) );
		assertEquals( "[31]", TextUtil.toPrintableString( (char)31 ) );
		assertEquals( " ", TextUtil.toPrintableString( (char)32 ) );
		assertEquals( "A", TextUtil.toPrintableString( (char)65 ) );
		assertEquals( "~", TextUtil.toPrintableString( (char)126 ) );
		assertEquals( "[127]", TextUtil.toPrintableString( (char)127 ) );
		assertEquals( "[255]", TextUtil.toPrintableString( (char)255 ) );
	}

	@Test
	void testToHexEncodedStringWithBytes() {
		Charset encoding = StandardCharsets.ISO_8859_1;
		assertEquals( "", TextUtil.toHexEncodedString( "".getBytes( encoding ) ) );
		assertEquals( "00", TextUtil.toHexEncodedString( "\u0000".getBytes( encoding ) ) );
		assertEquals( "0001", TextUtil.toHexEncodedString( "\u0000\u0001".getBytes( encoding ) ) );
		assertEquals( "ff01", TextUtil.toHexEncodedString( "\u00ff\u0001".getBytes( encoding ) ) );
		assertEquals( "00010f", TextUtil.toHexEncodedString( "\u0000\u0001\u000f".getBytes( encoding ) ) );
		assertEquals( "74657374", TextUtil.toHexEncodedString( "test".getBytes( encoding ) ) );
	}

	@Test
	void testHexEncodeWithString() {
		assertEquals( "", TextUtil.hexEncode( "" ) );
		assertEquals( "0000", TextUtil.hexEncode( "\u0000" ) );
		assertEquals( "00000001", TextUtil.hexEncode( "\u0000\u0001" ) );
		assertEquals( "00000001000f", TextUtil.hexEncode( "\u0000\u0001\u000f" ) );
		assertEquals( "0074006500730074", TextUtil.hexEncode( "test" ) );
	}

	@Test
	void testHexDecodeWithString() {
		assertNull( TextUtil.hexDecode( null ) );
		assertEquals( "", TextUtil.hexDecode( "" ) );
		assertEquals( "\u0000", TextUtil.hexDecode( "0000" ) );
		assertEquals( "\u0000\u0001", TextUtil.hexDecode( "00000001" ) );
		assertEquals( "\u0000\u0001\u000f", TextUtil.hexDecode( "00000001000f" ) );
		assertEquals( "test", TextUtil.hexDecode( "0074006500730074" ) );

	}

	@Test
	void testHexByteEncode() {
		Charset encoding = StandardCharsets.ISO_8859_1;
		assertEquals( "", TextUtil.secureHexByteEncode( "".getBytes( encoding ) ) );
		assertEquals( "00", TextUtil.secureHexByteEncode( "\u0000".getBytes( encoding ) ) );
		assertEquals( "0001", TextUtil.secureHexByteEncode( "\u0000\u0001".getBytes( encoding ) ) );
		assertEquals( "ff01", TextUtil.secureHexByteEncode( "\u00ff\u0001".getBytes( encoding ) ) );
		assertEquals( "00010f", TextUtil.secureHexByteEncode( "\u0000\u0001\u000f".getBytes( encoding ) ) );
		assertEquals( "74657374", TextUtil.secureHexByteEncode( "test".getBytes( encoding ) ) );
	}

	@Test
	void testHexByteDecode() {
		Charset encoding = StandardCharsets.ISO_8859_1;
		assertNull( TextUtil.secureHexByteDecode( null ) );
		assertEquals( "", new String( TextUtil.secureHexByteDecode( "" ), encoding ) );
		assertEquals( "\u0000", new String( TextUtil.secureHexByteDecode( "00" ), encoding ) );
		assertEquals( "\u0000\u0001", new String( TextUtil.secureHexByteDecode( "0001" ), encoding ) );
		assertEquals( "\u00ff\u0001", new String( TextUtil.secureHexByteDecode( "ff01" ), encoding ) );
		assertEquals( "\u0000\u0001\u000f", new String( TextUtil.secureHexByteDecode( "00010f" ), encoding ) );
		assertEquals( "test", new String( TextUtil.secureHexByteDecode( "74657374" ), encoding ) );
	}

	@Test
	void testHexCharEncode() {
		assertNull( TextUtil.secureHexEncode( null ) );
		assertEquals( "", TextUtil.secureHexEncode( "".toCharArray() ) );
		assertEquals( "0000", TextUtil.secureHexEncode( "\u0000".toCharArray() ) );
		assertEquals( "00000001", TextUtil.secureHexEncode( "\u0000\u0001".toCharArray() ) );
		assertEquals( "00000001000f", TextUtil.secureHexEncode( "\u0000\u0001\u000f".toCharArray() ) );
		assertEquals( "0074006500730074", TextUtil.secureHexEncode( "test".toCharArray() ) );
	}

	@Test
	void testHexCharDecode() {
		assertNull( TextUtil.secureHexDecode( null ) );
		assertEquals( "", new String( TextUtil.secureHexDecode( "" ) ) );
		assertEquals( "\u0000", new String( TextUtil.secureHexDecode( "0000" ) ) );
		assertEquals( "\u0000\u0001", new String( TextUtil.secureHexDecode( "00000001" ) ) );
		assertEquals( "\u0000\u0001\u000f", new String( TextUtil.secureHexDecode( "00000001000f" ) ) );
		assertEquals( "test", new String( TextUtil.secureHexDecode( "0074006500730074" ) ) );
	}

	@Test
	void testIsInteger() {
		assertFalse( TextUtil.isInteger( null ) );
		assertFalse( TextUtil.isInteger( "" ) );

		assertFalse( TextUtil.isInteger( "1e-10" ) );
		assertFalse( TextUtil.isInteger( "1.0" ) );
		assertFalse( TextUtil.isInteger( "2147483648" ) );
		assertFalse( TextUtil.isInteger( "-2147483649" ) );

		assertTrue( TextUtil.isInteger( "0" ) );
		assertTrue( TextUtil.isInteger( "2147483647" ) );
		assertTrue( TextUtil.isInteger( "-2147483648" ) );
	}

	@Test
	void testIsLong() {
		assertFalse( TextUtil.isLong( null ) );
		assertFalse( TextUtil.isLong( "" ) );

		assertFalse( TextUtil.isLong( "1e-10" ) );
		assertFalse( TextUtil.isLong( "1.0" ) );
		assertFalse( TextUtil.isLong( "9223372036854775808" ) );
		assertFalse( TextUtil.isLong( "-9223372036854775809" ) );

		assertTrue( TextUtil.isLong( "0" ) );
		assertTrue( TextUtil.isLong( "9223372036854775807" ) );
		assertTrue( TextUtil.isLong( "-9223372036854775808" ) );
	}

	@Test
	void testIsFloat() {
		assertFalse( TextUtil.isFloat( null ) );
		assertFalse( TextUtil.isFloat( "" ) );

		assertTrue( TextUtil.isFloat( "0" ) );
		assertTrue( TextUtil.isFloat( "1.0" ) );
		assertTrue( TextUtil.isFloat( "1e10" ) );
		assertTrue( TextUtil.isFloat( "1e-10" ) );
		assertTrue( TextUtil.isFloat( "-1e10" ) );
		assertTrue( TextUtil.isFloat( "-1e-10" ) );
	}

	@Test
	void testIsDouble() {
		assertFalse( TextUtil.isDouble( null ) );
		assertFalse( TextUtil.isDouble( "" ) );

		assertTrue( TextUtil.isDouble( "0" ) );
		assertTrue( TextUtil.isDouble( "1.0" ) );
		assertTrue( TextUtil.isDouble( "1e10" ) );
		assertTrue( TextUtil.isDouble( "1e-10" ) );
		assertTrue( TextUtil.isDouble( "-1e10" ) );
		assertTrue( TextUtil.isDouble( "-1e-10" ) );
	}

	@Test
	void testCapitalize() {
		assertNull( TextUtil.capitalize( null ) );
		assertEquals( "", TextUtil.capitalize( "" ) );
		assertEquals( "Test", TextUtil.capitalize( "test" ) );
		assertEquals( "New brunswick", TextUtil.capitalize( "new brunswick" ) );
	}

	@Test
	void testJustify() {
		assertEquals( "        ", TextUtil.justify( TextUtil.LEFT, "", 8 ) );
		assertEquals( "X       ", TextUtil.justify( TextUtil.LEFT, "X", 8 ) );
		assertEquals( "        ", TextUtil.justify( TextUtil.CENTER, "", 8 ) );
		assertEquals( "   X    ", TextUtil.justify( TextUtil.CENTER, "X", 8 ) );
		assertEquals( "   XX   ", TextUtil.justify( TextUtil.CENTER, "XX", 8 ) );
		assertEquals( "        ", TextUtil.justify( TextUtil.RIGHT, "", 8 ) );
		assertEquals( "       X", TextUtil.justify( TextUtil.RIGHT, "X", 8 ) );
	}

	@Test
	void testJustifyWithChar() {
		assertEquals( "........", TextUtil.justify( TextUtil.LEFT, "", 8, '.' ) );
		assertEquals( "X.......", TextUtil.justify( TextUtil.LEFT, "X", 8, '.' ) );
		assertEquals( "........", TextUtil.justify( TextUtil.CENTER, "", 8, '.' ) );
		assertEquals( "...X....", TextUtil.justify( TextUtil.CENTER, "X", 8, '.' ) );
		assertEquals( "...XX...", TextUtil.justify( TextUtil.CENTER, "XX", 8, '.' ) );
		assertEquals( "........", TextUtil.justify( TextUtil.RIGHT, "", 8, '.' ) );
		assertEquals( ".......X", TextUtil.justify( TextUtil.RIGHT, "X", 8, '.' ) );
	}

	@Test
	void testJustifyWithCharAndPad() {
		assertEquals( "  ......", TextUtil.justify( TextUtil.LEFT, "", 8, '.', 2 ) );
		assertEquals( "X  .....", TextUtil.justify( TextUtil.LEFT, "X", 8, '.', 2 ) );
		assertEquals( "..    ..", TextUtil.justify( TextUtil.CENTER, "", 8, '.', 2 ) );
		assertEquals( ".  X  ..", TextUtil.justify( TextUtil.CENTER, "X", 8, '.', 2 ) );
		assertEquals( ".  XX  .", TextUtil.justify( TextUtil.CENTER, "XX", 8, '.', 2 ) );
		assertEquals( "......  ", TextUtil.justify( TextUtil.RIGHT, "", 8, '.', 2 ) );
		assertEquals( ".....  X", TextUtil.justify( TextUtil.RIGHT, "X", 8, '.', 2 ) );
	}

	@Test
	void testPad() {
		assertEquals( "", TextUtil.pad( -1 ) );
		assertEquals( "", TextUtil.pad( 0 ) );
		assertEquals( " ", TextUtil.pad( 1 ) );
		assertEquals( "     ", TextUtil.pad( 5 ) );
		assertEquals( "        ", TextUtil.pad( 8 ) );
	}

	@Test
	void testPadWithChar() {
		assertEquals( "", TextUtil.pad( -1, '.' ) );
		assertEquals( "", TextUtil.pad( 0, '.' ) );
		assertEquals( "x", TextUtil.pad( 1, 'x' ) );
		assertEquals( ",,,,,", TextUtil.pad( 5, ',' ) );
		assertEquals( "--------", TextUtil.pad( 8, '-' ) );
	}

	@Test
	void testGetLines() {
		String test = "This\nis\na\ntest.";
		List<String> lines = TextUtil.getLines( test );

		assertEquals( "This", lines.get( 0 ) );
		assertEquals( "is", lines.get( 1 ) );
		assertEquals( "a", lines.get( 2 ) );
		assertEquals( "test.", lines.get( 3 ) );
	}

	@Test
	void testGetLineCount() {
		assertEquals( 0, TextUtil.getLineCount( null ) );
		assertEquals( 0, TextUtil.getLineCount( "" ) );
		assertEquals( 1, TextUtil.getLineCount( " " ) );
		assertEquals( 2, TextUtil.getLineCount( " \n " ) );
		assertEquals( 2, TextUtil.getLineCount( " \r " ) );
		assertEquals( 2, TextUtil.getLineCount( " \r\n " ) );
	}

	@Test
	void testCountLines() {
		List<String> lines = new ArrayList<>();
		for( int index = 0; index < 5; index++ ) {
			lines.add( "Test line " + index );
		}

		assertEquals( 0, TextUtil.countLines( lines, "Test line" ) );
		assertEquals( 1, TextUtil.countLines( lines, "Test line 1" ) );
		assertEquals( 5, TextUtil.countLines( lines, "Test line ." ) );
		assertEquals( 2, TextUtil.countLines( lines, "Test .* [3,4]" ) );
		assertEquals( 1, TextUtil.countLines( lines, ".*4" ) );
	}

	@Test
	void testFindLine() {
		List<String> lines = new ArrayList<>();
		for( int index = 0; index < 5; index++ ) {
			lines.add( "Test line " + index );
		}

		assertNull( TextUtil.findLine( lines, "Test line" ) );
		assertEquals( "Test line 1", TextUtil.findLine( lines, "Test line 1" ) );
		assertEquals( "Test line 2", TextUtil.findLine( lines, "Test line .*", 2 ) );
		assertEquals( "Test line 3", TextUtil.findLine( lines, "Test .* 3" ) );
		assertEquals( "Test line 2", TextUtil.findLine( lines, "Test line .*", 2 ) );
		assertEquals( "Test line 4", TextUtil.findLine( lines, ".*4" ) );
	}

	@Test
	void testFindLines() {
		List<String> lines = new ArrayList<>();
		for( int index = 0; index < 5; index++ ) {
			lines.add( "Test line " + index );
		}

		List<String> result;

		result = TextUtil.findLines( lines, "Test line" );
		assertEquals( 0, result.size() );

		result = TextUtil.findLines( lines, "Test line ." );
		assertEquals( 5, result.size() );

		result = TextUtil.findLines( lines, "Test line 1" );
		assertEquals( 1, result.size() );

		result = TextUtil.findLines( lines, "Test line [2,3]" );
		assertEquals( 2, result.size() );

		result = TextUtil.findLines( lines, ".*4" );
		assertEquals( 1, result.size() );
	}

	@Test
	void testFindLineIndex() {
		List<String> lines = new ArrayList<>();
		for( int index = 0; index < 5; index++ ) {
			lines.add( "Test line " + index );
		}

		assertEquals( -1, TextUtil.findLineIndex( lines, "Test line" ) );
		assertEquals( 1, TextUtil.findLineIndex( lines, "Test line 1" ) );
		assertEquals( 2, TextUtil.findLineIndex( lines, "Test line .*", 2 ) );
		assertEquals( 3, TextUtil.findLineIndex( lines, "Test .* 3" ) );
		assertEquals( 2, TextUtil.findLineIndex( lines, "Test line .*", 2 ) );
		assertEquals( 4, TextUtil.findLineIndex( lines, ".*4" ) );
	}

	@Test
	void testPrepend() {
		assertNull( TextUtil.prepend( null, "X" ) );
		assertEquals( "A", TextUtil.prepend( "A", null ) );
		assertEquals( "B", TextUtil.prepend( "B", "" ) );

		assertEquals( "XC", TextUtil.prepend( "C", "X" ) );
		assertEquals( "XD\nXE", TextUtil.prepend( "D\nE", "X" ) );
		assertEquals( "XF\nXG\nX", TextUtil.prepend( "F\nG\n", "X" ) );
	}

	@Test
	void testAppend() {
		assertNull( TextUtil.append( null, "X" ) );
		assertEquals( "A", TextUtil.append( "A", null ) );
		assertEquals( "B", TextUtil.append( "B", "" ) );

		assertEquals( "CX", TextUtil.append( "C", "X" ) );
		assertEquals( "DX\nEX", TextUtil.append( "D\nE", "X" ) );
		assertEquals( "FX\nGX\nX", TextUtil.append( "F\nG\n", "X" ) );
	}

	@Test
	void testReline() throws Exception {
		int length = 40;

		assertNull( TextUtil.reline( null, length ) );

		String sample = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		String result = TextUtil.reline( sample, length );

		BufferedReader parser = new BufferedReader( new StringReader( result ) );
		assertEquals( "Lorem ipsum dolor sit amet, consectetur", parser.readLine() );
		assertEquals( "adipisicing elit, sed do eiusmod tempor", parser.readLine() );
		assertEquals( "incididunt ut labore et dolore magna", parser.readLine() );
		assertEquals( "aliqua. Ut enim ad minim veniam, quis", parser.readLine() );
		assertEquals( "nostrud exercitation ullamco laboris", parser.readLine() );
		assertEquals( "nisi ut aliquip ex ea commodo consequat.", parser.readLine() );
		assertEquals( "Duis aute irure dolor in reprehenderit", parser.readLine() );
		assertEquals( "in voluptate velit esse cillum dolore eu", parser.readLine() );
		assertEquals( "fugiat nulla pariatur. Excepteur sint", parser.readLine() );
		assertEquals( "occaecat cupidatat non proident, sunt in", parser.readLine() );
		assertEquals( "culpa qui officia deserunt mollit anim", parser.readLine() );
		assertEquals( "id est laborum.", parser.readLine() );
	}

	@Test
	void testArrayToString() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertEquals( "[0] [1] [2] [3] [4]", TextUtil.toString( array ) );
	}

	@Test
	void testArrayToStringWithOffset() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertEquals( "[2] [3] [4]", TextUtil.toString( array, 2 ) );
	}

	@Test
	void testArrayToStringWithOffsetAndLength() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertEquals( "[1] [2] [3]", TextUtil.toString( array, 1, 3 ) );
	}

	@Test
	void testArrayToStringWithDelimiter() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertEquals( "0_1_2_3_4", TextUtil.toString( array, "_" ) );
	}

	@Test
	void testArrayToStringWithDelimiterAndOffset() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertEquals( "2_3_4", TextUtil.toString( array, "_", 2 ) );
	}

	@Test
	void testArrayToStringWithDelimiterLengthAndOffset() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertEquals( "1_2_3", TextUtil.toString( array, "_", 1, 3 ) );
	}

	@Test
	void testListToString() {
		List<Integer> list = new ArrayList<>();

		list.add( 0 );
		list.add( 1 );
		list.add( 2 );
		list.add( 3 );
		list.add( 4 );

		assertEquals( "[0] [1] [2] [3] [4]", TextUtil.toString( list ) );
	}

	@Test
	void testListToStringWithDelimiter() {
		List<Integer> list = new ArrayList<>();

		list.add( 0 );
		list.add( 1 );
		list.add( 2 );
		list.add( 3 );
		list.add( 4 );

		assertEquals( "0 1 2 3 4", TextUtil.toString( list, " " ) );
	}

	@Test
	void testListToStringWithPrefixAndSuffix() {
		List<Integer> list = new ArrayList<>();

		list.add( 0 );
		list.add( 1 );
		list.add( 2 );
		list.add( 3 );
		list.add( 4 );

		assertEquals( "<0> <1> <2> <3> <4>", TextUtil.toString( list, "<", ">" ) );
	}

	@Test
	void testSplit() {
		String string = "string to split";
		assertThat( TextUtil.split( string ), contains( "string", "to", "split" ) );
	}

	@Test
	void testSplitWithExtraWhitespace() {
		String string = "  \tstring   to  split\n";
		assertThat( TextUtil.split( string ), contains( "string", "to", "split" ) );
	}

	@Test
	void testSplitWithQuotes() {
		String string = "string \"with quotes\" to split ";
		assertThat( TextUtil.split( string ), contains( "string", "with quotes", "to", "split" ) );
	}

	@Test
	void testSplitWithEscapedQuotes() {
		String string = "string \"with\\\" quotes\" to split ";
		assertThat( TextUtil.split( string ), contains( "string", "with\" quotes", "to", "split" ) );
	}

	@Test
	void testSplitWindowsPath() {
		String string = "copy C:\\some\\path C:\\some\\other\\path ";
		assertThat( TextUtil.split( string ), contains( "copy", "C:\\some\\path", "C:\\some\\other\\path" ) );
	}

	@Test
	void testToStringOrNull() {
		assertNull( TextUtil.toStringOrNull( null ) );
		assertThat( TextUtil.toStringOrNull( new Double(1.234)), is( "1.234"));
	}

}
