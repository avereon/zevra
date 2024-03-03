package com.avereon.util;

import lombok.Getter;

/**
 * Enumeration of size units based on powers of 2.
 */
@Getter
public enum SizeUnitBase2 {

	B( 1, "B" ),
	KiB( 1024, "K" ),
	MiB( KiB.getSize() * 1024, "M" ),
	GiB( MiB.getSize() * 1024, "G" ),
	TiB( GiB.getSize() * 1024, "T" ),
	PiB( TiB.getSize() * 1024, "P" ),
	EiB( PiB.getSize() * 1024, "E" );

	final long size;

	final String compact;

	SizeUnitBase2( long size, String compact ) {
		this.size = size;
		this.compact = compact;
	}

}
