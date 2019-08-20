package com.avereon.product;

import com.avereon.util.Contributor;
import com.avereon.util.Maintainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ProductCardTest {

	@Test
	public void testHashCode() {
		ProductCard a = new ProductCard();
		ProductCard b = new ProductCard();

		a.setGroup( "com.example" );
		a.setArtifact( "artifact" );

		b.setGroup( "com.example" );
		b.setArtifact( "artifact" );

		assertThat( System.identityHashCode( a ), not( is( System.identityHashCode( b ) ) ) );
		assertThat( a.hashCode(), is( b.hashCode() ) );
	}

	@Test
	public void testEquals() {
		ProductCard a = new ProductCard();
		ProductCard b = new ProductCard();

		a.setGroup( "com.example" );
		a.setArtifact( "artifact" );

		b.setGroup( "com.example" );
		b.setArtifact( "artifact" );

		assertThat( System.identityHashCode( a ), not( is( System.identityHashCode( b ) ) ) );
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
		maintainers.add( maintainer );

		List<Contributor> contributors = new ArrayList<>();
		contributors.add( contributor1 );
		contributors.add( contributor2 );

		ProductCard card = new ProductCard();
		card.setGroup( "com.avereon" );
		card.setArtifact( "razor" );
		card.setVersion( "1.0.0" );
		card.setTimestamp( "2018-01-01 00:00:00" );
		card.setName( "Razor" );
		card.setMaintainers( maintainers );
		card.setContributors( contributors );

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
		String store = mapper.writeValueAsString( card );

		//System.out.println( store );

		BufferedReader reader = new BufferedReader( new StringReader( store ) );
		assertThat( reader.readLine(), is( "{" ) );
		//assertThat( reader.readLine(), is( "  \"productKey\" : com.avereon.razor," ) );
		assertThat( reader.readLine(), is( "  \"internalId\" : \"" + card.getInternalId() + "\"," ) );
		assertThat( reader.readLine(), is( "  \"group\" : \"com.avereon\"," ) );
		assertThat( reader.readLine(), is( "  \"artifact\" : \"razor\"," ) );
		assertThat( reader.readLine(), is( "  \"version\" : \"1.0.0\"," ) );
		assertThat( reader.readLine(), is( "  \"timestamp\" : \"2018-01-01 00:00:00\"," ) );
		//assertThat( reader.readLine(), is( "  \"release\" : null," ) );
		assertThat( reader.readLine(), is( "  \"iconUri\" : null," ) );
		assertThat( reader.readLine(), is( "  \"name\" : \"Razor\"," ) );
		assertThat( reader.readLine(), is( "  \"provider\" : null," ) );
		assertThat( reader.readLine(), is( "  \"providerUrl\" : null," ) );
		assertThat( reader.readLine(), is( "  \"inception\" : 0," ) );
		assertThat( reader.readLine(), is( "  \"summary\" : null," ) );
		assertThat( reader.readLine(), is( "  \"description\" : null," ) );
		assertThat( reader.readLine(), is( "  \"copyrightSummary\" : null," ) );
		assertThat( reader.readLine(), is( "  \"licenseSummary\" : null," ) );
		assertThat( reader.readLine(), is( "  \"productUri\" : null," ) );
		assertThat( reader.readLine(), is( "  \"mainClass\" : null," ) );
		assertThat( reader.readLine(), is( "  \"javaVersion\" : null," ) );
		assertThat( reader.readLine(), is( "  \"installFolder\" : null," ) );
		//		assertThat( reader.readLine(), is( "  \"maintainers\" : [ {" ) );
		//		assertThat( reader.readLine(), is( "    \"name\" : \"Sole Maintainer\"," ) );
		//		assertThat( reader.readLine(), is( "    \"email\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"timezone\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"organization\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"organizationUrl\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"roles\" : [ \"Architect\", \"Developer\", \"Tester\" ]" ) );
		//		assertThat( reader.readLine(), is( "  } ]," ) );
		//		assertThat( reader.readLine(), is( "  \"contributors\" : [ {" ) );
		//		assertThat( reader.readLine(), is( "    \"name\" : \"Contributor One\"," ) );
		//		assertThat( reader.readLine(), is( "    \"email\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"timezone\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"organization\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"organizationUrl\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"roles\" : [ \"Consultant\", \"Beta User\" ]" ) );
		//		assertThat( reader.readLine(), is( "  }, {" ) );
		//		assertThat( reader.readLine(), is( "    \"name\" : \"Contributor Two\"," ) );
		//		assertThat( reader.readLine(), is( "    \"email\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"timezone\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"organization\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"organizationUrl\" : null," ) );
		//		assertThat( reader.readLine(), is( "    \"roles\" : [ \"Philosopher\" ]" ) );
		//		assertThat( reader.readLine(), is( "  } ]," ) );
		assertThat( reader.readLine(), is( "  \"enabled\" : false," ) );
		assertThat( reader.readLine(), is( "  \"removable\" : false" ) );
		assertThat( reader.readLine(), is( "}" ) );
		assertThat( reader.readLine(), is( nullValue() ) );
	}

	@Test
	public void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "{\"name\" : \"Razor\", \"extra\" : \"unknown\"}";
		ProductCard card = new ProductCard().load( new ByteArrayInputStream( state.getBytes( "UTF-8" ) ), null );
		assertThat( card.getName(), is( "Razor" ) );
	}

}