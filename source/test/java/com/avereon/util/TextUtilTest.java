package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextUtilTest {

	@Test
	void testIsEmpty() {
		assertThat( TextUtil.isEmpty( null ) ).isTrue();
		assertThat( TextUtil.isEmpty( "" ) ).isTrue();
		assertThat( TextUtil.isEmpty( " " ) ).isTrue();
		assertThat( TextUtil.isEmpty( "." ) ).isFalse();
	}

	@Test
	@SuppressWarnings( "ConstantConditions" )
	void testAreEqual() {
		assertThat( TextUtil.areEqual( null, null ) ).isTrue();
		assertThat( TextUtil.areEqual( "", "" ) ).isTrue();
		assertThat( TextUtil.areEqual( " ", " " ) ).isTrue();
		assertThat( TextUtil.areEqual( "a", "a" ) ).isTrue();

		assertThat( TextUtil.areEqual( null, "" ) ).isFalse();
		assertThat( TextUtil.areEqual( "", null ) ).isFalse();
		assertThat( TextUtil.areEqual( "a", "b" ) ).isFalse();
		assertThat( TextUtil.areEqual( "b", "a" ) ).isFalse();
	}

	@Test
	@SuppressWarnings( "ConstantConditions" )
	void testAreEqualIgnoreCase() {
		assertThat( TextUtil.areEqualIgnoreCase( null, null ) ).isTrue();
		assertThat( TextUtil.areEqualIgnoreCase( "", "" ) ).isTrue();
		assertThat( TextUtil.areEqualIgnoreCase( " ", " " ) ).isTrue();
		assertThat( TextUtil.areEqualIgnoreCase( "A", "a" ) ).isTrue();
		assertThat( TextUtil.areEqualIgnoreCase( "a", "A" ) ).isTrue();

		assertThat( TextUtil.areEqualIgnoreCase( null, "" ) ).isFalse();
		assertThat( TextUtil.areEqualIgnoreCase( "", null ) ).isFalse();
		assertThat( TextUtil.areEqual( "A", "b" ) ).isFalse();
		assertThat( TextUtil.areEqual( "B", "a" ) ).isFalse();
	}

	@Test
	void testAreSame() {
		assertThat( TextUtil.areSame( null, null ) ).isTrue();
		assertThat( TextUtil.areSame( "", "" ) ).isTrue();
		assertThat( TextUtil.areSame( " ", " " ) ).isTrue();
		assertThat( TextUtil.areSame( "a", "a" ) ).isTrue();

		assertThat( TextUtil.areSame( null, "" ) ).isTrue();
		assertThat( TextUtil.areSame( "", null ) ).isTrue();
		assertThat( TextUtil.areSame( null, " " ) ).isTrue();

		assertThat( TextUtil.areSame( null, "a" ) ).isFalse();
		assertThat( TextUtil.areSame( "", "a" ) ).isFalse();
		assertThat( TextUtil.areSame( " ", "a" ) ).isFalse();
	}

	@Test
	void testCompare() {
		assertThat( TextUtil.compare( null, null ) ).isEqualTo( 0 );
		assertThat( TextUtil.compare( null, "" ) ).isEqualTo( -1 );
		assertThat( TextUtil.compare( "", null ) ).isEqualTo( 1 );
		assertThat( TextUtil.compare( "", "" ) ).isEqualTo( 0 );

		assertThat( TextUtil.compare( "a", "a" ) ).isEqualTo( 0 );
		assertThat( TextUtil.compare( "a", "b" ) ).isEqualTo( -1 );
		assertThat( TextUtil.compare( "b", "a" ) ).isEqualTo( 1 );
	}

	@Test
	void testCompareIgnoreCase() {
		assertThat( TextUtil.compareIgnoreCase( null, null ) ).isEqualTo( 0 );
		assertThat( TextUtil.compareIgnoreCase( null, "" ) ).isEqualTo( -1 );
		assertThat( TextUtil.compareIgnoreCase( "", null ) ).isEqualTo( 1 );
		assertThat( TextUtil.compareIgnoreCase( "", "" ) ).isEqualTo( 0 );

		assertThat( TextUtil.compareIgnoreCase( "A", "a" ) ).isEqualTo( 0 );
		assertThat( TextUtil.compareIgnoreCase( "a", "A" ) ).isEqualTo( 0 );
		assertThat( TextUtil.compareIgnoreCase( "a", "B" ) ).isEqualTo( -1 );
		assertThat( TextUtil.compareIgnoreCase( "A", "b" ) ).isEqualTo( -1 );
		assertThat( TextUtil.compareIgnoreCase( "B", "a" ) ).isEqualTo( 1 );
		assertThat( TextUtil.compareIgnoreCase( "b", "A" ) ).isEqualTo( 1 );
	}

	@Test
	void testCleanNull() {
		assertThat( TextUtil.cleanNull( null ) ).isNull();
		assertThat( TextUtil.cleanNull( "" ) ).isNull();
		assertThat( TextUtil.cleanNull( " " ) ).isNull();
		assertThat( TextUtil.cleanNull( " a " ) ).isEqualTo( "a" );
	}

	@Test
	void testCleanEmpty() {
		assertThat( TextUtil.cleanEmpty( null ) ).isEqualTo( "" );
		assertThat( TextUtil.cleanEmpty( "" ) ).isEqualTo( "" );
		assertThat( TextUtil.cleanEmpty( " " ) ).isEqualTo( "" );
		assertThat( TextUtil.cleanEmpty( " a " ) ).isEqualTo( "a" );
	}

	@Test
	void testConcatenate() {
		assertThat( TextUtil.concatenate( "Count: ", 10 ) ).isEqualTo( "Count: 10" );
		assertThat( TextUtil.concatenate( "Flag: ", false ) ).isEqualTo( "Flag: false" );
		assertThat( TextUtil.concatenate( "Test", " ", "String" ) ).isEqualTo( "Test String" );
	}

	@Test
	void testToPrintableStringUsingByte() {
		assertThat( TextUtil.toPrintableString( (byte)0 ) ).isEqualTo( "[0]" );
		assertThat( TextUtil.toPrintableString( (byte)27 ) ).isEqualTo( "[27]" );
		assertThat( TextUtil.toPrintableString( (byte)31 ) ).isEqualTo( "[31]" );
		assertThat( TextUtil.toPrintableString( (byte)32 ) ).isEqualTo( " " );
		assertThat( TextUtil.toPrintableString( (byte)65 ) ).isEqualTo( "A" );
		assertThat( TextUtil.toPrintableString( (byte)126 ) ).isEqualTo( "~" );
		assertThat( TextUtil.toPrintableString( (byte)127 ) ).isEqualTo( "[127]" );
		assertThat( TextUtil.toPrintableString( (byte)128 ) ).isEqualTo( "[128]" );
		assertThat( TextUtil.toPrintableString( (byte)255 ) ).isEqualTo( "[255]" );

		assertThat( TextUtil.toPrintableString( (byte)-1 ) ).isEqualTo( "[255]" );
		assertThat( TextUtil.toPrintableString( (byte)256 ) ).isEqualTo( "[0]" );
	}

	@Test
	void testToPrintableString() {
		assertThat( TextUtil.toPrintableString( (char)0 ) ).isEqualTo( "[0]" );
		assertThat( TextUtil.toPrintableString( (char)27 ) ).isEqualTo( "[27]" );
		assertThat( TextUtil.toPrintableString( (char)31 ) ).isEqualTo( "[31]" );
		assertThat( TextUtil.toPrintableString( (char)32 ) ).isEqualTo( " " );
		assertThat( TextUtil.toPrintableString( (char)65 ) ).isEqualTo( "A" );
		assertThat( TextUtil.toPrintableString( (char)126 ) ).isEqualTo( "~" );
		assertThat( TextUtil.toPrintableString( (char)127 ) ).isEqualTo( "[127]" );
		assertThat( TextUtil.toPrintableString( (char)255 ) ).isEqualTo( "[255]" );
	}

	@Test
	void testToHexEncodedStringWithBytes() {
		Charset encoding = StandardCharsets.ISO_8859_1;
		assertThat( TextUtil.toHexEncodedString( "".getBytes( encoding ) ) ).isEqualTo( "" );
		assertThat( TextUtil.toHexEncodedString( "\u0000".getBytes( encoding ) ) ).isEqualTo( "00" );
		assertThat( TextUtil.toHexEncodedString( "\u0000\u0001".getBytes( encoding ) ) ).isEqualTo( "0001" );
		assertThat( TextUtil.toHexEncodedString( "\u00ff\u0001".getBytes( encoding ) ) ).isEqualTo( "ff01" );
		assertThat( TextUtil.toHexEncodedString( "\u0000\u0001\u000f".getBytes( encoding ) ) ).isEqualTo( "00010f" );
		assertThat( TextUtil.toHexEncodedString( "test".getBytes( encoding ) ) ).isEqualTo( "74657374" );
	}

	@Test
	void testHexEncodeWithString() {
		assertThat( TextUtil.hexEncode( "" ) ).isEqualTo( "" );
		assertThat( TextUtil.hexEncode( "\u0000" ) ).isEqualTo( "0000" );
		assertThat( TextUtil.hexEncode( "\u0000\u0001" ) ).isEqualTo( "00000001" );
		assertThat( TextUtil.hexEncode( "\u0000\u0001\u000f" ) ).isEqualTo( "00000001000f" );
		assertThat( TextUtil.hexEncode( "test" ) ).isEqualTo( "0074006500730074" );
	}

	@Test
	void testHexDecodeWithString() {
		assertThat( TextUtil.hexDecode( null ) ).isNull();
		assertThat( TextUtil.hexDecode( "" ) ).isEqualTo( "" );
		assertThat( TextUtil.hexDecode( "0000" ) ).isEqualTo( "\u0000" );
		assertThat( TextUtil.hexDecode( "00000001" ) ).isEqualTo( "\u0000\u0001" );
		assertThat( TextUtil.hexDecode( "00000001000f" ) ).isEqualTo( "\u0000\u0001\u000f" );
		assertThat( TextUtil.hexDecode( "0074006500730074" ) ).isEqualTo( "test" );

	}

	@Test
	void testHexByteEncode() {
		Charset encoding = StandardCharsets.ISO_8859_1;
		assertThat( TextUtil.secureHexByteEncode( "".getBytes( encoding ) ) ).isEqualTo( "" );
		assertThat( TextUtil.secureHexByteEncode( "\u0000".getBytes( encoding ) ) ).isEqualTo( "00" );
		assertThat( TextUtil.secureHexByteEncode( "\u0000\u0001".getBytes( encoding ) ) ).isEqualTo( "0001" );
		assertThat( TextUtil.secureHexByteEncode( "\u00ff\u0001".getBytes( encoding ) ) ).isEqualTo( "ff01" );
		assertThat( TextUtil.secureHexByteEncode( "\u0000\u0001\u000f".getBytes( encoding ) ) ).isEqualTo( "00010f" );
		assertThat( TextUtil.secureHexByteEncode( "test".getBytes( encoding ) ) ).isEqualTo( "74657374" );
	}

	@Test
	void testHexByteDecode() {
		Charset encoding = StandardCharsets.ISO_8859_1;
		assertThat( TextUtil.secureHexByteDecode( null ) ).isNull();
		assertThat( new String( TextUtil.secureHexByteDecode( "" ), encoding ) ).isEqualTo( "" );
		assertThat( new String( TextUtil.secureHexByteDecode( "00" ), encoding ) ).isEqualTo( "\u0000" );
		assertThat( new String( TextUtil.secureHexByteDecode( "0001" ), encoding ) ).isEqualTo( "\u0000\u0001" );
		assertThat( new String( TextUtil.secureHexByteDecode( "ff01" ), encoding ) ).isEqualTo( "\u00ff\u0001" );
		assertThat( new String( TextUtil.secureHexByteDecode( "00010f" ), encoding ) ).isEqualTo( "\u0000\u0001\u000f" );
		assertThat( new String( TextUtil.secureHexByteDecode( "74657374" ), encoding ) ).isEqualTo( "test" );
	}

	@Test
	void testHexCharEncode() {
		assertThat( TextUtil.secureHexEncode( null ) ).isNull();
		assertThat( TextUtil.secureHexEncode( "".toCharArray() ) ).isEqualTo( "" );
		assertThat( TextUtil.secureHexEncode( "\u0000".toCharArray() ) ).isEqualTo( "0000" );
		assertThat( TextUtil.secureHexEncode( "\u0000\u0001".toCharArray() ) ).isEqualTo( "00000001" );
		assertThat( TextUtil.secureHexEncode( "\u0000\u0001\u000f".toCharArray() ) ).isEqualTo( "00000001000f" );
		assertThat( TextUtil.secureHexEncode( "test".toCharArray() ) ).isEqualTo( "0074006500730074" );
	}

	@Test
	void testHexCharDecode() {
		assertThat( TextUtil.secureHexDecode( null ) ).isNull();
		assertThat( new String( TextUtil.secureHexDecode( "" ) ) ).isEqualTo( "" );
		assertThat( new String( TextUtil.secureHexDecode( "0000" ) ) ).isEqualTo( "\u0000" );
		assertThat( new String( TextUtil.secureHexDecode( "00000001" ) ) ).isEqualTo( "\u0000\u0001" );
		assertThat( new String( TextUtil.secureHexDecode( "00000001000f" ) ) ).isEqualTo( "\u0000\u0001\u000f" );
		assertThat( new String( TextUtil.secureHexDecode( "0074006500730074" ) ) ).isEqualTo( "test" );
	}

	@Test
	void testIsInteger() {
		assertThat( TextUtil.isInteger( null ) ).isFalse();
		assertThat( TextUtil.isInteger( "" ) ).isFalse();

		assertThat( TextUtil.isInteger( "1e-10" ) ).isFalse();
		assertThat( TextUtil.isInteger( "1.0" ) ).isFalse();
		assertThat( TextUtil.isInteger( "2147483648" ) ).isFalse();
		assertThat( TextUtil.isInteger( "-2147483649" ) ).isFalse();

		assertThat( TextUtil.isInteger( "0" ) ).isTrue();
		assertThat( TextUtil.isInteger( "2147483647" ) ).isTrue();
		assertThat( TextUtil.isInteger( "-2147483648" ) ).isTrue();
	}

	@Test
	void testIsLong() {
		assertThat( TextUtil.isLong( null ) ).isFalse();
		assertThat( TextUtil.isLong( "" ) ).isFalse();

		assertThat( TextUtil.isLong( "1e-10" ) ).isFalse();
		assertThat( TextUtil.isLong( "1.0" ) ).isFalse();
		assertThat( TextUtil.isLong( "9223372036854775808" ) ).isFalse();
		assertThat( TextUtil.isLong( "-9223372036854775809" ) ).isFalse();

		assertThat( TextUtil.isLong( "0" ) ).isTrue();
		assertThat( TextUtil.isLong( "9223372036854775807" ) ).isTrue();
		assertThat( TextUtil.isLong( "-9223372036854775808" ) ).isTrue();
	}

	@Test
	void testIsFloat() {
		assertThat( TextUtil.isFloat( null ) ).isFalse();
		assertThat( TextUtil.isFloat( "" ) ).isFalse();

		assertThat( TextUtil.isFloat( "0" ) ).isTrue();
		assertThat( TextUtil.isFloat( "1.0" ) ).isTrue();
		assertThat( TextUtil.isFloat( "1e10" ) ).isTrue();
		assertThat( TextUtil.isFloat( "1e-10" ) ).isTrue();
		assertThat( TextUtil.isFloat( "-1e10" ) ).isTrue();
		assertThat( TextUtil.isFloat( "-1e-10" ) ).isTrue();
	}

	@Test
	void testIsDouble() {
		assertThat( TextUtil.isDouble( null ) ).isFalse();
		assertThat( TextUtil.isDouble( "" ) ).isFalse();

		assertThat( TextUtil.isDouble( "0" ) ).isTrue();
		assertThat( TextUtil.isDouble( "1.0" ) ).isTrue();
		assertThat( TextUtil.isDouble( "1e10" ) ).isTrue();
		assertThat( TextUtil.isDouble( "1e-10" ) ).isTrue();
		assertThat( TextUtil.isDouble( "-1e10" ) ).isTrue();
		assertThat( TextUtil.isDouble( "-1e-10" ) ).isTrue();
	}

	@Test
	void testCapitalize() {
		assertThat( TextUtil.capitalize( null ) ).isNull();
		assertThat( TextUtil.capitalize( "" ) ).isEqualTo( "" );
		assertThat( TextUtil.capitalize( "test" ) ).isEqualTo( "Test" );
		assertThat( TextUtil.capitalize( "new brunswick" ) ).isEqualTo( "New brunswick" );
	}

	@Test
	void testJustify() {
		assertThat( TextUtil.justify( TextUtil.LEFT, "", 8 ) ).isEqualTo( "        " );
		assertThat( TextUtil.justify( TextUtil.LEFT, "X", 8 ) ).isEqualTo( "X       " );
		assertThat( TextUtil.justify( TextUtil.CENTER, "", 8 ) ).isEqualTo( "        " );
		assertThat( TextUtil.justify( TextUtil.CENTER, "X", 8 ) ).isEqualTo( "   X    " );
		assertThat( TextUtil.justify( TextUtil.CENTER, "XX", 8 ) ).isEqualTo( "   XX   " );
		assertThat( TextUtil.justify( TextUtil.RIGHT, "", 8 ) ).isEqualTo( "        " );
		assertThat( TextUtil.justify( TextUtil.RIGHT, "X", 8 ) ).isEqualTo( "       X" );
	}

	@Test
	void testJustifyWithChar() {
		assertThat( TextUtil.justify( TextUtil.LEFT, "", 8, '.' ) ).isEqualTo( "........" );
		assertThat( TextUtil.justify( TextUtil.LEFT, "X", 8, '.' ) ).isEqualTo( "X......." );
		assertThat( TextUtil.justify( TextUtil.CENTER, "", 8, '.' ) ).isEqualTo( "........" );
		assertThat( TextUtil.justify( TextUtil.CENTER, "X", 8, '.' ) ).isEqualTo( "...X...." );
		assertThat( TextUtil.justify( TextUtil.CENTER, "XX", 8, '.' ) ).isEqualTo( "...XX..." );
		assertThat( TextUtil.justify( TextUtil.RIGHT, "", 8, '.' ) ).isEqualTo( "........" );
		assertThat( TextUtil.justify( TextUtil.RIGHT, "X", 8, '.' ) ).isEqualTo( ".......X" );
	}

	@Test
	void testJustifyWithCharAndPad() {
		assertThat( TextUtil.justify( TextUtil.LEFT, "", 8, '.', 2 ) ).isEqualTo( "  ......" );
		assertThat( TextUtil.justify( TextUtil.LEFT, "X", 8, '.', 2 ) ).isEqualTo( "X  ....." );
		assertThat( TextUtil.justify( TextUtil.CENTER, "", 8, '.', 2 ) ).isEqualTo( "..    .." );
		assertThat( TextUtil.justify( TextUtil.CENTER, "X", 8, '.', 2 ) ).isEqualTo( ".  X  .." );
		assertThat( TextUtil.justify( TextUtil.CENTER, "XX", 8, '.', 2 ) ).isEqualTo( ".  XX  ." );
		assertThat( TextUtil.justify( TextUtil.RIGHT, "", 8, '.', 2 ) ).isEqualTo( "......  " );
		assertThat( TextUtil.justify( TextUtil.RIGHT, "X", 8, '.', 2 ) ).isEqualTo( ".....  X" );
	}

	@Test
	void testPad() {
		assertThat( TextUtil.pad( -1 ) ).isEqualTo( "" );
		assertThat( TextUtil.pad( 0 ) ).isEqualTo( "" );
		assertThat( TextUtil.pad( 1 ) ).isEqualTo( " " );
		assertThat( TextUtil.pad( 5 ) ).isEqualTo( "     " );
		assertThat( TextUtil.pad( 8 ) ).isEqualTo( "        " );
	}

	@Test
	void testPadWithChar() {
		assertThat( TextUtil.pad( -1, '.' ) ).isEqualTo( "" );
		assertThat( TextUtil.pad( 0, '.' ) ).isEqualTo( "" );
		assertThat( TextUtil.pad( 1, 'x' ) ).isEqualTo( "x" );
		assertThat( TextUtil.pad( 5, ',' ) ).isEqualTo( ",,,,," );
		assertThat( TextUtil.pad( 8, '-' ) ).isEqualTo( "--------" );
	}

	@Test
	void testGetLines() {
		String test = "This\nis\na\ntest.";
		List<String> lines = TextUtil.getLines( test );

		assertThat( lines.get( 0 ) ).isEqualTo( "This" );
		assertThat( lines.get( 1 ) ).isEqualTo( "is" );
		assertThat( lines.get( 2 ) ).isEqualTo( "a" );
		assertThat( lines.get( 3 ) ).isEqualTo( "test." );
	}

	@Test
	void testGetLineCount() {
		assertThat( TextUtil.getLineCount( null ) ).isEqualTo( 0 );
		assertThat( TextUtil.getLineCount( "" ) ).isEqualTo( 0 );
		assertThat( TextUtil.getLineCount( " " ) ).isEqualTo( 1 );
		assertThat( TextUtil.getLineCount( " \n " ) ).isEqualTo( 2 );
		assertThat( TextUtil.getLineCount( " \r " ) ).isEqualTo( 2 );
		assertThat( TextUtil.getLineCount( " \r\n " ) ).isEqualTo( 2 );
	}

	@Test
	void testCountLines() {
		List<String> lines = new ArrayList<>();
		for( int index = 0; index < 5; index++ ) {
			lines.add( "Test line " + index );
		}

		assertThat( TextUtil.countLines( lines, "Test line" ) ).isEqualTo( 0 );
		assertThat( TextUtil.countLines( lines, "Test line 1" ) ).isEqualTo( 1 );
		assertThat( TextUtil.countLines( lines, "Test line ." ) ).isEqualTo( 5 );
		assertThat( TextUtil.countLines( lines, "Test .* [3,4]" ) ).isEqualTo( 2 );
		assertThat( TextUtil.countLines( lines, ".*4" ) ).isEqualTo( 1 );
	}

	@Test
	void testFindLine() {
		List<String> lines = new ArrayList<>();
		for( int index = 0; index < 5; index++ ) {
			lines.add( "Test line " + index );
		}

		assertThat( TextUtil.findLine( lines, "Test line" ) ).isNull();
		assertThat( TextUtil.findLine( lines, "Test line 1" ) ).isEqualTo( "Test line 1" );
		assertThat( TextUtil.findLine( lines, "Test line .*", 2 ) ).isEqualTo( "Test line 2" );
		assertThat( TextUtil.findLine( lines, "Test .* 3" ) ).isEqualTo( "Test line 3" );
		assertThat( TextUtil.findLine( lines, "Test line .*", 2 ) ).isEqualTo( "Test line 2" );
		assertThat( TextUtil.findLine( lines, ".*4" ) ).isEqualTo( "Test line 4" );
	}

	@Test
	void testFindLines() {
		List<String> lines = new ArrayList<>();
		for( int index = 0; index < 5; index++ ) {
			lines.add( "Test line " + index );
		}

		List<String> result;

		result = TextUtil.findLines( lines, "Test line" );
		assertThat( result.size() ).isEqualTo( 0 );

		result = TextUtil.findLines( lines, "Test line ." );
		assertThat( result.size() ).isEqualTo( 5 );

		result = TextUtil.findLines( lines, "Test line 1" );
		assertThat( result.size() ).isEqualTo( 1 );

		result = TextUtil.findLines( lines, "Test line [2,3]" );
		assertThat( result.size() ).isEqualTo( 2 );

		result = TextUtil.findLines( lines, ".*4" );
		assertThat( result.size() ).isEqualTo( 1 );
	}

	@Test
	void testFindLineIndex() {
		List<String> lines = new ArrayList<>();
		for( int index = 0; index < 5; index++ ) {
			lines.add( "Test line " + index );
		}

		assertThat( TextUtil.findLineIndex( lines, "Test line" ) ).isEqualTo( -1 );
		assertThat( TextUtil.findLineIndex( lines, "Test line 1" ) ).isEqualTo( 1 );
		assertThat( TextUtil.findLineIndex( lines, "Test line .*", 2 ) ).isEqualTo( 2 );
		assertThat( TextUtil.findLineIndex( lines, "Test .* 3" ) ).isEqualTo( 3 );
		assertThat( TextUtil.findLineIndex( lines, "Test line .*", 2 ) ).isEqualTo( 2 );
		assertThat( TextUtil.findLineIndex( lines, ".*4" ) ).isEqualTo( 4 );
	}

	@Test
	void testPrepend() {
		assertThat( TextUtil.prepend( null, "X" ) ).isNull();
		assertThat( TextUtil.prepend( "A", null ) ).isEqualTo( "A" );
		assertThat( TextUtil.prepend( "B", "" ) ).isEqualTo( "B" );

		assertThat( TextUtil.prepend( "C", "X" ) ).isEqualTo( "XC" );
		assertThat( TextUtil.prepend( "D\nE", "X" ) ).isEqualTo( "XD\nXE" );
		assertThat( TextUtil.prepend( "F\nG\n", "X" ) ).isEqualTo( "XF\nXG\nX" );
	}

	@Test
	void testAppend() {
		assertThat( TextUtil.append( null, "X" ) ).isNull();
		assertThat( TextUtil.append( "A", null ) ).isEqualTo( "A" );
		assertThat( TextUtil.append( "B", "" ) ).isEqualTo( "B" );

		assertThat( TextUtil.append( "C", "X" ) ).isEqualTo( "CX" );
		assertThat( TextUtil.append( "D\nE", "X" ) ).isEqualTo( "DX\nEX" );
		assertThat( TextUtil.append( "F\nG\n", "X" ) ).isEqualTo( "FX\nGX\nX" );
	}

	@Test
	void testReline() throws Exception {
		int length = 40;

		assertThat( TextUtil.reline( null, length ) ).isNull();

		String sample = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
		String result = TextUtil.reline( sample, length );

		BufferedReader parser = new BufferedReader( new StringReader( result ) );
		assertThat( parser.readLine() ).isEqualTo( "Lorem ipsum dolor sit amet, consectetur" );
		assertThat( parser.readLine() ).isEqualTo( "adipisicing elit, sed do eiusmod tempor" );
		assertThat( parser.readLine() ).isEqualTo( "incididunt ut labore et dolore magna" );
		assertThat( parser.readLine() ).isEqualTo( "aliqua. Ut enim ad minim veniam, quis" );
		assertThat( parser.readLine() ).isEqualTo( "nostrud exercitation ullamco laboris" );
		assertThat( parser.readLine() ).isEqualTo( "nisi ut aliquip ex ea commodo consequat." );
		assertThat( parser.readLine() ).isEqualTo( "Duis aute irure dolor in reprehenderit" );
		assertThat( parser.readLine() ).isEqualTo( "in voluptate velit esse cillum dolore eu" );
		assertThat( parser.readLine() ).isEqualTo( "fugiat nulla pariatur. Excepteur sint" );
		assertThat( parser.readLine() ).isEqualTo( "occaecat cupidatat non proident, sunt in" );
		assertThat( parser.readLine() ).isEqualTo( "culpa qui officia deserunt mollit anim" );
		assertThat( parser.readLine() ).isEqualTo( "id est laborum." );
	}

	@Test
	void testArrayToString() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertThat( TextUtil.toString( array ) ).isEqualTo( "[0] [1] [2] [3] [4]" );
	}

	@Test
	void testArrayToStringWithOffset() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertThat( TextUtil.toString( array, 2 ) ).isEqualTo( "[2] [3] [4]" );
	}

	@Test
	void testArrayToStringWithOffsetAndLength() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertThat( TextUtil.toString( array, 1, 3 ) ).isEqualTo( "[1] [2] [3]" );
	}

	@Test
	void testArrayToStringWithDelimiter() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertThat( TextUtil.toString( array, "_" ) ).isEqualTo( "0_1_2_3_4" );
	}

	@Test
	void testArrayToStringWithDelimiterAndOffset() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertThat( TextUtil.toString( array, "_", 2 ) ).isEqualTo( "2_3_4" );
	}

	@Test
	void testArrayToStringWithDelimiterLengthAndOffset() {
		Integer[] array = new Integer[ 5 ];

		array[ 0 ] = 0;
		array[ 1 ] = 1;
		array[ 2 ] = 2;
		array[ 3 ] = 3;
		array[ 4 ] = 4;

		assertThat( TextUtil.toString( array, "_", 1, 3 ) ).isEqualTo( "1_2_3" );
	}

	@Test
	void testListToString() {
		List<Integer> list = new ArrayList<>();

		list.add( 0 );
		list.add( 1 );
		list.add( 2 );
		list.add( 3 );
		list.add( 4 );

		assertThat( TextUtil.toString( list ) ).isEqualTo( "[0] [1] [2] [3] [4]" );
	}

	@Test
	void testListToStringWithDelimiter() {
		List<Integer> list = new ArrayList<>();

		list.add( 0 );
		list.add( 1 );
		list.add( 2 );
		list.add( 3 );
		list.add( 4 );

		assertThat( TextUtil.toString( list, " " ) ).isEqualTo( "0 1 2 3 4" );
	}

	@Test
	void testListToStringWithPrefixAndSuffix() {
		List<Integer> list = new ArrayList<>();

		list.add( 0 );
		list.add( 1 );
		list.add( 2 );
		list.add( 3 );
		list.add( 4 );

		assertThat( TextUtil.toString( list, "<", ">" ) ).isEqualTo( "<0> <1> <2> <3> <4>" );
	}

	@Test
	void testSplit() {
		String string = "string to split";
		assertThat( TextUtil.split( string ) ).contains( "string", "to", "split" );
	}

	@Test
	void testSplitWithExtraWhitespace() {
		String string = "  \tstring   to  split\n";
		assertThat( TextUtil.split( string ) ).contains( "string", "to", "split" );
	}

	@Test
	void testSplitWithQuotes() {
		String string = "string \"with quotes\" to split ";
		assertThat( TextUtil.split( string ) ).contains( "string", "with quotes", "to", "split" );
	}

	@Test
	void testSplitWithEscapedQuotes() {
		String string = "string \"with\\\" quotes\" to split ";
		assertThat( TextUtil.split( string ) ).contains( "string", "with\" quotes", "to", "split" );
	}

	@Test
	void testSplitWindowsPath() {
		String string = "copy C:\\some\\path C:\\some\\other\\path ";
		assertThat( TextUtil.split( string ) ).contains( "copy", "C:\\some\\path", "C:\\some\\other\\path" );
	}

	@Test
	void testToStringOrNull() {
		assertThat( TextUtil.toStringOrNull( null ) ).isNull();
		assertThat( TextUtil.toStringOrNull( 1.234D ) ).isEqualTo( "1.234" );
	}

}
