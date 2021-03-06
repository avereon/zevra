package com.avereon.product;

import java.util.Comparator;

public class ProductCardComparator implements Comparator<ProductCard> {

	public enum Field {
		KEY,
		NAME,
		GROUP,
		ARTIFACT,
		RELEASE,
		VERSION
	}

	private final Field field;

	public ProductCardComparator( Field field ) {
		this.field = field;
	}

	@Override
	public int compare( ProductCard card1, ProductCard card2 ) {
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
			case VERSION: {
				return card1.getVersion().compareTo( card2.getVersion() );
			}
			default: {
				return card1.getName().compareTo( card2.getName() );
			}
		}
	}

}
