package com.avereon.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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
		CatalogCard card = new CatalogCard();
		card.setTimestamp( System.currentTimeMillis() );
		card.setProducts( Set.of( "product1", "product2" ) );

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
		String store = mapper.writeValueAsString( card );

		BufferedReader reader = new BufferedReader( new StringReader( store ) );
		assertThat( reader.readLine() ).isEqualTo( "{" );
		assertThat( reader.readLine() ).isEqualTo( "  \"internalId\" : \"" + card.getInternalId() + "\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"timestamp\" : " + card.getTimestamp() + "," );
		assertThat( reader.readLine() ).satisfiesAnyOf(
			line -> assertThat( line ).isEqualTo( "  \"products\" : [ \"product1\", \"product2\" ]" ),
			line -> assertThat( line ).isEqualTo( "  \"products\" : [ \"product2\", \"product1\" ]" )
		);
		assertThat( reader.readLine() ).isEqualTo( "}" );
		assertThat( reader.readLine() ).isNull();
	}

}
