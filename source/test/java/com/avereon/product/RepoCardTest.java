package com.avereon.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class RepoCardTest {

	@Test
	public void testHashCode() {
		RepoCard a = new RepoCard();
		RepoCard b = new RepoCard();

		a.setName( "Example Repo" );
		a.setUrl( "http://example.com/repo" );

		b.setName( "Example Repo" );
		b.setUrl( "http://example.com/repo" );

		assertThat( System.identityHashCode( a ), not( is( System.identityHashCode( b ) ) ) );
		assertThat( a.hashCode(), is( b.hashCode() ) );
	}

	@Test
	public void testEquals() {
		RepoCard a = new RepoCard();
		RepoCard b = new RepoCard();

		a.setName( "Example Repo" );
		a.setUrl( "http://example.com/repo" );

		b.setName( "Example Repo" );
		b.setUrl( "http://example.com/repo" );

		assertThat( System.identityHashCode( a ), not( is( System.identityHashCode( b ) ) ) );
		assertThat( a, is( b ) );
	}

	@Test
	public void testJsonMarshalling() throws Exception {
		RepoCard card = new RepoCard();
		card.setName( "Example Repo" );
		card.setUrl( "http://example.com/repo" );
		card.setIcon( "http://example.com/repo/icon.png" );

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
		String store = mapper.writeValueAsString( card );

		//System.out.println( store );

		BufferedReader reader = new BufferedReader( new StringReader( store ) );
		assertThat( reader.readLine(), is( "{" ) );
		assertThat( reader.readLine(), is( "  \"internalId\" : " + card.getInternalId() + "," ) );
		assertThat( reader.readLine(), is( "  \"name\" : \"Example Repo\"," ) );
		assertThat( reader.readLine(), is( "  \"icon\" : \"http://example.com/repo/icon.png\"," ) );
		assertThat( reader.readLine(), is( "  \"url\" : \"http://example.com/repo\"" ) );
		assertThat( reader.readLine(), is( "}" ) );
		assertThat( reader.readLine(), is( nullValue() ) );
	}

	@Test
	public void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "{\"name\" : \"Example Repo\", \"url\" : \"http://example.com/repo\"}";
		RepoCard card = new RepoCard().load( new ByteArrayInputStream( state.getBytes( StandardCharsets.UTF_8 ) ), null );
		assertThat( card.getName(), is( "Example Repo" ) );
	}

}
