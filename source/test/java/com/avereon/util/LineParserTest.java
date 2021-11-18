package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LineParserTest {

	@Test
	void testParserWithNull() {
		LineParser parser = new LineParser( null );
		assertThat( parser.next() ).isNull();
	}

	@Test
	void testParserWithEmpty() {
		LineParser parser = new LineParser( "" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isNull();
	}

	@Test
	void testParserWithSingleLine() {
		LineParser parser = new LineParser( "a" );
		assertThat( parser.next() ).isEqualTo( "a" );
		assertThat( parser.next() ).isNull();
	}

	@Test
	void testParserWithTwoEmptyLines() {
		LineParser parser = new LineParser( "\n" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.getTerminator() ).isEqualTo( "\n" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.getTerminator() ).isEqualTo( "" );
		assertThat( parser.next() ).isNull();
	}

	@Test
	void testParserWithTwoEmptyLinesWithTerminators() {
		LineParser parser = new LineParser( "\n" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isNull();

		parser = new LineParser( "\r" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isNull();

		parser = new LineParser( "\r\n" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isNull();
	}

	@Test
	void testParserWithFirstLineEmpty() {
		LineParser parser = new LineParser( "\na" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isEqualTo( "a" );
		assertThat( parser.next() ).isNull();
	}

	@Test
	void testParserWithTwoLines() {
		LineParser parser = new LineParser( "a\nb" );
		assertThat( parser.next() ).isEqualTo( "a" );
		assertThat( parser.next() ).isEqualTo( "b" );
		assertThat( parser.next() ).isNull();
	}

	@Test
	void testParserWithThreeLinesLastLineEmpty() {
		LineParser parser = new LineParser( "a\nb\n" );
		assertThat( parser.next() ).isEqualTo( "a" );
		assertThat( parser.next() ).isEqualTo( "b" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isNull();
	}

	@Test
	void testParserWithFirstLineAndLastLineEmpty() {
		LineParser parser = new LineParser( "\na\nb\n" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isEqualTo( "a" );
		assertThat( parser.next() ).isEqualTo( "b" );
		assertThat( parser.next() ).isEqualTo( "" );
		assertThat( parser.next() ).isNull();
	}

	@Test
	void testGetRemaining() {
		LineParser parser = new LineParser( "a\nb\nc" );
		assertThat( parser.next() ).isEqualTo( "a" );
		assertThat( parser.getRemaining() ).isEqualTo( "b\nc" );
		assertThat( parser.next() ).isEqualTo( "b" );
		assertThat( parser.getRemaining() ).isEqualTo( "c" );
		assertThat( parser.next() ).isEqualTo( "c" );
		assertThat( parser.next() ).isNull();
	}

}
