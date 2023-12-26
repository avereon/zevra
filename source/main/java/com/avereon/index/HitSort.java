package com.avereon.index;

import java.util.Comparator;

class HitSort implements Comparator<Hit> {

	@Override
	public int compare( Hit hit1, Hit hit2 ) {
		return Comparator.comparing( Hit::getPriority ).thenComparing( Hit::getPoints ).reversed().compare( hit1, hit2 );
	}

}
