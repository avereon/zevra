package com.xeomar.util;

public enum SizeUnitBase10 {

	B( 1, "B" ),
	KB( 1000, "K" ),
	MB( KB.getSize() * 1000, "M" ),
	GB( MB.getSize() * 1000, "G" ),
	TB( GB.getSize() * 1000, "T" ),
	PB( TB.getSize() * 1000, "P" ),
	EB( PB.getSize() * 1000, "E" );

	long size;

	String compact;

	SizeUnitBase10( long size, String compact ) {
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
