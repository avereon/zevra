package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

class IndenterTest {

	// For convenience.
	private static final String IND = Indenter.DEFAULT_INDENT_STRING;

	@Test
	void testCreateIndent() {
		assertThat( Indenter.createIndent() ).isEqualTo( IND);
	}

	@Test
	void testCreateIndentWithSize() {
		assertThat( Indenter.createIndent( -1 ) ).isEqualTo( "");
		assertThat( Indenter.createIndent( 0 ) ).isEqualTo( "");
		assertThat( Indenter.createIndent( 1 ) ).isEqualTo( IND + "");
		assertThat( Indenter.createIndent( 2 ) ).isEqualTo( IND + "" + IND + "");
		assertThat( Indenter.createIndent( 3 ) ).isEqualTo( IND + "" + IND + "" + IND + "");
	}

	@Test
	void testCreateIndentWithString() {
		assertThat( Indenter.createIndent( -1, "<>" ) ).isEqualTo( "");
		assertThat( Indenter.createIndent( 0, "<>" ) ).isEqualTo( "");
		assertThat( Indenter.createIndent( 1, "<>" ) ).isEqualTo( "<>");
		assertThat( Indenter.createIndent( 2, "<>" ) ).isEqualTo( "<><>");
		assertThat( Indenter.createIndent( 3, "<>" ) ).isEqualTo( "<><><>");
	}

	@Test
	void testWriteIndent() throws Exception {
		StringWriter writer = new StringWriter();
		Indenter.writeIndent( writer );
		assertThat( writer.toString() ).isEqualTo( IND);
	}

	@Test
	void testWriteIndentWithSize() throws Exception {
		assertWriteIndent( "", -1 );
		assertWriteIndent( "", 0 );
		assertWriteIndent( IND + "", 1 );
		assertWriteIndent( IND + "" + IND + "", 2 );
		assertWriteIndent( IND + "" + IND + "" + IND + "", 3 );
	}

	@Test
	void testWriteIndentWithString() throws Exception {
		assertWriteIndentWithString( "<>", "<>" );
		assertWriteIndentWithString( "<><>", "<><>" );
		assertWriteIndentWithString( "<><><>", "<><><>" );
	}

	@Test
	void testWriteIndentWithSizeAndString() throws Exception {
		assertWriteIndentWithSizeAndString( "", -1, "<>" );
		assertWriteIndentWithSizeAndString( "", 0, "<>" );
		assertWriteIndentWithSizeAndString( "<>", 1, "<>" );
		assertWriteIndentWithSizeAndString( "[][]", 2, "[]" );
		assertWriteIndentWithSizeAndString( "<*><*><*>", 3, "<*>" );
	}

	@Test
	void testIndent() {
		assertThat( Indenter.indent( null )).isNull();

		assertThat( Indenter.indent( "" ) ).isEqualTo( IND);
		assertThat( Indenter.indent( "a" ) ).isEqualTo( IND + "a");

		assertThat( Indenter.indent( "\n" ) ).isEqualTo( IND + "\n" + IND);
		assertThat( Indenter.indent( "a\na" ) ).isEqualTo( IND + "a\n" + IND + "a");

		assertThat( Indenter.indent( "\n\n" ) ).isEqualTo( IND + "\n" + IND + "\n" + IND);
		assertThat( Indenter.indent( "a\na\na" ) ).isEqualTo( IND + "a\n" + IND + "a\n" + IND + "a");
	}

	@Test
	void testIndentWithSize() {
		assertThat( Indenter.indent( null, 2 ) ).isNull();

		assertThat( Indenter.indent( "", 2 ) ).isEqualTo( IND + IND);
		assertThat( Indenter.indent( "a", 2 ) ).isEqualTo( IND + IND + "a");

		assertThat( Indenter.indent( "\n", 2 ) ).isEqualTo( IND + IND + "\n" + IND + IND);
		assertThat( Indenter.indent( "a\na", 2 ) ).isEqualTo( IND + IND + "a\n" + IND + IND + "a");

		assertThat( Indenter.indent( "\n\n", 2 ) ).isEqualTo( IND + IND + "\n" + IND + IND + "\n" + IND + IND);
		assertThat( Indenter.indent( "a\na\na", 2 ) ).isEqualTo( IND + IND + "a\n" + IND + IND + "a\n" + IND + IND + "a");
	}

	@Test
	void testIndentWithString() {
		assertThat( Indenter.indent( null, "<>" ) ).isNull();

		assertThat( Indenter.indent( "", "<>" ) ).isEqualTo( "<>");
		assertThat( Indenter.indent( "a", "<>" ) ).isEqualTo( "<>a");

		assertThat( Indenter.indent( "\n", "<>" ) ).isEqualTo( "<>\n<>");
		assertThat( Indenter.indent( "a\na", "<>" ) ).isEqualTo( "<>a\n<>a");

		assertThat( Indenter.indent( "\n\n", "<>" ) ).isEqualTo( "<>\n<>\n<>");
		assertThat( Indenter.indent( "a\na\na", "<>" ) ).isEqualTo( "<>a\n<>a\n<>a");
	}

	@Test
	void testIndentWithSizeAndString() {
		assertThat( Indenter.indent( null, 1, "<>" ) ).isNull();

		assertThat( Indenter.indent( "", 3, "<>" ) ).isEqualTo( "<><><>");
		assertThat( Indenter.indent( "a", 3, "<>" ) ).isEqualTo( "<><><>a");

		assertThat( Indenter.indent( "\n", 3, "<>" ) ).isEqualTo( "<><><>\n<><><>");
		assertThat( Indenter.indent( "a\na", 3, "<>" ) ).isEqualTo( "<><><>a\n<><><>a");

		assertThat( Indenter.indent( "\n\n", 3, "<>" ) ).isEqualTo( "<><><>\n<><><>\n<><><>");
		assertThat( Indenter.indent( "a\na\na", 3, "<>" ) ).isEqualTo( "<><><>a\n<><><>a\n<><><>a");
	}

	@Test
	void testCanUnindent() {
		assertThat( Indenter.canUnindent( null ) ).isFalse();

		assertThat( Indenter.canUnindent( "a" + IND ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + "a\nb" + IND ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + "a\n" + IND + "b\nc" + IND ) ).isFalse();

		assertThat( Indenter.canUnindent( "" ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + "\n" ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + "\n" + IND + "\n" ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + "\n" + IND + "\n" + IND + "\n" ) ).isFalse();

		assertThat( Indenter.canUnindent( "", true ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + "\n", true ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + "\n" + IND + "\n", true ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + "\n" + IND + "\n" + IND + "\n", true ) ).isTrue();

		assertThat( Indenter.canUnindent( IND + "a" ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + "a\n" + IND + "b" ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + "a\n" + IND + "b\n" + IND + "c" ) ).isTrue();
	}

	@Test
	void testCanUnindentWithSize() {
		assertThat( Indenter.canUnindent( null ) ).isFalse();

		assertThat( Indenter.canUnindent( "a" + IND + IND + "", 2 ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + IND + "a\nb" + IND + IND + "", 2 ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + IND + "a\n" + IND + IND + "b\nc" + IND + IND + "", 2 ) ).isFalse();

		assertThat( Indenter.canUnindent( "", 2 ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + IND + "\n", 2 ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + IND + "\n" + IND + IND + "\n", 2 ) ).isFalse();
		assertThat( Indenter.canUnindent( IND + IND + "\n" + IND + IND + "\n" + IND + IND + "\n", 2 ) ).isFalse();

		assertThat( Indenter.canUnindent( "", 2, true ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + IND + "\n", 2, true ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + IND + "\n" + IND + IND + "\n", 2, true ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + IND + "\n" + IND + IND + "\n" + IND + IND + "\n", 2, true ) ).isTrue();

		assertThat( Indenter.canUnindent( IND + IND + "a", 2 ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + IND + "a\n" + IND + IND + "b", 2 ) ).isTrue();
		assertThat( Indenter.canUnindent( IND + IND + "a\n" + IND + IND + "b\n" + IND + IND + "c", 2 ) ).isTrue();
	}

	@Test
	void testCanUnindentWithString() {
		assertThat( Indenter.canUnindent( null ) ).isFalse();

		assertThat( Indenter.canUnindent( "a<><><>", 3, "<>" ) ).isFalse();
		assertThat( Indenter.canUnindent( "<><><>a\nb<><><>", 3, "<>" ) ).isFalse();
		assertThat( Indenter.canUnindent( "<><><>a\n<><><>b\nc<><><>", 3, "<>" ) ).isFalse();

		assertThat( Indenter.canUnindent( "", 3, "<>" ) ).isFalse();
		assertThat( Indenter.canUnindent( "<><><>\n", 3, "<>" ) ).isFalse();
		assertThat( Indenter.canUnindent( "<><><>\n" + "<><><>\n", 3, "<>" ) ).isFalse();
		assertThat( Indenter.canUnindent( "<><><>\n" + "<><><>\n" + "<><><>\n", 3, "<>" ) ).isFalse();

		assertThat( Indenter.canUnindent( "", 3, "<>", true ) ).isTrue();
		assertThat( Indenter.canUnindent( "<><><>\n", 3, "<>", true ) ).isTrue();
		assertThat( Indenter.canUnindent( "<><><>\n" + "<><><>\n", 3, "<>", true ) ).isTrue();
		assertThat( Indenter.canUnindent( "<><><>\n" + "<><><>\n" + "<><><>\n", 3, "<>", true ) ).isTrue();

		assertThat( Indenter.canUnindent( "<><><>a", 3, "<>" ) ).isTrue();
		assertThat( Indenter.canUnindent( "<><><>a\n" + "<><><>b", 3, "<>" ) ).isTrue();
		assertThat( Indenter.canUnindent( "<><><>a\n" + "<><><>b\n" + "<><><>c", 3, "<>" ) ).isTrue();
	}

	@Test
	void testUnindent() {
		assertThat( Indenter.unindent( null )).isNull();

		assertThat( Indenter.unindent( "" ) ).isEqualTo( "");
		assertThat( Indenter.unindent( "\n" ) ).isEqualTo( "\n");

		assertThat( Indenter.unindent( IND ) ).isEqualTo( "");
		assertThat( Indenter.unindent( IND + "a" ) ).isEqualTo( "a");

		assertThat( Indenter.unindent( IND + "\n" + IND ) ).isEqualTo( "\n");
		assertThat( Indenter.unindent( IND + "a\n" + IND + "b" ) ).isEqualTo( "a\nb");

		assertThat( Indenter.unindent( IND + "\n" + IND + "\n" + IND ) ).isEqualTo( "\n\n");
		assertThat( Indenter.unindent( IND + "a\n" + IND + "b\n" + IND + "c" ) ).isEqualTo( "a\nb\nc");
	}

	@Test
	void testUnindentWithSize() {
		assertThat( Indenter.unindent( null, 2 ) ).isNull();

		assertThat( Indenter.unindent( "", 2 ) ).isEqualTo( "");
		assertThat( Indenter.unindent( "\n", 2 ) ).isEqualTo( "\n");

		assertThat( Indenter.unindent( IND + IND, 2 ) ).isEqualTo( "");
		assertThat( Indenter.unindent( IND + IND + "a", 2 ) ).isEqualTo( "a");

		assertThat( Indenter.unindent( IND + IND + "\n" + IND + IND, 2 ) ).isEqualTo( "\n");
		assertThat( Indenter.unindent( IND + IND + "a\n" + IND + IND + "b", 2 ) ).isEqualTo( "a\nb");

		assertThat( Indenter.unindent( IND + IND + "\n" + IND + IND + "\n" + IND + IND, 2 ) ).isEqualTo( "\n\n");
		assertThat( Indenter.unindent( IND + IND + "a\n" + IND + IND + "b\n" + IND + IND + "c", 2 ) ).isEqualTo( "a\nb\nc");
	}

	@Test
	void testUnindentWithString() {
		assertThat( Indenter.unindent( null, 3, "<>" ) ).isNull();

		assertThat( Indenter.unindent( "", 3, "<>" ) ).isEqualTo( "");
		assertThat( Indenter.unindent( "\n", 3, "<>" ) ).isEqualTo( "\n");

		assertThat( Indenter.unindent( "<><><>", 3, "<>" ) ).isEqualTo( "");
		assertThat( Indenter.unindent( "<><><>a", 3, "<>" ) ).isEqualTo( "a");

		assertThat( Indenter.unindent( "<><><>\n<><><>", 3, "<>" ) ).isEqualTo( "\n");
		assertThat( Indenter.unindent( "<><><>a\n<><><>b", 3, "<>" ) ).isEqualTo( "a\nb");

		assertThat( Indenter.unindent( "<><><>\n<><><>\n<><><>", 3, "<>" ) ).isEqualTo( "\n\n");
		assertThat( Indenter.unindent( "<><><>a\n<><><>b\n<><><>c", 3, "<>" ) ).isEqualTo( "a\nb\nc");
	}

	@Test
	void testTrim() {
		assertThat( Indenter.trim( null, null ) ).isNull();
		assertThat( Indenter.trim( "", "" ) ).isEqualTo( "");
		assertThat( Indenter.trim( "abc", "ac" ) ).isEqualTo( "b");
	}

	@Test
	void testTrimLines() {
		assertThat( Indenter.trimLines( null, null ) ).isNull();
		assertThat( Indenter.trimLines( "", "" ) ).isEqualTo( "");
	}

	private void assertWriteIndent( String result, int size ) throws IOException {
		StringWriter writer = new StringWriter();
		Indenter.writeIndent( writer, size );
		assertThat( writer.toString() ).isEqualTo( result);
	}

	private void assertWriteIndentWithString( String result, String text ) throws IOException {
		StringWriter writer = new StringWriter();
		Indenter.writeIndent( writer, text );
		assertThat( writer.toString() ).isEqualTo( result);
	}

	private void assertWriteIndentWithSizeAndString( String result, int size, String text ) throws IOException {
		StringWriter writer = new StringWriter();
		Indenter.writeIndent( writer, size, text );
		assertThat( writer.toString() ).isEqualTo( result);
	}

}
