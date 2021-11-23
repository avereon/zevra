package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

class ParametersTest {

	@Test
	@SuppressWarnings( "ConstantConditions" )
	void testNull() {
		try {
			Parameters.parse( (List<String>)null );
			fail( "Parameters.parse(null) should throw a NullPointerException." );
		} catch( NullPointerException exception ) {
			//
		}
	}

	@Test
	void testEmpty() {
		Parameters parameters = Parameters.parse();
		assertThat( parameters.size() ).isEqualTo( 0 );
	}

	@Test
	void testEmptyValues() {
		Parameters parameters = Parameters.parse();
		assertThat( parameters.getUris().size() ).isEqualTo( 0 );
		assertThat( parameters.getValues( "help" ).size() ).isEqualTo( 0 );
		assertThat( parameters.getOriginalCommands().size() ).isEqualTo( 0 );
		assertThat( parameters.getResolvedCommands().size() ).isEqualTo( 0 );
	}

	@Test
	void testParse() {
		Parameters parameters = Parameters.parse( "-help" );
		assertThat( parameters.get( "help" ) ).isEqualTo( "true" );
	}

	@Test
	void testParseWithValue() {
		Parameters parameters = Parameters.parse( "-help", "topic" );
		assertThat( parameters.get( "help" ) ).isEqualTo( "topic" );
	}

	@Test
	void testParseWithValue2() {
		String key = "key";
		String value = "value";
		String notakey = "notakey";
		String[] args = new String[]{ "-" + key, value };
		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.isTrue( notakey ) ).isFalse();
		assertThat( parameters.get( key ) ).isEqualTo( value );
	}

	@Test
	void testParseWithMultiValues() {
		Parameters parameters = Parameters.parse( "-module", "a", "-module", "b", "--module", "c" );
		assertThat( parameters.getValues( "module" ) ).contains( "a", "b", "c" );
	}

	@Test
	void testParseWithValueAndFile() {
		Parameters parameters = Parameters.parse( "-help", "topic", "test.txt" );
		assertThat( parameters.get( "help" ) ).isEqualTo( "topic" );
		assertThat( parameters.getUris() ).contains( UriUtil.resolve( "test.txt" ).toString() );
	}

	@Test
	void testParseWithValueAndFiles() {
		Parameters parameters = Parameters.parse( "-help", "topic", "test1.txt", "test2.txt" );
		assertThat( parameters.get( "help" ) ).isEqualTo( "topic" );
		assertThat( parameters.getUris() ).contains( UriUtil.resolve( "test1.txt" ).toString(), UriUtil.resolve( "test2.txt" ).toString() );
	}

	@Test
	void testParseWithFlags() {
		Parameters parameters = Parameters.parse( "-one", "-two", "-three" );
		assertThat( parameters.get( "one" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "two" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "three" ) ).isEqualTo( "true" );
	}

	@Test
	void testParseWithFlagAndFile() {
		Parameters parameters = Parameters.parse( new String[]{ "-help", "topic", "test.txt" }, "-help" );
		assertThat( parameters.get( "help" ) ).isEqualTo( "topic" );
		assertThat( parameters.getUris() ).contains( UriUtil.resolve( "test.txt" ).toString() );
	}

	@Test
	void testParseWithUnknownFlagAndFile() {
		try {
			Parameters.parse( new String[]{ "-help", "topic", "-test", "test", "test.txt" }, "-help" );
			fail( "Unknown flags should cause an exception" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage() ).isEqualTo( "Unknown command: -test" );
		}
	}

	@Test
	void testParseWithFlag() {
		String flag = "flag";
		String notaflag = "notaflag";
		String[] args = new String[]{ "-" + flag };
		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.isTrue( notaflag ) ).isFalse();
		assertThat( parameters.isTrue( flag ) ).isTrue();
	}

	@Test
	void testParseWithFlags2() {
		String notaflag = "notaflag";
		List<String> flags = new ArrayList<>();
		flags.add( "flag1" );
		flags.add( "flag2" );
		flags.add( "flag3" );
		flags.add( "flag4" );
		flags.add( "flag5" );

		String[] args = new String[ flags.size() ];
		int count = args.length;
		for( int index = 0; index < count; index++ ) {
			args[ index ] = "-" + flags.get( index );
		}

		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.isTrue( notaflag ) ).isFalse();
		for( String flag : flags ) {
			assertThat( parameters.isTrue( flag ) ).isTrue();
		}
	}

	@Test
	void testParseWithValues() {
		int count = 5;
		String notakey = "notakey";
		List<String> keys = new ArrayList<>();
		List<String> values = new ArrayList<>();
		for( int index = 0; index < count; index++ ) {
			keys.add( "key" + index );
			values.add( "value" + index );
		}

		String[] args = new String[ keys.size() + values.size() ];
		for( int index = 0; index < count; index++ ) {
			args[ index * 2 ] = "-" + keys.get( index );
			args[ index * 2 + 1 ] = values.get( index );
		}

		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.isSet( notakey ) ).isFalse();
		assertThat( parameters.isTrue( notakey ) ).isFalse();
		for( int index = 0; index < count; index++ ) {
			assertThat( parameters.get( keys.get( index ) ) ).isEqualTo( values.get( index ) );
		}
	}

	@Test
	void testParseWithEscapedValues() {
		Parameters parameters = Parameters.parse( "--test", "go", "\\-help" );
		assertThat( parameters.isSet( "test" ) ).isTrue();
		assertThat( parameters.isTrue( "test" ) ).isFalse();
		assertThat( parameters.getValues( "test" ) ).contains( "go", "-help" );
	}

	@Test
	void testParseFlagsWithValue() {
		String[] args = new String[]{ "-flag1", "-key", "value", "-flag2" };
		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.isTrue( "flag1" ) ).isTrue();
		assertThat( parameters.get( "key" ) ).isEqualTo( "value" );
		assertThat( parameters.isTrue( "flag2" ) ).isTrue();
	}

	@Test
	void testParseValuesWithFlag() {
		String[] args = new String[]{ "-key1", "value1", "-flag", "-key2", "value2" };
		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.get( "key1" ) ).isEqualTo( "value1" );
		assertThat( parameters.isTrue( "flag" ) ).isTrue();
		assertThat( parameters.get( "key2" ) ).isEqualTo( "value2" );
	}

	@Test
	void testParseWithFile() {
		String filename = "test.file";
		String[] args = new String[]{ filename };
		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.getUris() ).contains( UriUtil.resolve( filename ).toString() );
	}

	@Test
	void testParseWithFiles() {
		int count = 5;
		List<String> list = new ArrayList<>();
		for( int index = 0; index < count; index++ ) {
			list.add( "test" + index + ".file" );
		}

		String[] args = list.toArray( new String[ 0 ] );
		Parameters parameters = Parameters.parse( args );

		List<String> uris = parameters.getUris();
		for( int index = 0; index < count; index++ ) {
			assertThat( uris.get( index ) ).isEqualTo( UriUtil.resolve( "test" + index + ".file" ).toString() );
		}
		assertThat( uris.size() ).isEqualTo( count );
	}

	@Test
	void testParseFlagWithFile() {
		String filename = "file";
		String[] args = new String[]{ "-flag", "--", filename };
		Parameters parameters = Parameters.parse( args );

		assertThat( parameters.isTrue( "flag" ) ).isTrue();
		assertThat( parameters.getUris() ).contains( UriUtil.resolve( filename ).toString() );
	}

	@Test
	void testParseWithNullEntry() {
		String[] args = new String[ 1 ];
		try {
			Parameters.parse( args );
			fail( "Null values should cause an exception" );
		} catch( IllegalArgumentException exception ) {
			assertThat( exception.getMessage() ).isEqualTo( "Null command at index: 0" );
		} catch( Exception exception ) {
			exception.printStackTrace( System.out );
			fail( "Unexpected exception " + exception );
		}
	}

	@Test
	void testParseWithJavaFxFlag() {
		String[] commands = new String[]{ "--execmode=dev" };
		Parameters parameters = Parameters.parse( commands );

		assertThat( parameters.getOriginalCommands() ).contains( commands );
		assertThat( parameters.getFlags() ).contains( "--execmode" );
		assertThat( parameters.get( "execmode" ) ).isEqualTo( "dev" );
	}

	@Test
	void testAdd() {
		String[] args = new String[]{ "-flag", "-key", "value1", "file1" };
		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.get( "none" ) ).isNull();
		assertThat( parameters.get( "flag" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "key" ) ).isEqualTo( "value1" );
		assertThat( parameters.getUris() ).contains( UriUtil.resolve( "file1" ).toString() );

		String[] addArgs = new String[]{ "-flag", "false", "-key", "value2", "file2" };
		Parameters addParameters = Parameters.parse( addArgs );
		parameters.add( addParameters );

		assertThat( parameters.get( "none" ) ).isNull();
		assertThat( parameters.get( "flag" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "key" ) ).isEqualTo( "value1" );
		assertThat( parameters.getUris() ).contains( UriUtil.resolve( "file1" ).toString(), UriUtil.resolve( "file2" ).toString() );
	}

	@Test
	void testGet() {
		String[] args = new String[]{ "-flag", "-key", "value", "file" };
		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.get( "none" ) ).isNull();
		assertThat( parameters.get( "flag" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "key" ) ).isEqualTo( "value" );

		assertThat( parameters.get( "-none" ) ).isNull();
		assertThat( parameters.get( "-flag" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "-key" ) ).isEqualTo( "value" );

		assertThat( parameters.get( "--none" ) ).isNull();
		assertThat( parameters.get( "--flag" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "--key" ) ).isEqualTo( "value" );
	}

	@Test
	void testGetWithDefault() {
		String[] args = new String[]{ "-flag", "-key", "value", "file" };
		Parameters parameters = Parameters.parse( args );

		assertThat( parameters.get( "none", "true" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "flag", "false" ) ).isEqualTo( "true" );
		assertThat( parameters.get( "key", null ) ).isEqualTo( "value" );
	}

	@Test
	void testGetFlags() {
		String[] args = new String[]{ "-flag", "-key", "value", "--flag", "value1", "value2", "value3", "--", "file0", "file1" };
		Parameters parameters = Parameters.parse( args );

		assertThat( parameters.getFlags() ).contains( "--flag", "-flag", "-key" );
		assertThat( parameters.getValues( "flag" ) ).contains( "true", "value1", "value2", "value3" );
	}

	@Test
	void testGetValues() {
		String[] args = new String[]{ "--flag", "value0", "value1", "value2" };
		Parameters parameters = Parameters.parse( args );

		assertThat( parameters.getValues( "flag" ) ).contains( "value0", "value1", "value2" );
		assertThat( parameters.getValues( "-flag" ) ).contains( "value0", "value1", "value2" );
		assertThat( parameters.getValues( "--flag" ) ).contains( "value0", "value1", "value2" );
	}

	@Test
	void testGetValuesWithFlags() {
		String[] args = new String[]{ "--flag", "value0", "value1", "value2", "-other" };
		Parameters parameters = Parameters.parse( args );

		assertThat( parameters.getValues( "flag" ) ).contains( "value0", "value1", "value2" );

		assertThat( parameters.isSet( "other" ) ).isTrue();
		assertThat( parameters.isTrue( "other" ) ).isTrue();
	}

	@Test
	void testGetValuesWithFiles() {
		String[] args = new String[]{ "--flag", "value0", "value1", "value2", "--", "file1.txt" };
		Parameters parameters = Parameters.parse( args );

		assertThat( parameters.getValues( "flag" ) ).contains( "value0", "value1", "value2" );

		assertThat( parameters.getUris() ).contains( UriUtil.resolve( "file1.txt" ).toString() );
	}

	@Test
	void testGetValuesWithFlagsAndFiles() {
		String[] args = new String[]{ "--flag", "value0", "value1", "value2", "-other", "test", "file1.txt" };
		Parameters parameters = Parameters.parse( args );

		assertThat( parameters.getValues( "flag" ) ).contains( "value0", "value1", "value2" );

		assertThat( parameters.isSet( "other" ) ).isTrue();
		assertThat( parameters.isTrue( "other" ) ).isFalse();

		assertThat( parameters.getUris() ).contains( UriUtil.resolve( "file1.txt" ).toString() );
	}

	@Test
	void testGetValuesWithQuotes() {
		String[] commands = new String[]{ "-title", "The Title of the Program" };
		Parameters parameters = Parameters.parse( commands );

		assertThat( parameters.get( "title" ) ).isEqualTo( "The Title of the Program" );
	}

	@Test
	void testIsSet() {
		String[] args = new String[]{ "-flag1", "false", "-flag2", "true", "-flag3" };
		Parameters parameters = Parameters.parse( args );
		assertThat( parameters.isSet( "flag0" ) ).isFalse();
		assertThat( parameters.isTrue( "flag0" ) ).isFalse();

		assertThat( parameters.isSet( "flag1" ) ).isTrue();
		assertThat( parameters.isTrue( "flag1" ) ).isFalse();

		assertThat( parameters.isSet( "flag2" ) ).isTrue();
		assertThat( parameters.isTrue( "flag2" ) ).isTrue();

		assertThat( parameters.isSet( "flag3" ) ).isTrue();
		assertThat( parameters.isTrue( "flag3" ) ).isTrue();

		assertThat( parameters.isSet( "-flag0" ) ).isFalse();
		assertThat( parameters.isTrue( "-flag0" ) ).isFalse();

		assertThat( parameters.isSet( "-flag1" ) ).isTrue();
		assertThat( parameters.isTrue( "-flag1" ) ).isFalse();

		assertThat( parameters.isSet( "-flag2" ) ).isTrue();
		assertThat( parameters.isTrue( "-flag2" ) ).isTrue();

		assertThat( parameters.isSet( "-flag3" ) ).isTrue();
		assertThat( parameters.isTrue( "-flag3" ) ).isTrue();

		assertThat( parameters.isSet( "--flag0" ) ).isFalse();
		assertThat( parameters.isTrue( "--flag0" ) ).isFalse();

		assertThat( parameters.isSet( "--flag1" ) ).isTrue();
		assertThat( parameters.isTrue( "--flag1" ) ).isFalse();

		assertThat( parameters.isSet( "--flag2" ) ).isTrue();
		assertThat( parameters.isTrue( "--flag2" ) ).isTrue();

		assertThat( parameters.isSet( "--flag3" ) ).isTrue();
		assertThat( parameters.isTrue( "--flag3" ) ).isTrue();
	}

	@Test
	void testGetOriginalCommands() {
		List<String> args = Arrays.asList( "-flag", "-key", "value", "file" );
		Parameters parameters = Parameters.parse( args );
		List<String> commands = parameters.getOriginalCommands();

		assertThat( parameters.getOriginalCommands() ).isNotSameAs( args );
		assertThat( commands ).contains( "-flag", "-key", "value", "file" );
	}

	@Test
	void testIdempotentParse() {
		String[] commands = new String[]{ "-flag", "-key", "value", "file" };
		Parameters parameters = Parameters.parse( commands );
		assertThat( Parameters.parse( parameters.getOriginalCommands() ) ).isEqualTo( parameters );
	}

	@Test
	void testHashCode() {
		String[] commands = new String[]{ "-flag", "-key", "value", "file" };
		assertThat( Parameters.parse( commands ).hashCode() ).isEqualTo( Parameters.parse( commands ).hashCode() );
	}

	@Test
	void testEquals() {
		String[] commands = new String[]{ "-flag", "-key", "value", "file" };
		Parameters parameters1 = Parameters.parse( commands );
		Parameters parameters2 = Parameters.parse( commands );
		Parameters parameters3 = Parameters.parse( "-flag", "-key", "value", "otherfile" );

		assertThat( parameters1.equals( parameters2 ) ).isTrue();
		assertThat( parameters2.equals( parameters1 ) ).isTrue();
		assertThat( parameters1.equals( parameters3 ) ).isFalse();
		assertThat( parameters3.equals( parameters1 ) ).isFalse();
	}

}
