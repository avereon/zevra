package com.xeomar.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {

	public static final Path TEMP_FOLDER = Paths.get( System.getProperty( "java.io.tmpdir" ) );

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	/**
	 * Get a human readable string using orders of magnitude in base-10.
	 *
	 * @param size The size to convert to human readable
	 * @return The human readable size string
	 */
	public static String getHumanSize( long size ) {
		return getHumanSize( size, false );
	}

	/**
	 * Get a human readable string using orders of magnitude in base-10.
	 *
	 * @param size The size to convert to human readable
	 * @param compact If the unit should use compact notation
	 * @return The human readable size string
	 */
	public static String getHumanSize( long size, boolean compact ) {
		return getHumanSize( size, compact, true );
	}

	/**
	 * Get a human readable string using orders of magnitude in base-10.
	 *
	 * @param size The size to convert to human readable
	 * @param compact If the unit should use compact notation
	 * @param showUnit If the unit should be shown
	 * @return The human readable size string
	 */
	public static String getHumanSize( long size, boolean compact, boolean showUnit ) {
		int exponent = 0;
		boolean negative = size < 0;
		size = Math.abs( size );
		long coefficient = size;
		long base = SizeUnitBase10.KB.getSize();
		while( coefficient >= base ) {
			coefficient /= base;
			exponent++;
		}

		SizeUnitBase10 unit = SizeUnitBase10.values()[ exponent ];
		String unitString = compact ? unit.getCompact() : unit.name();

		// Should be, at most, seven characters long: possible negative sign, four numbers, two units
		StringBuilder text = new StringBuilder( negative ? "-" : "" );
		if( exponent > 0 && coefficient < 10 ) {
			long precise = size;
			while( precise >= SizeUnitBase10.MB.getSize() ) {
				precise /= base;
			}
			text.append( String.format( "%3.1f", (float)precise / base ) );
		} else {
			text.append( String.valueOf( coefficient ) );
		}
		if( showUnit ) text.append( unitString );

		return text.toString();
	}

	/**
	 * Get a human readable string using orders of magnitude in base-2.
	 *
	 * @param size The size to convert to human readable
	 * @return The human readable size string
	 */
	public static String getHumanSizeBase2( long size ) {
		return getHumanSizeBase2( size, false );
	}

	/**
	 * Get a human readable string using orders of magnitude in base-2.
	 *
	 * @param size The size to convert to human readable
	 * @param compact If the unit should use compact notation
	 * @return The human readable size string
	 */
	public static String getHumanSizeBase2( long size, boolean compact ) {
		return getHumanSizeBase2( size, compact, true );
	}

	/**
	 * Get a human readable string using orders of magnitude in base-2.
	 *
	 * @param size The size to convert to human readable
	 * @param compact If the unit should use compact notation
	 * @param showUnit If the unit should be shown
	 * @return The human readable size string
	 */
	public static String getHumanSizeBase2( long size, boolean compact, boolean showUnit ) {
		int exponent = 0;
		boolean negative = size < 0;
		size = Math.abs( size );
		long coefficient = size;
		long base = SizeUnitBase2.KiB.getSize();
		while( coefficient >= base ) {
			coefficient /= base;
			exponent++;
		}

		SizeUnitBase2 unit = SizeUnitBase2.values()[ exponent ];
		String unitString = compact ? unit.getCompact() : unit.name();

		// Should be, at most, eight characters long: possible negative sign, four numbers, three units
		StringBuilder text = new StringBuilder( negative ? "-" : "" );
		if( exponent > 0 && coefficient < 10 ) {
			long precise = size;
			while( precise >= SizeUnitBase2.MiB.getSize() ) {
				precise /= base;
			}
			text.append( String.format( "%3.1f", (float)precise / base ) );
		} else {
			text.append( String.valueOf( coefficient ) );
		}
		if( showUnit ) text.append( unitString );

		return text.toString();
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

	public static boolean copy( Path source, Path target ) throws IOException {
		return copy( source, target, false );
	}

	public static boolean copy( Path source, Path target, boolean addRootFolder ) throws IOException {
		// Copy file sources to file targets
		if( Files.isRegularFile( source ) && Files.isRegularFile( target ) ) {
			FileUtils.copyFile( source.toFile(), target.toFile() );
			return true;
		}

		// Copy file sources to folder targets
		if( Files.isRegularFile( source ) && Files.isDirectory( target ) ) {
			FileUtils.copyFileToDirectory( source.toFile(), target.toFile() );
			return true;
		}

		// Copy folder sources to folder targets
		if( Files.isDirectory( source ) && Files.isDirectory( target ) ) {
			if( addRootFolder ) {
				FileUtils.copyDirectoryToDirectory( source.toFile(), target.toFile() );
			} else {
				FileUtils.copyDirectory( source.toFile(), target.toFile() );
			}
			return true;
		}

		// Copy file source to new file target
		if( Files.isRegularFile( source ) ) {
			FileUtils.copyFile( source.toFile(), target.toFile() );
			return true;
		}

		return false;
	}

	public static long copy( Path file, OutputStream target ) throws IOException {
		try( FileInputStream source = new FileInputStream( file.toFile() ) ) {
			return IOUtils.copy( source, target );
		}
	}

	public static boolean move( Path source, Path target ) throws IOException {
		try {
			Files.move( source, target, StandardCopyOption.ATOMIC_MOVE );
		} catch( IOException exception ) {
			if( copy( source, target ) && delete( source ) ) return true;
		}
		return false;
	}

	public static void zip( Path source, Path target ) throws IOException {
		List<Path> paths = listPaths( source );

		long total = 0;
		for( Path path : paths ) {
			total += Files.isDirectory( path ) ? 0 : Files.size( path );
		}

		try( ZipOutputStream zip = new ZipOutputStream( new FileOutputStream( target.toFile() ) ) ) {
			for( Path sourcePath : paths ) {
				boolean folder = Files.isDirectory( sourcePath );
				String zipEntryPath = source.relativize( sourcePath ).toString();

				if( folder ) {
					// Folders need to have a trailing slash
					zip.putNextEntry( new ZipEntry( zipEntryPath + "/" ) );
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
				boolean folder = path.endsWith( "/" );
				Path file = target.resolve( path );

				if( folder ) {
					Files.createDirectories( file );
				} else {
					Files.createDirectories( file.getParent() );
					try( FileOutputStream output = new FileOutputStream( file.toFile() ) ) {
						IOUtils.copy( zip, output );
					}
				}
			}
		}
	}

	public static long getRecursiveSize( Path source ) throws IOException {
		List<Path> paths = listPaths( source );

		long total = 0;
		for( Path path : paths ) {
			total += Files.isDirectory( path ) ? 0 : Files.size( path );
		}

		return total;
	}

	public static long getUncompressedZipSize( Path source ) throws IOException {
		ZipFile zipFile = new ZipFile( source.toFile() );
		Iterator<? extends ZipEntry> entries = zipFile.entries().asIterator();

		long total = 0;
		while( entries.hasNext() ) {
			ZipEntry entry = entries.next();
			total += entry.isDirectory() ? 0 : entry.getSize();
		}

		return total;
	}

	public static void zip( Path source, Path target, LongCallback progressCallback ) throws IOException {
		final AtomicLong total = new AtomicLong( 0 );
		final AtomicLong last = new AtomicLong( 0 );

		try( ZipOutputStream zip = new ZipOutputStream( new FileOutputStream( target.toFile() ) ) ) {
			for( Path sourcePath : listPaths( source ) ) {
				boolean folder = Files.isDirectory( sourcePath );
				String zipEntryPath = source.relativize( sourcePath ).toString();

				if( folder ) {
					// Folders need to have a trailing slash
					zip.putNextEntry( new ZipEntry( zipEntryPath + "/" ) );
				} else {
					zip.putNextEntry( new ZipEntry( zipEntryPath ) );
					try( FileInputStream input = new FileInputStream( sourcePath.toFile() ) ) {
						IoUtil.copy( input, zip, ( value ) -> {
							long diff = value - last.get();
							if( diff > 0 ) progressCallback.call( total.addAndGet( diff ) );
							last.set( value );
						} );
					}
				}
				zip.closeEntry();
			}
		}
	}

	public static void unzip( Path source, Path target, LongCallback progressCallback ) throws IOException {
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
						IoUtil.copy( zip, output, progressCallback );
					}
				}
			}
		}
	}

	public static List<Path> listPaths( Path file ) throws IOException {
		try( Stream<Path> paths = Files.walk( file ) ) {
			return paths.collect( Collectors.toList() );
		}
	}

	public static boolean delete( Path path ) throws IOException {
		if( !Files.exists( path ) ) return true;

		Files.walkFileTree( path, new SimpleFileVisitor<>() {

			@Override
			public FileVisitResult visitFile( Path file, BasicFileAttributes attributes ) throws IOException {
				Files.delete( file );
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory( Path folder, IOException exception ) throws IOException {
				if( exception == null ) {
					Files.delete( folder );
					return FileVisitResult.CONTINUE;
				} else {
					throw exception;
				}
			}
		} );

		return !Files.exists( path );
	}

	public static Path deleteOnExit( Path path ) throws IOException {
		if( !Files.exists( path ) ) return path;

		Files.walkFileTree( path, new SimpleFileVisitor<>() {

			@Override
			public FileVisitResult visitFile( Path file, BasicFileAttributes attributes ) {
				file.toFile().deleteOnExit();
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory( Path folder, IOException exception ) throws IOException {
				if( exception == null ) {
					folder.toFile().deleteOnExit();
					return FileVisitResult.CONTINUE;
				} else {
					throw exception;
				}
			}
		} );

		return path;
	}

	public static Path getTempFolder() {
		return Paths.get( System.getProperty( "java.io.tmpdir" ) );
	}

	public static Path createTempFile( String prefix, String suffix, FileAttribute... attributes ) throws IOException {
		return Files.createTempFile( prefix, suffix, attributes );
	}

	public static Path createTempFile( Path parent, String prefix, String suffix, FileAttribute... attributes ) throws IOException {
		return Files.createTempFile( parent, prefix, suffix, attributes );
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

	/**
	 * Create a temporary folder.
	 *
	 * @param prefix The temporary folder prefix
	 * @param suffix The temporary folder
	 * @return A temporary folder path
	 * @throws IOException If an error occurs
	 */
	public static Path createTempFolder( String prefix, String suffix ) throws IOException {
		Path path = Files.createTempFile( prefix, suffix );
		Files.deleteIfExists( path );
		if( Files.exists( path ) ) throw new IOException( "Unable to create temp folder" );
		Files.createDirectories( path );
		if( !Files.exists( path ) ) throw new IOException( "Unable to create temp folder" );
		return path;
	}

	/**
	 * Create a temporary folder.
	 *
	 * @param parent The parent folder of the temporary folder
	 * @param prefix The temporary folder prefix
	 * @param suffix The temporary folder
	 * @return A temporary folder path
	 * @throws IOException If an error occurs
	 */
	public static Path createTempFolder( Path parent, String prefix, String suffix ) throws IOException {
		Path path = Files.createTempFile( parent, prefix, suffix );
		Files.deleteIfExists( path );
		if( Files.exists( path ) ) throw new IOException( "Unable to create temp folder" );
		Files.createDirectories( path );
		if( !Files.exists( path ) ) throw new IOException( "Unable to create temp folder" );
		return path;
	}

}
