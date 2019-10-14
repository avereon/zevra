package com.avereon.product;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ProductCardComparatorTest {

	@Test
	void testCompareName() {
		ProductCard card1 = new ProductCard();
		card1.setName( "B" );

		ProductCard card2 = new ProductCard();
		card2.setName( "A" );

		ProductCard card3 = new ProductCard();
		card3.setName( "B" );

		assertThat( new ProductCardComparator( ProductCardComparator.Field.NAME ).compare( card1, card2 ), greaterThanOrEqualTo( 1 ) );
		assertThat( new ProductCardComparator( ProductCardComparator.Field.NAME ).compare( card2, card1 ), lessThanOrEqualTo( -1 ) );
		assertThat( new ProductCardComparator( ProductCardComparator.Field.NAME ).compare( card1, card3 ), is( 0 ) );
	}

	@Test
	void testCompareArtifact() {
		ProductCard card1 = new ProductCard();
		card1.setArtifact( "b" );

		ProductCard card2 = new ProductCard();
		card2.setArtifact( "a" );

		ProductCard card3 = new ProductCard();
		card3.setArtifact( "b" );

		assertThat( new ProductCardComparator( ProductCardComparator.Field.ARTIFACT ).compare( card1, card2 ), greaterThanOrEqualTo( 1 ) );
		assertThat( new ProductCardComparator( ProductCardComparator.Field.ARTIFACT ).compare( card2, card1 ), lessThanOrEqualTo( -1 ) );
		assertThat( new ProductCardComparator( ProductCardComparator.Field.ARTIFACT ).compare( card1, card3 ), is( 0 ) );
	}

	@Test
	void testCompareVersion() {
		ProductCard card1 = new ProductCard();
		card1.setVersion( "2" );

		ProductCard card2 = new ProductCard();
		card2.setVersion( "1" );

		ProductCard card3 = new ProductCard();
		card3.setVersion( "2" );

		assertThat( new ProductCardComparator( ProductCardComparator.Field.VERSION ).compare( card1, card2 ), greaterThanOrEqualTo( 1 ) );
		assertThat( new ProductCardComparator( ProductCardComparator.Field.VERSION ).compare( card2, card1 ), lessThanOrEqualTo( -1 ) );
		assertThat( new ProductCardComparator( ProductCardComparator.Field.VERSION ).compare( card1, card3 ), is( 0 ) );

		List<ProductCard> cards = new ArrayList<>();
		cards.add( new ProductCard().setVersion( "1" ) );
		cards.add( new ProductCard().setVersion( "3" ) );
		cards.add( new ProductCard().setVersion( "2" ) );

		cards.sort( new ProductCardComparator( ProductCardComparator.Field.VERSION ) );
		assertThat( cards.get( 0 ).getVersion(), is( "1" ) );

		cards.sort( new ProductCardComparator( ProductCardComparator.Field.VERSION ).reversed() );
		assertThat( cards.get( 0 ).getVersion(), is( "3" ) );
	}

	@Test
	void testCompareRelease() {
		ProductCard card1 = new ProductCard();
		card1.setVersion( "1" ).setTimestamp( "2018-02-14 00:00:00" );

		ProductCard card2 = new ProductCard();
		card2.setVersion( "1" ).setTimestamp( "2018-02-13 00:00:00" );

		ProductCard card3 = new ProductCard();
		card3.setVersion( "1" ).setTimestamp( "2018-02-14 00:00:00" );

		assertThat( new ProductCardComparator( ProductCardComparator.Field.RELEASE ).compare( card1, card2 ), greaterThanOrEqualTo( 1 ) );
		assertThat( new ProductCardComparator( ProductCardComparator.Field.RELEASE ).compare( card2, card1 ), lessThanOrEqualTo( -1 ) );
		assertThat( new ProductCardComparator( ProductCardComparator.Field.RELEASE ).compare( card1, card3 ), is( 0 ) );

		List<ProductCard> cards = new ArrayList<>();
		cards.add( new ProductCard().setVersion( "0.8" ).setTimestamp( "2018-04-17 00:17:05" ) );
		cards.add( new ProductCard().setVersion( "0.9-SNAPSHOT" ).setTimestamp( "2018-06-27 23:47:46" ) );

		cards.sort( new ProductCardComparator( ProductCardComparator.Field.RELEASE ) );
		assertThat( cards.get( 0 ).getVersion(), is( "0.8" ) );

		cards.sort( new ProductCardComparator( ProductCardComparator.Field.RELEASE ).reversed() );
		assertThat( cards.get( 0 ).getVersion(), is( "0.9-SNAPSHOT" ) );
	}

}
