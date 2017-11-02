package com.xeomar.razor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

	/**
	 * Get a human readable string using orders of magnitude in base-10.
	 *
	 * @param size
	 * @return
	 */
	public static String getHumanSize( long size ) {
		int exponent = 0;
		long coefficient = size;
		long base = SizeUnit.KB.getSize();
		while( coefficient >= base ) {
			coefficient /= base;
			exponent++;
		}

		SizeUnit unit = SizeUnit.B;
		switch( exponent ) {
			case 1: {
				unit = SizeUnit.KB;
				break;
			}
			case 2: {
				unit = SizeUnit.MB;
				break;
			}
			case 3: {
				unit = SizeUnit.GB;
				break;
			}
			case 4: {
				unit = SizeUnit.TB;
				break;
			}
			case 5: {
				unit = SizeUnit.PB;
				break;
			}
			case 6: {
				unit = SizeUnit.EB;
				break;
			}
		}

		// Should be, at most, five characters long; three numbers, two units.
		if( exponent > 0 && coefficient < 10 ) {
			long precise = size;
			while( precise >= SizeUnit.MB.getSize() ) {
				precise /= base;
			}
			return String.format( "%3.1f", (float)precise / base ) + unit;
		}

		return String.valueOf( coefficient ) + unit;
	}

	/**
	 * Get a human readable string using orders of magnitude in base-2.
	 *
	 * @param size
	 * @return
	 */
	public static String getHumanBinSize( long size ) {
		int exponent = 0;
		long coefficient = size;
		long base = SizeUnit.KiB.getSize();
		while( coefficient >= base ) {
			coefficient /= base;
			exponent++;
		}

		String unit = "B";
		switch( exponent ) {
			case 1: {
				unit = "KiB";
				break;
			}
			case 2: {
				unit = "MiB";
				break;
			}
			case 3: {
				unit = "GiB";
				break;
			}
			case 4: {
				unit = "TiB";
				break;
			}
			case 5: {
				unit = "PiB";
				break;
			}
			case 6: {
				unit = "EiB";
				break;
			}
		}

		// Should be, at most, seven characters long; four numbers, three units.
		if( exponent > 0 && coefficient < 10 ) {
			long precise = size;
			while( precise >= SizeUnit.MiB.getSize() ) {
				precise /= base;
			}
			return String.format( "%3.1f", (float)precise / base ) + unit;
		}

		return String.valueOf( coefficient ) + unit;
	}

	public static String getExtension( Path path ) {
		if( path == null ) return null;
		return getExtension( path.getFileName().toString() );
	}

	public static String getExtension( String name ) {
		if( name == null ) return null;
		int index = name.lastIndexOf( '.' );
		if( index < 0 ) return "";
		return name.substring( index + 1 );
	}

	/**
	 * Create a temporary folder.
	 *
	 * @param prefix The temp folder prefix
	 * @return The temp folder Path object
	 * @throws IOException If an I/O error occurs
	 */
	public static Path createTempFolder( String prefix ) throws IOException {
		return Files.createTempDirectory( prefix );
	}

	/**
	 * Create a temporary folder.
	 *
	 * @param path The temp folder path
	 * @param prefix The temp folder prefix
	 * @return The temp folder Path object
	 * @throws IOException If an I/O error occurs
	 */
	public static Path createTempFolder( Path path, String prefix ) throws IOException {
		return Files.createTempDirectory( path, prefix );
	}

}
