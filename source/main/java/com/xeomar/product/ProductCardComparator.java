package com.xeomar.product;

import java.util.Comparator;

public class ProductCardComparator implements Comparator<ProductCard> {

	public enum Field {
		KEY, NAME, GROUP, ARTIFACT, RELEASE
	}

	private Product product;

	private Field field;

	public ProductCardComparator( Product product, Field field ) {
		this.product = product;
		this.field = field;
	}

	@Override
	public int compare( ProductCard card1, ProductCard card2 ) {
		if( card1.equals( product.getCard() ) ) return -1;
		if( card2.equals( product.getCard() ) ) return 1;

		switch( field ) {
			case KEY: {
				return card1.getProductKey().compareTo( card2.getProductKey() );
			}
			case RELEASE: {
				return card1.getRelease().compareTo( card2.getRelease() );
			}
			case ARTIFACT: {
				return card1.getArtifact().compareTo( card2.getArtifact() );
			}
			case GROUP: {
				return card1.getGroup().compareTo( card2.getGroup() );
			}
			case NAME: {
				return card1.getName().compareTo( card2.getName() );
			}
			default: {
				return card1.getName().compareTo( card2.getName() );
			}
		}
	}

}
