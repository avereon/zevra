package com.avereon.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductCardTest {

	@Test
	void testCard() {
		ProductCard card = ProductCard.card( getClass() );

		assertThat( card.getGroup(), is( "com.avereon" ) );
		assertThat( card.getArtifact(), is( "zevra" ) );
		assertThat( card.getVersion(), is( "0.0-SNAPSHOT" ) );
		assertThat( card.getTimestamp(), is( "2018-01-01 00:00:00" ) );

		assertThat( card.getName(), is( "Zevra" ) );
		assertThat( card.getIcons(), contains( "library" ) );
		assertThat( card.getProvider(), is( "Avereon" ) );
		assertThat( card.getInception(), is( 2018 ) );

		assertThat( card.getSummary(), is( "Utility library" ) );
		assertThat( card.getDescription(), is( "A utility library for Avereon applications." ) );
		assertThat( card.getCopyrightSummary(), is( "All rights reserved." ) );
		assertThat(
			card.getLicenseSummary(),
			is( "Zevra comes with ABSOLUTELY NO WARRANTY. This is open software, and you are welcome to redistribute it under certain conditions." )
		);
	}

	@Test
	void testJsonCard() {
		ProductCard card = new ProductCard().jsonCard( getClass() );
		assertThat( card.getProductKey(), is( "com.avereon.zevra" ) );

		assertThat( card.getGroup(), is( "com.avereon" ) );
		assertThat( card.getArtifact(), is( "zevra" ) );
		assertThat( card.getVersion(), is( "0.0-SNAPSHOT" ) );
		assertThat( card.getTimestamp(), is( "2018-01-01 00:00:00" ) );

		assertThat( card.getPackaging(), is( "lib" ) );
		assertThat( card.getPackagingVersion(), is( "2.7" ) );

		assertThat( card.getName(), is( "Zevra" ) );
		assertThat( card.getIcons(), contains( "library", "https://avereon.com/download/latest/zevra/product/icon" ) );
		assertThat( card.getProvider(), is( "Avereon" ) );
		assertThat( card.getProviderUrl(), is( "https://www.avereon.com" ) );
		assertThat( card.getInception(), is( 2018 ) );

		assertThat( card.getSummary(), is( "Utility library" ) );
		assertThat( card.getDescription(), is( "A utility library for Avereon applications." ) );
		assertThat( card.getCopyrightSummary(), is( "All rights reserved." ) );
		assertThat(
			card.getLicenseSummary(),
			is( "Zevra comes with ABSOLUTELY NO WARRANTY. This is open software, and you are welcome to redistribute it under certain conditions." )
		);

		assertNull( card.getProductUri() );
		assertThat( card.getJavaVersion(), is( "11" ) );

		List<Maintainer> maintainers = card.getMaintainers();
		assertThat( maintainers.get( 0 ).getName(), is( "Sole Maintainer" ) );
		assertThat( maintainers.get( 0 ).getEmail(), is( "sole.maintainer@example.com" ) );
		assertThat( maintainers.get( 0 ).getOrganization(), is( "Example" ) );
		assertThat( maintainers.get( 0 ).getOrganizationUrl(), is( "https://www.example.com" ) );
		assertThat( maintainers.get( 0 ).getTimezone(), is( nullValue() ) );
		assertThat( maintainers.get( 0 ).getRoles().get( 0 ), is( "Maintainer" ) );
		assertThat( maintainers.get( 0 ).getRoles().get( 1 ), is( "Developer" ) );
		assertThat( maintainers.get( 0 ).getRoles().size(), is( 2 ) );
		assertThat( maintainers.size(), is( 1 ) );

		List<Contributor> contributors = card.getContributors();
		assertThat( contributors.get( 0 ).getName(), is( "John Doe" ) );
		assertThat( contributors.get( 0 ).getEmail(), is( nullValue() ) );
		assertThat( contributors.get( 0 ).getOrganization(), is( nullValue() ) );
		assertThat( contributors.get( 0 ).getOrganizationUrl(), is( nullValue() ) );
		assertThat( contributors.get( 0 ).getTimezone(), is( nullValue() ) );
		assertThat( contributors.get( 0 ).getRoles().get( 0 ), is( "Just John" ) );
		assertThat( contributors.get( 0 ).getRoles().size(), is( 1 ) );
		assertThat( contributors.get( 1).getName(), is( "Jane Doe" ) );
		assertThat( contributors.get( 1 ).getEmail(), is( nullValue() ) );
		assertThat( contributors.get( 1 ).getOrganization(), is( nullValue() ) );
		assertThat( contributors.get( 1 ).getOrganizationUrl(), is( nullValue() ) );
		assertThat( contributors.get( 1 ).getTimezone(), is( nullValue() ) );
		assertThat( contributors.get( 1 ).getRoles().get( 0 ), is( "Just Jane" ) );
		assertThat( contributors.get( 1 ).getRoles().size(), is( 1 ) );
		assertThat( contributors.size(), is( 2 ) );
	}

	@Test
	void testHashCode() {
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
	void testEquals() {
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
	void testJsonMarshalling() throws Exception {
		Maintainer maintainer = new Maintainer();
		maintainer.setName( "Sole Maintainer" );
		maintainer.setEmail( "sole.maintainer@example.com" );
		maintainer.setOrganization( "Example" );
		maintainer.setOrganizationUrl( "https://www.example.com" );
		maintainer.setRoles( Arrays.asList( "Maintainer", "Developer", "Tester" ) );

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
		card.setArtifact( "zevra" );
		card.setPackaging( "lib" );
		card.setPackagingVersion( "2.7" );
		card.setVersion( "1.0.0" );
		card.setTimestamp( "2018-01-01 00:00:00" );
		card.setIcons( List.of( "avereon", "https://avereon.com/download/stable/avereon/provider/icon" ) );
		card.setName( "Zevra" );
		card.setMaintainers( maintainers );
		card.setContributors( contributors );

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure( SerializationFeature.INDENT_OUTPUT, true );
		String store = mapper.writeValueAsString( card );

		//System.out.println( store );

		BufferedReader reader = new BufferedReader( new StringReader( store ) );
		assertThat( reader.readLine(), is( "{" ) );
		assertThat( reader.readLine(), is( "  \"internalId\" : \"" + card.getInternalId() + "\"," ) );
		assertThat( reader.readLine(), is( "  \"group\" : \"com.avereon\"," ) );
		assertThat( reader.readLine(), is( "  \"artifact\" : \"zevra\"," ) );
		assertThat( reader.readLine(), is( "  \"version\" : \"1.0.0\"," ) );
		assertThat( reader.readLine(), is( "  \"timestamp\" : \"2018-01-01 00:00:00\"," ) );
		assertThat( reader.readLine(), is( "  \"packaging\" : \"lib\"," ) );
		assertThat( reader.readLine(), is( "  \"packagingVersion\" : \"2.7\"," ) );
		assertThat( reader.readLine(), is( "  \"icons\" : [ \"avereon\", \"https://avereon.com/download/stable/avereon/provider/icon\" ]," ) );
		assertThat( reader.readLine(), is( "  \"name\" : \"Zevra\"," ) );
		assertThat( reader.readLine(), is( "  \"provider\" : null," ) );
		assertThat( reader.readLine(), is( "  \"providerUrl\" : null," ) );
		assertThat( reader.readLine(), is( "  \"inception\" : 0," ) );
		assertThat( reader.readLine(), is( "  \"summary\" : null," ) );
		assertThat( reader.readLine(), is( "  \"description\" : null," ) );
		assertThat( reader.readLine(), is( "  \"copyrightSummary\" : null," ) );
		assertThat( reader.readLine(), is( "  \"licenseSummary\" : null," ) );
		assertThat( reader.readLine(), is( "  \"javaVersion\" : null," ) );
		assertThat( reader.readLine(), is( "  \"installFolder\" : null," ) );
		assertThat( reader.readLine(), is( "  \"maintainers\" : [ {" ) );
		assertThat( reader.readLine(), is( "    \"name\" : \"Sole Maintainer\"," ) );
		assertThat( reader.readLine(), is( "    \"email\" : \"sole.maintainer@example.com\"," ) );
		assertThat( reader.readLine(), is( "    \"timezone\" : null," ) );
		assertThat( reader.readLine(), is( "    \"organization\" : \"Example\"," ) );
		assertThat( reader.readLine(), is( "    \"organizationUrl\" : \"https://www.example.com\"," ) );
		assertThat( reader.readLine(), is( "    \"roles\" : [ \"Maintainer\", \"Developer\", \"Tester\" ]" ) );
		assertThat( reader.readLine(), is( "  } ]," ) );
		assertThat( reader.readLine(), is( "  \"contributors\" : [ {" ) );
		assertThat( reader.readLine(), is( "    \"name\" : \"Contributor One\"," ) );
		assertThat( reader.readLine(), is( "    \"email\" : null," ) );
		assertThat( reader.readLine(), is( "    \"timezone\" : null," ) );
		assertThat( reader.readLine(), is( "    \"organization\" : null," ) );
		assertThat( reader.readLine(), is( "    \"organizationUrl\" : null," ) );
		assertThat( reader.readLine(), is( "    \"roles\" : [ \"Consultant\", \"Beta User\" ]" ) );
		assertThat( reader.readLine(), is( "  }, {" ) );
		assertThat( reader.readLine(), is( "    \"name\" : \"Contributor Two\"," ) );
		assertThat( reader.readLine(), is( "    \"email\" : null," ) );
		assertThat( reader.readLine(), is( "    \"timezone\" : null," ) );
		assertThat( reader.readLine(), is( "    \"organization\" : null," ) );
		assertThat( reader.readLine(), is( "    \"organizationUrl\" : null," ) );
		assertThat( reader.readLine(), is( "    \"roles\" : [ \"Philosopher\" ]" ) );
		assertThat( reader.readLine(), is( "  } ]," ) );
		assertThat( reader.readLine(), is( "  \"enabled\" : false," ) );
		assertThat( reader.readLine(), is( "  \"removable\" : false" ) );
		assertThat( reader.readLine(), is( "}" ) );
		assertThat( reader.readLine(), is( nullValue() ) );
	}

	@Test
	void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "{\"name\" : \"Zevra\", \"extra\" : \"unknown\"}";
		ProductCard card = new ProductCard().fromJson( new ByteArrayInputStream( state.getBytes( StandardCharsets.UTF_8 ) ), null );
		assertThat( card.getName(), is( "Zevra" ) );
	}

}
