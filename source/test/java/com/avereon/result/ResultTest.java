package com.avereon.result;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

public class ResultTest {

	@Test
	void testEmpty() {
		Result<?> result = Result.empty();
		assertTrue( result.isEmpty() );
		assertFalse( result.isPresent() );
	}

	@Test
	void testResult() {
		Result<String> result = Result.of( "value" );
		assertFalse( result.isEmpty() );
		assertTrue( result.isPresent() );
	}

	@Test
	void testIsPresent() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( "present" );
		result.ifPresent( results::add );

		assertThat( results.get( 0 ), is( "present" ) );
	}

	@Test
	void testIsPresentWithException() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( new RuntimeException() );
		result.ifPresent( results::add );

		assertThat( results.size(), is( 0 ) );
	}

	@Test
	void testIfSuccess() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( "success" );
		result.ifSuccess( results::add );

		assertThat( results.get( 0 ), is( "success" ) );
	}

	@Test
	void testIfSuccessWithException() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( new RuntimeException() );
		result.ifSuccess( results::add );

		assertThat( results.size(), is( 0 ) );
	}

	@Test
	void testIfFailure() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( new RuntimeException( "failure" ) );
		result.ifFailure( e -> results.add( e.getMessage() ) );

		assertThat( results.get( 0 ), is( "failure" ) );
	}

	@Test
	void testIfFailureWithoutAnException() {
		List<String> results = new ArrayList<>();

		Result<String> result = Result.of( "success" );
		result.ifFailure( e -> results.add( e.getMessage() ) );

		assertThat( results.size(), is( 0 ) );
	}

	@Test
	void testAcceptExceptionWithAnException() {
		Result<String> result = Result.of( new RuntimeException( "test exception" ) );

		try {
			result.tryException();
			fail( "Exception not thrown as expected" );
		} catch( RuntimeException exception ) {
			assertThat( exception.getMessage(), is( "test exception" ) );
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

		assertTrue( result.isPresent() );
		assertThat( result.get(), is( "result" ) );
		assertThat( results, contains( "andThen", "ifPresent" ) );
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

		assertTrue( result.isPresent() );
		assertThat( result.get(), is( "result" ) );
		assertThat( results, contains( "andThen", "andThen" ) );
	}

}
