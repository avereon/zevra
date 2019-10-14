package com.avereon.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilTest {

	private static final String PREFIX = "com-avereon-unit-test-";

	@BeforeEach
	void setup() throws Exception {
		removeTempFiles();
	}

	@AfterEach
	void cleanup() throws Exception {
		removeTempFiles();
	}

	@Test
	void testGetHumanSize() {
		assertThat( FileUtil.getHumanSize( 0 ), is( "0B" ) );
		assertThat( FileUtil.getHumanSize( 1 ), is( "1B" ) );
		assertThat( FileUtil.getHumanSize( 12 ), is( "12B" ) );
		assertThat( FileUtil.getHumanSize( 123 ), is( "123B" ) );
		assertThat( FileUtil.getHumanSize( 1234 ), is( "1.2KB" ) );
		assertThat( FileUtil.getHumanSize( 12345 ), is( "12KB" ) );
		assertThat( FileUtil.getHumanSize( 123456 ), is( "123KB" ) );
		assertThat( FileUtil.getHumanSize( 1234567 ), is( "1.2MB" ) );
		assertThat( FileUtil.getHumanSize( 12345678 ), is( "12MB" ) );
		assertThat( FileUtil.getHumanSize( 123456789 ), is( "123MB" ) );

		assertThat( FileUtil.getHumanSize( -0 ), is( "0B" ) );
		assertThat( FileUtil.getHumanSize( -1 ), is( "-1B" ) );
		assertThat( FileUtil.getHumanSize( -12 ), is( "-12B" ) );
		assertThat( FileUtil.getHumanSize( -123 ), is( "-123B" ) );
		assertThat( FileUtil.getHumanSize( -1234 ), is( "-1.2KB" ) );
		assertThat( FileUtil.getHumanSize( -12345 ), is( "-12KB" ) );
		assertThat( FileUtil.getHumanSize( -123456 ), is( "-123KB" ) );
		assertThat( FileUtil.getHumanSize( -1234567 ), is( "-1.2MB" ) );
		assertThat( FileUtil.getHumanSize( -12345678 ), is( "-12MB" ) );
		assertThat( FileUtil.getHumanSize( -123456789 ), is( "-123MB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.KB.getSize() - 1 ), is( "999B" ) );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.KB.getSize() ), is( "1.0KB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.MB.getSize() - 1 ), is( "999KB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.MB.getSize() ), is( "1.0MB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.GB.getSize() - 1 ), is( "999MB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.GB.getSize() ), is( "1.0GB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.TB.getSize() - 1 ), is( "999GB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.TB.getSize() ), is( "1.0TB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.PB.getSize() - 1 ), is( "999TB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.PB.getSize() ), is( "1.0PB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.EB.getSize() - 1 ), is( "999PB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.EB.getSize() ), is( "1.0EB" ) );
	}

	@Test
	void testGetHumanBinSize() {
		assertThat( FileUtil.getHumanSizeBase2( 0 ), is( "0B" ) );
		assertThat( FileUtil.getHumanSizeBase2( 1 ), is( "1B" ) );
		assertThat( FileUtil.getHumanSizeBase2( 12 ), is( "12B" ) );
		assertThat( FileUtil.getHumanSizeBase2( 123 ), is( "123B" ) );
		assertThat( FileUtil.getHumanSizeBase2( 1234 ), is( "1.2KiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( 12345 ), is( "12KiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( 123456 ), is( "120KiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( 1234567 ), is( "1.2MiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( 12345678 ), is( "11MiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( 123456789 ), is( "117MiB" ) );

		assertThat( FileUtil.getHumanSizeBase2( -0 ), is( "0B" ) );
		assertThat( FileUtil.getHumanSizeBase2( -1 ), is( "-1B" ) );
		assertThat( FileUtil.getHumanSizeBase2( -12 ), is( "-12B" ) );
		assertThat( FileUtil.getHumanSizeBase2( -123 ), is( "-123B" ) );
		assertThat( FileUtil.getHumanSizeBase2( -1234 ), is( "-1.2KiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( -12345 ), is( "-12KiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( -123456 ), is( "-120KiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( -1234567 ), is( "-1.2MiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( -12345678 ), is( "-11MiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( -123456789 ), is( "-117MiB" ) );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.KiB.getSize() - 1 ), is( "1023B" ) );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.KiB.getSize() ), is( "1.0KiB" ) );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.MiB.getSize() - 1 ), is( "1023KiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.MiB.getSize() ), is( "1.0MiB" ) );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.GiB.getSize() - 1 ), is( "1023MiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.GiB.getSize() ), is( "1.0GiB" ) );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.TiB.getSize() - 1 ), is( "1023GiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.TiB.getSize() ), is( "1.0TiB" ) );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.PiB.getSize() - 1 ), is( "1023TiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.PiB.getSize() ), is( "1.0PiB" ) );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.EiB.getSize() - 1 ), is( "1023PiB" ) );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.EiB.getSize() ), is( "1.0EiB" ) );
	}

	@Test
	void testGetExtensionWithPath() {
		assertThat( FileUtil.getExtension( (Path)null ), is( nullValue() ) );
		assertThat( FileUtil.getExtension( Paths.get( "test" ) ), is( "" ) );
		assertThat( FileUtil.getExtension( Paths.get( "test.txt" ) ), is( "txt" ) );
	}

	@Test
	void testGetExtensionWithName() {
		assertThat( FileUtil.getExtension( (String)null ), is( nullValue() ) );
		assertThat( FileUtil.getExtension( "test" ), is( "" ) );
		assertThat( FileUtil.getExtension( "test.txt" ), is( "txt" ) );
	}

	@Test
	void testRemoveExtensionWithPath() {
		assertThat( FileUtil.removeExtension( (Path)null ), is( nullValue() ) );
		assertThat( FileUtil.removeExtension( Paths.get( "test" ) ), is( Paths.get( "test" ) ) );
		assertThat( FileUtil.removeExtension( Paths.get( "test.txt" ) ), is( Paths.get( "test" ) ) );
	}

	@Test
	void testRemoveExtensionWithName() {
		assertThat( FileUtil.removeExtension( (String)null ), is( nullValue() ) );
		assertThat( FileUtil.removeExtension( "test" ), is( "test" ) );
		assertThat( FileUtil.removeExtension( "test.txt" ), is( "test" ) );
	}

	@Test
	void testSaveAndLoad() throws Exception {
		Path path = FileUtil.createTempFile( PREFIX, "Test" );
		FileUtil.save( path.toString(), path );
		assertThat( FileUtil.load( path ), is( path.toString() ) );
	}

	@Test
	void testCopyWithNonExistantFiles() throws Exception {
		assertThat( FileUtil.copy( Paths.get( "/nonexistant" ), Paths.get( "/nonexistant" ) ), is( false ) );
	}

	@Test
	void testCopyFileToNewFile() throws Exception {
		long time = System.currentTimeMillis();
		Path source = FileUtil.createTempFile( PREFIX, "copyFileToFileSource" );
		Path target = source.getParent().resolve( "copyFileToNewFileTarget" );

		assertFileCopy( time, source, target );
	}

	@Test
	void testCopyFileToExistingFile() throws Exception {
		long time = System.currentTimeMillis();
		Path source = FileUtil.createTempFile( PREFIX, "copyFileToFileSource" );
		Path target = FileUtil.createTempFile( PREFIX, "copyFileToFileTarget" );

		assertFileCopy( time, source, target );
	}

	@Test
	void testCopyFileToFolder() throws Exception {
		long time = System.currentTimeMillis();
		Path source = FileUtil.createTempFile( PREFIX, "copyFileToFolderSource" );
		Path target = FileUtil.createTempFolder( PREFIX, "copyFileToFolderTarget" );
		try {
			FileOutputStream fileOutput = new FileOutputStream( source.toFile() );
			try( DataOutputStream output = new DataOutputStream( fileOutput ) ) {
				output.writeLong( time );
			}

			assertThat( FileUtil.copy( source, target ), is( true ) );

			Path child = target.resolve( source.getFileName() );
			FileInputStream fileInput = new FileInputStream( child.toFile() );
			try( DataInputStream input = new DataInputStream( fileInput ) ) {
				assertThat( input.readLong(), is( time ) );
			}
		} finally {
			FileUtil.deleteOnExit( source );
			FileUtil.deleteOnExit( target );
		}
	}

	@Test
	void testCopyFolderToFile() throws Exception {
		Path source = FileUtil.createTempFolder( PREFIX, "copyFolderToFileSource" );
		Path target = FileUtil.createTempFile( PREFIX, "copyFolderToFileTarget" );
		try {
			assertThat( FileUtil.copy( source, target ), is( false ) );
			assertThat( Files.exists( source ), is( true ) );
			assertThat( Files.exists( target ), is( true ) );
		} finally {
			FileUtil.deleteOnExit( source );
			FileUtil.deleteOnExit( target );
		}
	}

	@Test
	void testCopyFolderToFolder() throws Exception {
		Path source0 = FileUtil.createTempFolder( PREFIX, "copyFolderToFolderSource0" );
		Path source1 = FileUtil.createTempFolder( source0, PREFIX, "copyFolderToFolderSource1" );
		Path target0 = FileUtil.createTempFolder( PREFIX, "copyFolderToFolderTarget0" );
		Path target1 = target0.resolve( source1.getFileName() );
		try {
			Path leaf0 = FileUtil.createTempFile( source0, PREFIX, "copyFolderToFolderLeaf0" );
			Path leaf1 = FileUtil.createTempFile( source0, PREFIX, "copyFolderToFolderLeaf1" );
			Path leaf2 = FileUtil.createTempFile( source1, PREFIX, "copyFolderToFolderLeaf2" );
			Path leaf3 = FileUtil.createTempFile( source1, PREFIX, "copyFolderToFolderLeaf3" );

			try( Stream list = Files.list( source0 ) ) {
				assertThat( list.count(), is( 3L ) );
			}
			try( Stream list = Files.list( source1 ) ) {
				assertThat( list.count(), is( 2L ) );
			}

			assertThat( FileUtil.copy( source0, target0, false ), is( true ) );

			try( Stream list = Files.list( target0 ) ) {
				assertThat( list.count(), is( 3L ) );
			}
			try( Stream list = Files.list( target1 ) ) {
				assertThat( list.count(), is( 2L ) );
			}

			assertThat( Files.exists( target0.resolve( leaf0.getFileName() ) ), is( true ) );
			assertThat( Files.exists( target0.resolve( leaf1.getFileName() ) ), is( true ) );
			assertThat( Files.exists( target1.resolve( leaf2.getFileName() ) ), is( true ) );
			assertThat( Files.exists( target1.resolve( leaf3.getFileName() ) ), is( true ) );
		} finally {
			FileUtil.deleteOnExit( source0 );
			FileUtil.deleteOnExit( source1 );
			FileUtil.deleteOnExit( target0 );
			FileUtil.deleteOnExit( target1 );
		}
	}

	@Test
	void testCopyFolderToFolderWithSourceFolder() throws Exception {
		Path source0 = FileUtil.createTempFolder( PREFIX, "copyFolderToFolderParent0" );
		Path source1 = FileUtil.createTempFolder( source0, PREFIX, "copyFolderToFolderParent1" );
		Path target = FileUtil.createTempFolder( PREFIX, "copyFolderToFolderTarget" );
		Path target0 = target.resolve( source0.getFileName() );
		Path target1 = target0.resolve( source1.getFileName() );
		try {
			Path leaf0 = Files.createTempFile( source0, PREFIX, "copyFolderToFolderLeaf0" );
			Path leaf1 = Files.createTempFile( source0, PREFIX, "copyFolderToFolderLeaf1" );
			Path leaf2 = Files.createTempFile( source1, PREFIX, "copyFolderToFolderLeaf2" );
			Path leaf3 = Files.createTempFile( source1, PREFIX, "copyFolderToFolderLeaf3" );

			try( Stream list = Files.list( source0 ) ) {
				assertThat( list.count(), is( 3L ) );
			}
			try( Stream list = Files.list( source1 ) ) {
				assertThat( list.count(), is( 2L ) );
			}
			assertThat( FileUtil.copy( source0, target, true ), is( true ) );

			try( Stream list = Files.list( target0 ) ) {
				assertThat( list.count(), is( 3L ) );
			}
			try( Stream list = Files.list( target1 ) ) {
				assertThat( list.count(), is( 2L ) );
			}

			assertThat( Files.exists( target0.resolve( leaf0.getFileName() ) ), is( true ) );
			assertThat( Files.exists( target0.resolve( leaf1.getFileName() ) ), is( true ) );
			assertThat( Files.exists( target1.resolve( leaf2.getFileName() ) ), is( true ) );
			assertThat( Files.exists( target1.resolve( leaf3.getFileName() ) ), is( true ) );
		} finally {
			FileUtil.deleteOnExit( source0 );
			FileUtil.deleteOnExit( source0 );
			FileUtil.deleteOnExit( target0 );
			FileUtil.deleteOnExit( target1 );
		}
	}

	@Test
	void testCopyFileToOutputStream() throws Exception {
		long time = System.currentTimeMillis();
		Path source = FileUtil.createTempFile( PREFIX, "copyFileToFileSource" );
		ByteArrayOutputStream target = new ByteArrayOutputStream();
		FileOutputStream fileOutput = new FileOutputStream( source.toFile() );
		try {
			try( DataOutputStream output = new DataOutputStream( fileOutput ) ) {
				output.writeLong( time );
			}

			assertThat( FileUtil.copy( source, target ), is( 8L ) );

			try( DataInputStream input = new DataInputStream( new ByteArrayInputStream( target.toByteArray() ) ) ) {
				assertThat( input.readLong(), is( time ) );
			}
		} finally {
			FileUtil.deleteOnExit( source );
		}
	}

	@Test
	void testZipAndUnzip() throws Exception {
		Path sourceData = Paths.get( "source/test/java" );
		Path zip = Paths.get( "target/test.source.zip" );
		Path targetData = Paths.get( "target/test/data" );

		// Make a list of relativized paths
		List<String> paths = Files.walk( sourceData ).map( file -> {
			String entryPath = sourceData.relativize( file ).toString();
			if( Files.isDirectory( file ) ) entryPath += "/";
			return entryPath;
		} ).collect( Collectors.toList() );

		// Initialize for zip tests
		Files.deleteIfExists( zip );
		assertThat( Files.exists( zip ), is( false ) );

		// Zip the data
		FileUtil.zip( sourceData, zip );
		assertThat( Files.exists( zip ), is( true ) );

		try( ZipFile zipFile = new ZipFile( zip.toFile() ) ) {
			// Check that all paths are in the zip file
			zipFile.stream().forEach( entry -> assertThat( paths.contains( entry.getName() ), is( true ) ) );

			// Initialize for unzip tests
			assertThat( FileUtil.delete( targetData ), is( true ) );
			assertThat( Files.exists( targetData ), is( false ) );
			Files.createDirectories( targetData );
			assertThat( Files.exists( targetData ), is( true ) );

			// Unzip the data
			FileUtil.unzip( zip, targetData );

			// Check that all the zip entries are in the target
			zipFile.stream().forEach( entry -> assertThat( Files.exists( targetData.resolve( entry.getName() ) ), is( true ) ) );

			// Check that the target files match the source files
			Files.walk( sourceData ).forEach( path -> assertThat( Files.exists( targetData.resolve( sourceData.relativize( path ) ) ), is( true ) ) );
		} finally {
			if( Files.exists( targetData ) ) FileUtil.delete( targetData );
			assertThat( Files.exists( targetData ), is( false ) );
		}
	}

	@Test
	void testListPaths() throws Exception {
		Path sourceRoot = FileUtil.createTempFolder( getClass().getSimpleName() );
		try {
			Path sourceFile1 = FileUtil.createTempFile( sourceRoot, getClass().getSimpleName(), "" );
			Path sourceFile2 = FileUtil.createTempFile( sourceRoot, getClass().getSimpleName(), "" );
			Path sourceSubFolder = FileUtil.createTempFolder( sourceRoot, getClass().getSimpleName() );
			Path sourceFile3 = FileUtil.createTempFile( sourceSubFolder, getClass().getSimpleName(), "" );
			Path sourceFile4 = FileUtil.createTempFile( sourceSubFolder, getClass().getSimpleName(), "" );

			List<Path> paths = FileUtil.listPaths( sourceRoot );

			for( Path expected : Arrays.asList( sourceRoot, sourceFile1, sourceFile2, sourceSubFolder, sourceFile3, sourceFile4 ) ) {
				assertThat( paths.contains( expected ), is( true ) );
			}
			assertThat( paths.size(), is( 6 ) );
		} finally {
			FileUtil.delete( sourceRoot );
		}
	}

	@Test
	void testCreateTempFolder() throws Exception {
		Path folder = FileUtil.createTempFolder( PREFIX );
		try {
			assertThat( Files.exists( folder ), is( true ) );
			String name = folder.getFileName().toString();
			Path check = Paths.get( System.getProperty( "java.io.tmpdir" ), name );
			assertThat( Files.exists( check ), is( true ) );
			assertThat( folder, is( check ) );
		} finally {
			FileUtil.delete( folder );
		}
	}

	@Test
	void testFindValidParent() {
		Path path = FileUtil.getTempFolder().resolve( "non/existant/path" );
		assertFalse( Files.exists( path ) );

		Path valid = FileUtil.findValidParent( path );
		assertTrue( Files.exists( valid ) );
		assertTrue( Files.isDirectory( valid ) );
	}

	private void assertFileCopy( long time, Path source, Path target ) throws IOException {
		try {
			FileOutputStream fileOutput = new FileOutputStream( source.toFile() );
			try( DataOutputStream output = new DataOutputStream( fileOutput ) ) {
				output.writeLong( time );
			}

			assertThat( FileUtil.copy( source, target ), is( true ) );

			FileInputStream fileInput = new FileInputStream( target.toFile() );
			try( DataInputStream input = new DataInputStream( fileInput ) ) {
				assertThat( input.readLong(), is( time ) );
			}
		} finally {
			FileUtil.deleteOnExit( source );
			FileUtil.deleteOnExit( target );
		}
	}

	private void removeTempFiles() throws Exception {
		Path tmp = Paths.get( System.getProperty( "java.io.tmpdir" ) );
		try( DirectoryStream<Path> stream = Files.newDirectoryStream( tmp, PREFIX + "*" ) ) {
			for( Path path : stream ) {
				FileUtil.delete( path );
			}
		}
	}

}
