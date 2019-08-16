package com.avereon.product;

import java.util.Comparator;

public class RepoCardComparator implements Comparator<RepoCard> {

	public enum Field {
		NAME,
		REPO
	}

	private Field field;

	public RepoCardComparator( Field field ) {
		this.field = field;
	}

	@Override
	public int compare( RepoCard card1, RepoCard card2 ) {
		switch( field ) {
			case REPO: {
				return card1.getUrl().compareTo( card2.getUrl() );
			}
			default: {
				return card1.getName().compareTo( card2.getName() );
			}
		}
	}

}
