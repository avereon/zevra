package com.xeomar.util;

public enum SizeUnit {

	B(1),
	KB( 1000 ),
	MB( KB.getSize()*1000),
	GB( MB.getSize()*1000),
	TB( GB.getSize()*1000),
	PB( TB.getSize()*1000),
	EB( PB.getSize()*1000),

	KiB( 1024 ),
	MiB( KiB.getSize()*1024),
	GiB( MiB.getSize()*1024),
	TiB( GiB.getSize()*1024),
	PiB( TiB.getSize()*1024),
	EiB( PiB.getSize()*1024);

	long size;

	SizeUnit( long size ) {
		this.size = size;
	}

	public long getSize() {
		return size;
	}

}
