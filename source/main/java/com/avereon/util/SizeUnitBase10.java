package com.avereon.util;

import lombok.Getter;

/**
 * Enumeration of size units based on powers of 10.
 */
@Getter
public enum SizeUnitBase10 {

	B( 1, "B" ),
	KB( 1000, "K" ),
	MB( KB.getSize() * 1000, "M" ),
	GB( MB.getSize() * 1000, "G" ),
	TB( GB.getSize() * 1000, "T" ),
	PB( TB.getSize() * 1000, "P" ),
	EB( PB.getSize() * 1000, "E" );

	final long size;

	final String compact;

	SizeUnitBase10( long size, String compact ) {
		this.size = size;
		this.compact = compact;
	}

}
