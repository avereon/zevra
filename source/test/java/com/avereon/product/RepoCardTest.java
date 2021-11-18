package com.avereon.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RepoCardTest {

	@Test
	void testHashCode() {
		RepoCard a = new RepoCard();
		RepoCard b = new RepoCard();

		a.setName( "Example Repo" );
		a.setUrl( "http://example.com/repo" );

		b.setName( "Example Repo" );
		b.setUrl( "http://example.com/repo" );

		assertThat( System.identityHashCode( a ) ).isNotEqualTo( System.identityHashCode( b ) );
		assertThat( a.hashCode() ).isEqualTo( b.hashCode() );
	}

	@Test
	void testEquals() {
		RepoCard a = new RepoCard();
		RepoCard b = new RepoCard();

		a.setName( "Example Repo" );
		a.setUrl( "http://example.com/repo" );

		b.setName( "Example Repo" );
		b.setUrl( "http://example.com/repo" );

		assertThat( System.identityHashCode( a ) ).isNotEqualTo( System.identityHashCode( b ) );
		assertThat( a ).isEqualTo( b );
	}

	@Test
	void testJsonMarshalling() throws Exception {
		RepoCard card = new RepoCard();
		card.setName( "Example Repo" );
		card.setUrl( "http://example.com/repo" );
		card.setIcons( List.of( "http://example.com/repo/icon.png" ) );

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
		String store = mapper.writeValueAsString( card );

		BufferedReader reader = new BufferedReader( new StringReader( store ) );
		assertThat( reader.readLine() ).isEqualTo( "{" );
		assertThat( reader.readLine() ).isEqualTo( "  \"internalId\" : \"" + card.getInternalId() + "\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"name\" : \"Example Repo\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"icons\" : [ \"http://example.com/repo/icon.png\" ]," );
		assertThat( reader.readLine() ).isEqualTo( "  \"url\" : \"http://example.com/repo\"" );
		assertThat( reader.readLine() ).isEqualTo( "}" );
		assertThat( reader.readLine() ).isNull();
	}

	@Test
	void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "{\"name\" : \"Example Repo\", \"url\" : \"http://example.com/repo\"}";
		RepoCard card = new RepoCard().fromJson( new ByteArrayInputStream( state.getBytes( StandardCharsets.UTF_8 ) ), null );
		assertThat( card.getName() ).isEqualTo( "Example Repo" );
	}

}
