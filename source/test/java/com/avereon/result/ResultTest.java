package com.avereon.result;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class ResultTest {

	@Test
	void testEmpty() {
		Result<?> result = Result.empty();
		assertThat( result.isEmpty() ).isTrue();
		assertThat( result.isPresent() ).isFalse();
	}

	@Test
	void testResult() {
		Result<String> result = Result.of( "value" );
		assertThat( result.isEmpty() ).isFalse();
		assertThat( result.isPresent() ).isTrue();
	}

	@Test
	void testIsPresent() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( "present" );
		result.ifPresent( results::add );

		assertThat( results.get( 0 ) ).isEqualTo( "present" );
	}

	@Test
	void testIsPresentWithException() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( new RuntimeException() );
		result.ifPresent( results::add );

		assertThat( results.size() ).isEqualTo( 0 );
	}

	@Test
	void testIfSuccess() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( "success" );
		result.ifSuccess( results::add );

		assertThat( results.get( 0 ) ).isEqualTo( "success" );
	}

	@Test
	void testIfSuccessWithException() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( new RuntimeException() );
		result.ifSuccess( results::add );

		assertThat( results.size() ).isEqualTo( 0 );
	}

	@Test
	void testIfFailure() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( new RuntimeException( "failure" ) );
		result.ifFailure( e -> results.add( e.getMessage() ) );

		assertThat( results.get( 0 ) ).isEqualTo( "failure" );
	}

	@Test
	void testIfFailureWithoutAnException() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( "success" );
		result.ifFailure( e -> results.add( e.getMessage() ) );

		assertThat( results.size() ).isEqualTo( 0 );
	}

	@Test
	@SuppressWarnings( "CatchMayIgnoreException" )
	void testAcceptExceptionWithAnException() {
		Result<String> result = Result.of( new RuntimeException( "test exception" ) );

		try {
			result.tryException();
			fail( "Exception not thrown as expected" );
		} catch( RuntimeException exception ) {
			assertThat( exception.getMessage() ).isEqualTo( "test exception" );
		} catch( Exception exception ) {
			fail( "RuntimeException not handled as expected" );
		}
	}

	@Test
	void testAcceptExceptionWithoutAnException() {
		Result<String> result = Result.of( "value" );

		try {
			result.tryException();
		} catch( FileNotFoundException exception ) {
			System.out.println( "File not found: " + exception.getMessage() );
		} catch( Exception exception ) {
			System.out.println( "Some other exception" );
		}
	}

	@Test
	void testResultChaining() {
		List<String> results = new ArrayList<>();
		Result<String> result = Result.of( "result" ).andThen( r -> {
			results.add( "andThen" );
			return r;
		} ).andThen( r -> {
			results.add( "ifPresent" );
			return r;
		} );

		assertThat( result.isPresent() ).isTrue();
		assertThat( result.get() ).isEqualTo( "result" );
		assertThat( results ).contains( "andThen", "ifPresent" );
	}

	@Test
	void testResultChainingWithException() {
		List<String> results = new ArrayList<>();
		Result<String> result = Result.of( "result" ).andThen( r -> {
			results.add( "andThen" );
			return r;
		} ).andThen( r -> {
			results.add( "andThen" );
			return r;
		} );

		assertThat( result.isPresent() ).isTrue();
		assertThat( result.get() ).isEqualTo( "result" );
		assertThat( results ).contains( "andThen", "andThen" );
	}

}
