package com.xeomar.product;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ProductCardComparatorTest {

	@Test
	public void testCompareName() {
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
	public void testCompareArtifact() {
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

}
