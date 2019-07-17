package com.avereon.product;

import com.avereon.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;

public class RepoCardComparator implements Comparator<RepoCard> {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public enum Field {
		NAME,
		RANK,
		REPO
	}

	private Field field;

	public RepoCardComparator( Field field ) {
		this.field = field;
	}

	@Override
	public int compare( RepoCard card1, RepoCard card2 ) {
		switch( field ) {
			case RANK: {
				return card1.getRank() - card2.getRank();
			}
			case REPO: {
				return card1.getRepo().compareTo( card2.getRepo() );
			}
			default: {
				return card1.getName().compareTo( card2.getName() );
			}
		}
	}

}
