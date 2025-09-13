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

import static org.assertj.core.api.Assertions.assertThat;

class ProductCardTest {

	@Test
	void testCard() {
		ProductCard card = ProductCard.info( getClass() );

		assertThat( card.getGroup() ).isEqualTo( "com.avereon" );
		assertThat( card.getArtifact() ).isEqualTo( "zevra" );
		assertThat( card.getVersion() ).isEqualTo( "0.0-SNAPSHOT" );
		assertThat( card.getTimestamp() ).isEqualTo( "2018-01-01 00:00:00" );

		assertThat( card.getName() ).isEqualTo( "Zevra" );
		assertThat( card.getIcons() ).contains( "library" );
		assertThat( card.getProvider() ).isEqualTo( "Avereon" );
		assertThat( card.getInception() ).isEqualTo( 2018 );

		assertThat( card.getSummary() ).isEqualTo( "Utility library" );
		assertThat( card.getDescription() ).isEqualTo( "A utility library for Avereon applications." );
		assertThat( card.getCopyrightSummary() ).isEqualTo( "All rights reserved." );
		assertThat( card.getLicenseSummary() ).isEqualTo( "Zevra comes with ABSOLUTELY NO WARRANTY. This is open software, and you are welcome to redistribute it under certain conditions." );
	}

	@Test
	void testJsonCard() {
		ProductCard card = ProductCard.card( getClass() );
		assertThat( card.getProductKey() ).isEqualTo( "com.avereon.zevra" );

		assertThat( card.getGroup() ).isEqualTo( "com.avereon" );
		assertThat( card.getArtifact() ).isEqualTo( "zevra" );
		assertThat( card.getVersion() ).isEqualTo( "0.0-SNAPSHOT" );
		assertThat( card.getTimestamp() ).isEqualTo( "2018-01-01 00:00:00" );

		assertThat( card.getPackaging() ).isEqualTo( "lib" );
		assertThat( card.getPackagingVersion() ).isEqualTo( "2.7" );

		assertThat( card.getName() ).isEqualTo( "Zevra" );
		assertThat( card.getIcons()).contains( "library", "https://avereon.com/download/latest/zevra/product/icon"  );
		assertThat( card.getProvider() ).isEqualTo( "Avereon" );
		assertThat( card.getProviderUrl() ).isEqualTo( "https://www.avereon.com" );
		assertThat( card.getInception() ).isEqualTo( 2018 );

		assertThat( card.getSummary() ).isEqualTo( "Utility library" );
		assertThat( card.getDescription() ).isEqualTo( "A utility library for Avereon applications." );
		assertThat( card.getCopyrightSummary() ).isEqualTo( "All rights reserved." );
		assertThat( card.getLicenseSummary()).isEqualTo( "Zevra comes with ABSOLUTELY NO WARRANTY. This is open software, and you are welcome to redistribute it under certain conditions." );

		assertThat( card.getProductUri() ).isNull();
		assertThat( card.getJavaVersion() ).isEqualTo( "11"  );

		List<Maintainer> maintainers = card.getMaintainers();
		assertThat( maintainers.get( 0 ).getName() ).isEqualTo( "Sole Maintainer" );
		assertThat( maintainers.get( 0 ).getEmail() ).isEqualTo( "sole.maintainer@example.com" );
		assertThat( maintainers.get( 0 ).getOrganization() ).isEqualTo( "Example" );
		assertThat( maintainers.get( 0 ).getOrganizationUrl() ).isEqualTo( "https://www.example.com" );
		assertThat( maintainers.get( 0 ).getTimezone() ).isNull();
		assertThat( maintainers.get( 0 ).getRoles().get( 0 ) ).isEqualTo( "Maintainer" );
		assertThat( maintainers.get( 0 ).getRoles().get( 1 ) ).isEqualTo( "Developer" );
		assertThat( maintainers.get( 0 ).getRoles().size() ).isEqualTo( 2 );
		assertThat( maintainers.size() ).isEqualTo( 1 );

		List<Contributor> contributors = card.getContributors();
		assertThat( contributors.get( 0 ).getName() ).isEqualTo( "John Doe" );
		assertThat( contributors.get( 0 ).getEmail() ).isNull();
		assertThat( contributors.get( 0 ).getOrganization() ).isNull();
		assertThat( contributors.get( 0 ).getOrganizationUrl() ).isNull();
		assertThat( contributors.get( 0 ).getTimezone() ).isNull();
		assertThat( contributors.get( 0 ).getRoles().get( 0 ) ).isEqualTo( "Just John" );
		assertThat( contributors.get( 0 ).getRoles().size() ).isEqualTo( 1 );
		assertThat( contributors.get( 1 ).getName() ).isEqualTo( "Jane Doe" );
		assertThat( contributors.get( 1 ).getEmail() ).isNull();
		assertThat( contributors.get( 1 ).getOrganization() ).isNull();
		assertThat( contributors.get( 1 ).getOrganizationUrl() ).isNull();
		assertThat( contributors.get( 1 ).getTimezone() ).isNull();
		assertThat( contributors.get( 1 ).getRoles().get( 0 ) ).isEqualTo( "Just Jane" );
		assertThat( contributors.get( 1 ).getRoles().size() ).isEqualTo( 1 );
		assertThat( contributors.size() ).isEqualTo( 2 );
	}

	@Test
	void testHashCode() {
		ProductCard a = new ProductCard();
		ProductCard b = new ProductCard();

		a.setGroup( "com.example" );
		a.setArtifact( "artifact" );

		b.setGroup( "com.example" );
		b.setArtifact( "artifact" );

		assertThat( System.identityHashCode( a ) ).isNotEqualTo( System.identityHashCode( b ) );
		assertThat( a.hashCode() ).isEqualTo( b.hashCode() );
	}

	@Test
	void testEquals() {
		ProductCard a = new ProductCard();
		ProductCard b = new ProductCard();

		a.setGroup( "com.example" );
		a.setArtifact( "artifact" );

		b.setGroup( "com.example" );
		b.setArtifact( "artifact" );

		assertThat( System.identityHashCode( a ) ).isNotEqualTo( System.identityHashCode( b ) );
		assertThat( a ).isEqualTo( b );
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
		assertThat( reader.readLine() ).isEqualTo( "{" );
		assertThat( reader.readLine() ).isEqualTo( "  \"internalId\" : \"" + card.getInternalId() + "\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"group\" : \"com.avereon\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"artifact\" : \"zevra\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"version\" : \"1.0.0\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"timestamp\" : \"2018-01-01 00:00:00\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"packaging\" : \"lib\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"packagingVersion\" : \"2.7\"," );
		assertThat( reader.readLine() ).isEqualTo( "  \"icons\" : [ \"avereon\", \"https://avereon.com/download/stable/avereon/provider/icon\" ]," );
		assertThat( reader.readLine() ).isEqualTo( "  \"name\" : \"Zevra\"," );
		//assertThat( reader.readLine() ).isEqualTo( "  \"provider\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "  \"providerUrl\" : null," );
		assertThat( reader.readLine() ).isEqualTo( "  \"inception\" : 0," );
		//assertThat( reader.readLine() ).isEqualTo( "  \"summary\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "  \"description\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "  \"copyrightSummary\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "  \"licenseSummary\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "  \"javaVersion\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "  \"installFolder\" : null," );
		assertThat( reader.readLine() ).isEqualTo( "  \"maintainers\" : [ {" );
		assertThat( reader.readLine() ).isEqualTo( "    \"name\" : \"Sole Maintainer\"," );
		assertThat( reader.readLine() ).isEqualTo( "    \"email\" : \"sole.maintainer@example.com\"," );
		//assertThat( reader.readLine() ).isEqualTo( "    \"timezone\" : null," );
		assertThat( reader.readLine() ).isEqualTo( "    \"organization\" : \"Example\"," );
		assertThat( reader.readLine() ).isEqualTo( "    \"organizationUrl\" : \"https://www.example.com\"," );
		assertThat( reader.readLine() ).isEqualTo( "    \"roles\" : [ \"Maintainer\", \"Developer\", \"Tester\" ]" );
		assertThat( reader.readLine() ).isEqualTo( "  } ]," );
		assertThat( reader.readLine() ).isEqualTo( "  \"contributors\" : [ {" );
		assertThat( reader.readLine() ).isEqualTo( "    \"name\" : \"Contributor One\"," );
		//assertThat( reader.readLine() ).isEqualTo( "    \"email\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "    \"timezone\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "    \"organization\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "    \"organizationUrl\" : null," );
		assertThat( reader.readLine() ).isEqualTo( "    \"roles\" : [ \"Consultant\", \"Beta User\" ]" );
		assertThat( reader.readLine() ).isEqualTo( "  }, {" );
		assertThat( reader.readLine() ).isEqualTo( "    \"name\" : \"Contributor Two\"," );
		//assertThat( reader.readLine() ).isEqualTo( "    \"email\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "    \"timezone\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "    \"organization\" : null," );
		//assertThat( reader.readLine() ).isEqualTo( "    \"organizationUrl\" : null," );
		assertThat( reader.readLine() ).isEqualTo( "    \"roles\" : [ \"Philosopher\" ]" );
		assertThat( reader.readLine() ).isEqualTo( "  } ]," );
		assertThat( reader.readLine() ).isEqualTo( "  \"enabled\" : false," );
		assertThat( reader.readLine() ).isEqualTo( "  \"removable\" : false" );
		assertThat( reader.readLine() ).isEqualTo( "}" );
		assertThat( reader.readLine() ).isNull();
	}

	@Test
	void testIgnoreMissingAndUnknownProperties() throws Exception {
		String state = "{\"name\" : \"Zevra\", \"extra\" : \"unknown\"}";
		ProductCard card = new ProductCard().fromJson( new ByteArrayInputStream( state.getBytes( StandardCharsets.UTF_8 ) ), null );
		assertThat( card.getName() ).isEqualTo( "Zevra" );
	}

}
