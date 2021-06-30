package com.avereon.util;

import lombok.extern.flogger.Flogger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Flogger
public class FileUtil {

	public static final Path TEMP_FOLDER = Paths.get( System.getProperty( "java.io.tmpdir" ) );

	private static final long FILE_COPY_BUFFER_SIZE = SizeUnitBase2.MiB.getSize() * 25;

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
			text.append( coefficient );
		}
		if( showUnit ) {
			if( !compact ) text.append( " " );
			text.append( unitString );
		}

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
			text.append( coefficient );
		}
		if( showUnit ) {
			if( !compact ) text.append( " " );
			text.append( unitString );
		}

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
			IoUtil.write( data, output, encoding );
		}
	}

	public static String load( Path source ) throws IOException {
		return load( source, "UTF-8" );
	}

	public static String load( Path source, String encoding ) throws IOException {
		try( FileInputStream input = new FileInputStream( source.toString() ) ) {
			return IoUtil.toString( input, encoding );
		}
	}

	public static boolean copy( Path source, Path target ) throws IOException {
		return copy( source, target, false );
	}

	public static boolean copy( Path source, Path target, LongConsumer progressCallback ) throws IOException {
		return copy( source, target, false, progressCallback );
	}

	public static boolean copy( Path source, Path target, boolean includeRootFolder ) throws IOException {
		return copy( source, target, includeRootFolder, null );
	}

	public static boolean copy( Path source, Path target, boolean includeRootFolder, LongConsumer progressCallback ) throws IOException {
		return copy( source.toFile(), target.toFile(), includeRootFolder, progressCallback );
	}

	public static long copy( Path file, OutputStream target ) throws IOException {
		try( FileInputStream source = new FileInputStream( file.toFile() ) ) {
			return IoUtil.copy( source, target );
		}
	}

	public static boolean copy( File source, File target, boolean includeRootFolder ) throws IOException {
		return copy( source, target, includeRootFolder, null );
	}

	public static boolean copy( File source, File target, boolean includeRootFolder, LongConsumer progressCallback ) throws IOException {
		// Copy file sources to file targets
		if( source.isFile() && target.isFile() ) {
			copyFileToFile( source, target, progressCallback );
			return true;
		}

		// Copy file sources to folder targets
		if( source.isFile() && target.isDirectory() ) {
			copyFileToDirectory( source, target, progressCallback );
			return true;
		}

		// Copy folder sources to folder targets
		if( source.isDirectory() && target.isDirectory() ) {
			if( includeRootFolder ) {
				copyDirectoryToDirectory( source, target, progressCallback );
			} else {
				copyDirectory( source, target, progressCallback );
			}
			return true;
		}

		// Copy file source to new file target
		if( source.isFile() ) {
			copyFileToFile( source, target, progressCallback );
			return true;
		}

		return false;
	}

	private static void copyFileToFile( File source, File target, LongConsumer progressCallback ) throws IOException {
		if( target.exists() && target.isDirectory() ) throw new IOException( "Destination is a directory: " + target );

		try( FileInputStream fis = new FileInputStream( source ); FileChannel input = fis.getChannel(); FileOutputStream fos = new FileOutputStream( target ); FileChannel output = fos.getChannel() ) {
			final long size = input.size();
			long count;
			long position = 0;
			while( position < size ) {
				final long remain = size - position;
				count = Math.min( remain, FILE_COPY_BUFFER_SIZE );
				final long bytesCopied = output.transferFrom( input, position, count );
				if( bytesCopied == 0 ) break;
				position += bytesCopied;
				if( progressCallback != null ) progressCallback.accept( position );
			}
		}
	}

	private static void copyFileToDirectory( File source, File target, LongConsumer progressCallback ) throws IOException {
		copyFileToFile( source, new File( target, source.getName() ), progressCallback );
	}

	private static void copyDirectoryToDirectory( File source, File target, LongConsumer progressCallback ) throws IOException {
		copyDirectory( source, new File( target, source.getName() ), progressCallback );
	}

	private static void copyDirectory( File source, File target, LongConsumer progressCallback ) throws IOException {
		doCopyDirectory( source, target, null, progressCallback );
	}

	private static void doCopyDirectory( File source, File target, FileFilter filter, LongConsumer progressCallback ) throws IOException {
		File[] sourceFiles = filter == null ? source.listFiles() : source.listFiles( filter );
		if( sourceFiles == null ) throw new IOException( "Failed to list source: " + source );

		// Make sure there is a valid target
		if( target.exists() ) {
			if( !target.isDirectory() ) throw new IOException( "Target not a folder: " + target );
		} else {
			if( !target.mkdirs() && !target.isDirectory() ) throw new IOException( "Cannot create target: " + target );
		}
		if( !target.canWrite() ) throw new IOException( "Cannot write to target: " + target );

		// Copy files
		for( File sourceFile : sourceFiles ) {
			File targetFile = new File( target, sourceFile.getName() );
			if( sourceFile.isDirectory() ) {
				doCopyDirectory( sourceFile, targetFile, filter, progressCallback );
			} else {
				copyFileToFile( sourceFile, targetFile, progressCallback );
			}
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
		zip( source, target, null );
	}

	public static void unzip( Path source, Path target ) throws IOException {
		unzip( source, target, null );
	}

	public static void zip( Path source, Path target, LongConsumer progressCallback ) throws IOException {
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
							if( progressCallback != null && diff > 0 ) progressCallback.accept( total.addAndGet( diff ) );
							last.set( value );
						} );
					}
				}
				zip.closeEntry();
			}
		}
	}

	public static void unzip( Path source, Path target, LongConsumer progressCallback ) throws IOException {
		Files.createDirectories( target );

		ZipEntry entry;
		try( ZipInputStream zip = new ZipInputStream( new FileInputStream( source.toFile() ) ) ) {
			while( (entry = zip.getNextEntry()) != null ) {
				String path = sanitize( entry.getName() );
				if( TextUtil.isEmpty( path ) ) continue;

				Path file = target.resolve( path );

				if( path.endsWith( "/" ) ) {
					Files.createDirectories( file );
				} else {
					Files.createDirectories( file.getParent() );
					try( FileOutputStream output = new FileOutputStream( file.toFile() ) ) {
						IoUtil.copy( zip, output, progressCallback );
					}
				}
			}
		}
	}

	public static long getDeepSize( Path source ) throws IOException {
		List<Path> paths = listPaths( source );

		long total = 0;
		for( Path path : paths ) {
			total += Files.isDirectory( path ) ? 0 : Files.size( path );
		}

		return total;
	}

	public static long getUncompressedSize( Path source ) throws IOException {
		ZipFile zipFile = new ZipFile( source.toFile() );
		Iterator<? extends ZipEntry> entries = zipFile.entries().asIterator();

		long total = 0;
		while( entries.hasNext() ) {
			ZipEntry entry = entries.next();
			total += entry.isDirectory() ? 0 : entry.getSize();
		}

		return total;
	}

	public static long getDeepUncompressedSize( Path source ) throws IOException {
		// TODO Accumulates all files sizes and uncompressed zip file sizes
		return 0;
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
		return TEMP_FOLDER;
	}

	public static Path createTempFile( String prefix, String suffix, FileAttribute<?>... attributes ) throws IOException {
		return Files.createTempFile( prefix, suffix, attributes );
	}

	public static Path createTempFile( Path parent, String prefix, String suffix, FileAttribute<?>... attributes ) throws IOException {
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

	/**
	 * Find the closest existing parent folder for the given file.
	 *
	 * @param file The file for which to find a valid parent folder
	 */
	public static Path findValidParent( String file ) {
		return findValidParent( Paths.get( file ) );
	}

	/**
	 * Find the closest existing parent folder for the given file.
	 *
	 * @param file The file for which to find a valid parent folder
	 */
	public static Path findValidParent( File file ) {
		return findValidParent( file.toPath() );
	}

	/**
	 * Find the closest existing parent folder for the given path.
	 *
	 * @param path The path for which to find a valid parent
	 */
	public static Path findValidParent( Path path ) {
		return findValidFolder( path.getParent() );
	}

	/**
	 * Find the closest existing folder for the given path.
	 *
	 * @param path The path for which to find a valid parent
	 */
	public static Path findValidFolder( String path ) {
		return findValidFolder( Paths.get( path ) );
	}

	/**
	 * Find the closest existing folder for the given file.
	 *
	 * @param file The file for which to find a valid parent
	 */
	public static Path findValidFolder( File file ) {
		return findValidFolder( file.toPath() );
	}

	/**
	 * Find the closest existing folder for the given path.
	 *
	 * @param path The path for which to find a valid parent
	 */
	public static Path findValidFolder( Path path ) {
		while( Files.notExists( path ) || !Files.isDirectory( path ) ) {
			path = path.getParent();
		}
		return path;
	}

	private static String sanitize( String path ) {
		boolean found;
		do {
			found = false;
			if( path.startsWith( "/" ) ) {
				path = path.substring( 1 );
				found = true;
			} else if( path.startsWith( "../" ) ) {
				path = path.substring( 3 );
				found = true;
			}
		} while( found );
		return path;
	}

}
