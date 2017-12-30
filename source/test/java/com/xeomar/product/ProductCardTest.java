package com.xeomar.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xeomar.util.Contributor;
import com.xeomar.util.Maintainer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProductCardTest {

	@Test
	public void testEquals() {
		ProductCard a = new ProductCard();
		ProductCard b = new ProductCard();

		a.setGroup( "com.example" );
		a.setArtifact( "artifact" );

		b.setGroup( "com.example" );
		b.setArtifact( "artifact" );

		assertThat( a, is( b ) );
	}

	@Test
	public void testJsonMarshalling() throws Exception {
		Maintainer maintainer = new Maintainer();
		maintainer.setName( "Sole Maintainer" );
		maintainer.setRoles( Arrays.asList( "Architect", "Developer", "Tester" ) );

		Contributor contributor1 = new Contributor();
		contributor1.setName( "Contributor One" );
		contributor1.setRoles( Arrays.asList( "Consultant", "Beta User" ) );

		Contributor contributor2 = new Contributor();
		contributor2.setName( "Contributor Two" );
		contributor2.setRoles( Collections.singletonList( "Philosopher" ) );

		List<Maintainer> maintainers = new ArrayList<>();
		maintainers.add(maintainer);

		List<Contributor> contributors = new ArrayList<>();
		contributors.add( contributor1 );
		contributors.add( contributor2 );

		ProductCard card = new ProductCard();
		card.setGroup( "com.xeomar" );
		card.setArtifact( "razor" );
		card.setVersion( "1.0.0" );
		card.setTimestamp( "2018-01-01 00:00:00" );
		card.setName( "Razor" );
		card.setMaintainers( maintainers );
		card.setContributors( contributors );

		ObjectMapper mapper = new ObjectMapper();
		//mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );
		//mapper.configure( SerializationFeature.WRITE_NULL_MAP_VALUES, false );
		mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
		String store = mapper.writeValueAsString( card );

		System.out.println( store );
	}

}
