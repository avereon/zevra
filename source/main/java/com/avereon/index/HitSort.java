package com.avereon.index;

import java.util.Comparator;

class HitSort implements Comparator<Hit> {

	@Override
	public int compare( Hit hit1, Hit hit2 ) {

		return Comparator
			.comparing( ( o ) -> ((Hit)o).getDocument().title() ).reversed()
			.thenComparingInt( o -> ((Hit)o).getPriority() )
			.thenComparingInt( o -> ((Hit)o).getPoints() ).reversed()
			.compare( hit1, hit2 );
	}

}
