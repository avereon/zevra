package com.avereon.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class CatalogCardTest {

	@Test
	void testLoad() throws Exception {
		String data = "{\"timestamp\":\"1557457963562\",\"products\":[\"product1\",\"product2\"]}";
		RepoCard repo = new RepoCard();
		CatalogCard card = CatalogCard.load( repo, new ByteArrayInputStream( data.getBytes( StandardCharsets.UTF_8 ) ) );

		assertThat( card.getTimestamp(), is( 1557457963562L ) );
		assertThat( card.getProducts(), org.hamcrest.CoreMatchers.hasItems( "product1", "product2" ) );
	}

	@Test
	void testJsonMarshalling() throws Exception {
		RepoCard repo = new RepoCard();
		repo.setName( "Example Product Market" );
		repo.setUrl( "https://www.example.com/market" );
		repo.setIcon( "https://www.example.com/market/icon" );

		CatalogCard card = new CatalogCard();
		card.setTimestamp( System.currentTimeMillis() );
		card.setProducts( Set.of( "product1", "product2" ) );
		card.setRepo( repo );

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
		String store = mapper.writeValueAsString( card );

		BufferedReader reader = new BufferedReader( new StringReader( store ) );
		assertThat( reader.readLine(), is( "{" ) );
		assertThat( reader.readLine(), is( "  \"internalId\" : \"" + card.getInternalId() + "\"," ) );
		assertThat( reader.readLine(), is( "  \"repo\" : {" ) );
		assertThat( reader.readLine(), is( "    \"internalId\" : \"" + card.getRepo().getInternalId() + "\"," ) );
		assertThat( reader.readLine(), is( "    \"name\" : \"Example Product Market\"," ) );
		assertThat( reader.readLine(), is( "    \"icon\" : \"https://www.example.com/market/icon\"," ) );
		assertThat( reader.readLine(), is( "    \"url\" : \"https://www.example.com/market\"" ) );
		assertThat( reader.readLine(), is( "  }," ) );
		assertThat( reader.readLine(), is( "  \"timestamp\" : " + card.getTimestamp() + "," ) );
		assertThat( reader.readLine(), is( "  \"products\" : [ \"product2\", \"product1\" ]" ) );
		assertThat( reader.readLine(), is( "}" ) );
		assertThat( reader.readLine(), is( nullValue() ) );
	}

}
