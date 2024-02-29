package com.avereon.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogCardTest {

	@Test
	void testLoad() throws Exception {
		String data = "{\"timestamp\":\"1557457963562\",\"products\":[\"product1\",\"product2\"]}";
		CatalogCard card = CatalogCard.fromJson( new ByteArrayInputStream( data.getBytes( StandardCharsets.UTF_8 ) ) );

		assertThat( card.getTimestamp() ).isEqualTo( 1557457963562L );
		assertThat( card.getProducts() ).contains( "product1", "product2" );
	}

	@Test
	void testJsonMarshalling() throws Exception {
		RepoCard repo = new RepoCard();
		repo.setName( "Example Product Market" );
		repo.setUrl( "https://www.example.com/market" );
		repo.setIcons( List.of( "https://www.example.com/market/icon" ) );

		CatalogCard card = new CatalogCard();
		card.setName( "Example Product Market" );
		card.setUrl( "https://www.example.com/market" );
		card.setIcons( List.of( "https://www.example.com/market/icon" ) );
		card.setTimestamp( System.currentTimeMillis() );
		card.setProducts( Set.of( "product1", "product2" ) );

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
		String store = mapper.writeValueAsString( card );

		BufferedReader reader = new BufferedReader( new StringReader( store ) );
		assertThat( reader.readLine() ).isEqualTo( "{" );
		assertThat( reader.readLine() ).isEqualTo( "  \"internalId\" : \"" + card.getInternalId() + "\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"name\" : \"Example Product Market\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"url\" : \"https://www.example.com/market\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"icons\" : [ \"https://www.example.com/market/icon\" ]," );
		assertThat( reader.readLine() ).isEqualTo( "  \"timestamp\" : " + card.getTimestamp() + "," );
		assertThat( reader.readLine() ).isEqualTo( "  \"products\" : [ \"product2\", \"product1\" ]" );
		//		assertThat( reader.readLine() ).isEqualTo( "  \"repo\" : {" );
		//		assertThat( reader.readLine() ).isEqualTo( "    \"internalId\" : \"" + card.getInternalId() + "\"," );
		//		assertThat( reader.readLine() ).isEqualTo( "    \"name\" : \"Example Product Market\"," );
		//		assertThat( reader.readLine() ).isEqualTo( "    \"url\" : \"https://www.example.com/market\"," );
		//		assertThat( reader.readLine() ).isEqualTo( "    \"icons\" : [ \"https://www.example.com/market/icon\" ]" );
		//		assertThat( reader.readLine() ).isEqualTo( "  }" );
		assertThat( reader.readLine() ).isEqualTo( "}" );
		assertThat( reader.readLine() ).isNull();
	}

}
