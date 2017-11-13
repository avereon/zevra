package com.xeomar.util;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {

	/**
	 * Get a human readable string using orders of magnitude in base-10.
	 *
	 * @param size The size to convert to human readable
	 * @return The human readable size string
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
	 * @param size The size to convert to human readable
	 * @return The human readable size string
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
		return path == null ? null : getExtension( path.getFileName().toString() );
	}

	public static String getExtension( String name ) {
		if( name == null ) return null;
		int index = name.lastIndexOf( '.' );
		if( index < 0 ) return "";
		return name.substring( index + 1 );
	}

	public static Path removeExtension( Path path ) {
		return path == null ? null : Paths.get( removeExtension( path.toString() ) );
	}

	public static String removeExtension( String name ) {
		if( name == null ) return null;
		int index = name.lastIndexOf( '.' );
		if( index < 0 ) return name;
		return name.substring( 0, index );
	}

	public static void save( String data, Path target ) throws IOException {
		save( data, target, "UTF-8" );
	}

	public static void save( String data, Path target, String encoding ) throws IOException {
		try( OutputStream output = new FileOutputStream( target.toFile() ) ) {
			IOUtils.write( data, output, encoding );
		}
	}

	public static String load( Path source ) throws IOException {
		return load( source, "UTF-8" );
	}

	public static String load( Path source, String encoding ) throws IOException {
		try( FileInputStream input = new FileInputStream( source.toString() ) ) {
			return IOUtils.toString( input, encoding );
		}
	}

	public static void zip( Path source, Path target ) throws IOException {
		try( ZipOutputStream zip = new ZipOutputStream( new FileOutputStream( target.toFile() ) ) ) {
			for( Path sourcePath : Files.walk( source ).collect( Collectors.toList() ) ) {
				boolean folder = Files.isDirectory( sourcePath );
				String zipEntryPath = source.relativize( sourcePath ).toString();

				if( folder ) {
					// Folders need to have a trailing slash
					zip.putNextEntry( new ZipEntry( zipEntryPath +"/") );
				} else {
					zip.putNextEntry( new ZipEntry( zipEntryPath ) );
					try( FileInputStream input = new FileInputStream( sourcePath.toFile() ) ) {
						IOUtils.copy( input, zip );
					}
				}
				zip.closeEntry();
			}
		}
	}

	public static void unzip( Path source, Path target ) throws IOException {
		Files.createDirectories( target );

		ZipEntry entry;
		try( ZipInputStream zip = new ZipInputStream( new FileInputStream( source.toFile() ) ) ) {
			while( (entry = zip.getNextEntry()) != null ) {
				String path = entry.getName();
				Path file = target.resolve( path );

				if( path.endsWith( "/" ) ) {
					Files.createDirectories( file );
				} else {
					try( FileOutputStream output = new FileOutputStream( file.toFile() ) ) {
						Files.createDirectories( file.getParent() );
						IOUtils.copy( zip, output );
					}
				}
			}
		}
	}

	public static List<Path> listPaths( Path file ) throws IOException {
		return Files.walk( file ).collect( Collectors.toList() );
	}

	public static boolean delete( Path path ) throws IOException {
		if( !Files.exists( path ) ) return true;

		Files.walk( path ).sorted( Comparator.reverseOrder() ).forEach( file -> {
			try {
				Files.delete( file );
			} catch( IOException exception ) {
				// Intentionally ignore exception
			}
		} );

		return !Files.exists( path );
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
