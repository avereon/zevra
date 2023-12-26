package com.avereon.index;

import java.util.Comparator;

class HitSort implements Comparator<Hit> {

	@Override
	public int compare( Hit hit1, Hit hit2 ) {

		return Comparator
			.comparingInt( o -> ((Hit)o).getPoints() ).reversed()
			//.thenComparingInt( o -> ((Hit)o).getPriority() )
			.thenComparing( ( o ) -> ((Hit)o).getDocument().title() )
			.compare( hit1, hit2 );
	}

}
