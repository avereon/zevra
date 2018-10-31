package com.xeomar.util;

public enum SizeUnitBase2 {

	B( 1, "B" ),
	KiB( 1024, "K" ),
	MiB( KiB.getSize() * 1024, "M" ),
	GiB( MiB.getSize() * 1024, "G" ),
	TiB( GiB.getSize() * 1024, "T" ),
	PiB( TiB.getSize() * 1024, "P" ),
	EiB( PiB.getSize() * 1024, "E" );

	long size;

	String compact;

	SizeUnitBase2( long size, String compact ) {
		this.size = size;
		this.compact = compact;
	}

	public long getSize() {
		return size;
	}

	public String getCompact() {
		return compact;
	}

}
