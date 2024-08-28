package com.avereon.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat( FileUtil.getHumanSize( 0 ) ).isEqualTo( "0 B" );
		assertThat( FileUtil.getHumanSize( 1 ) ).isEqualTo( "1 B" );
		assertThat( FileUtil.getHumanSize( 12 ) ).isEqualTo( "12 B" );
		assertThat( FileUtil.getHumanSize( 123 ) ).isEqualTo( "123 B" );
		assertThat( FileUtil.getHumanSize( 1234 ) ).isEqualTo( "1.2 KB" );
		assertThat( FileUtil.getHumanSize( 12345 ) ).isEqualTo( "12 KB" );
		assertThat( FileUtil.getHumanSize( 123456 ) ).isEqualTo( "123 KB" );
		assertThat( FileUtil.getHumanSize( 1234567 ) ).isEqualTo( "1.2 MB" );
		assertThat( FileUtil.getHumanSize( 12345678 ) ).isEqualTo( "12 MB" );
		assertThat( FileUtil.getHumanSize( 123456789 ) ).isEqualTo( "123 MB" );

		assertThat( FileUtil.getHumanSize( -0 ) ).isEqualTo( "0 B" );
		assertThat( FileUtil.getHumanSize( -1 ) ).isEqualTo( "-1 B" );
		assertThat( FileUtil.getHumanSize( -12 ) ).isEqualTo( "-12 B" );
		assertThat( FileUtil.getHumanSize( -123 ) ).isEqualTo( "-123 B" );
		assertThat( FileUtil.getHumanSize( -1234 ) ).isEqualTo( "-1.2 KB" );
		assertThat( FileUtil.getHumanSize( -12345 ) ).isEqualTo( "-12 KB" );
		assertThat( FileUtil.getHumanSize( -123456 ) ).isEqualTo( "-123 KB" );
		assertThat( FileUtil.getHumanSize( -1234567 ) ).isEqualTo( "-1.2 MB" );
		assertThat( FileUtil.getHumanSize( -12345678 ) ).isEqualTo( "-12 MB" );
		assertThat( FileUtil.getHumanSize( -123456789 ) ).isEqualTo( "-123 MB" );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.KB.getSize() - 1 ) ).isEqualTo( "999 B" );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.KB.getSize() ) ).isEqualTo( "1.0 KB" );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.MB.getSize() - 1 ) ).isEqualTo( "999 KB" );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.MB.getSize() ) ).isEqualTo( "1.0 MB" );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.GB.getSize() - 1 ) ).isEqualTo( "999 MB" );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.GB.getSize() ) ).isEqualTo( "1.0 GB" );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.TB.getSize() - 1 ) ).isEqualTo( "999 GB" );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.TB.getSize() ) ).isEqualTo( "1.0 TB" );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.PB.getSize() - 1 ) ).isEqualTo( "999 TB" );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.PB.getSize() ) ).isEqualTo( "1.0 PB" );

		assertThat( FileUtil.getHumanSize( SizeUnitBase10.EB.getSize() - 1 ) ).isEqualTo( "999 PB" );
		assertThat( FileUtil.getHumanSize( SizeUnitBase10.EB.getSize() ) ).isEqualTo( "1.0 EB" );
	}

	@Test
	void testGetHumanSizeCompact() {
		assertThat( FileUtil.getHumanSize( 0, true ) ).isEqualTo( "0B" );
		assertThat( FileUtil.getHumanSize( 1, true ) ).isEqualTo( "1B" );
		assertThat( FileUtil.getHumanSize( 12, true ) ).isEqualTo( "12B" );
		assertThat( FileUtil.getHumanSize( 123, true ) ).isEqualTo( "123B" );
		assertThat( FileUtil.getHumanSize( 1234, true ) ).isEqualTo( "1.2K" );
		assertThat( FileUtil.getHumanSize( 12345, true ) ).isEqualTo( "12K" );
		assertThat( FileUtil.getHumanSize( 123456, true ) ).isEqualTo( "123K" );
		assertThat( FileUtil.getHumanSize( 1234567, true ) ).isEqualTo( "1.2M" );
		assertThat( FileUtil.getHumanSize( 12345678, true ) ).isEqualTo( "12M" );
		assertThat( FileUtil.getHumanSize( 123456789, true ) ).isEqualTo( "123M" );
	}

	@Test
	void testGetHumanBinSize() {
		assertThat( FileUtil.getHumanSizeBase2( 0 ) ).isEqualTo( "0 B" );
		assertThat( FileUtil.getHumanSizeBase2( 1 ) ).isEqualTo( "1 B" );
		assertThat( FileUtil.getHumanSizeBase2( 12 ) ).isEqualTo( "12 B" );
		assertThat( FileUtil.getHumanSizeBase2( 123 ) ).isEqualTo( "123 B" );
		assertThat( FileUtil.getHumanSizeBase2( 1234 ) ).isEqualTo( "1.2 KiB" );
		assertThat( FileUtil.getHumanSizeBase2( 12345 ) ).isEqualTo( "12 KiB" );
		assertThat( FileUtil.getHumanSizeBase2( 123456 ) ).isEqualTo( "120 KiB" );
		assertThat( FileUtil.getHumanSizeBase2( 1234567 ) ).isEqualTo( "1.2 MiB" );
		assertThat( FileUtil.getHumanSizeBase2( 12345678 ) ).isEqualTo( "11 MiB" );
		assertThat( FileUtil.getHumanSizeBase2( 123456789 ) ).isEqualTo( "117 MiB" );

		assertThat( FileUtil.getHumanSizeBase2( -0 ) ).isEqualTo( "0 B" );
		assertThat( FileUtil.getHumanSizeBase2( -1 ) ).isEqualTo( "-1 B" );
		assertThat( FileUtil.getHumanSizeBase2( -12 ) ).isEqualTo( "-12 B" );
		assertThat( FileUtil.getHumanSizeBase2( -123 ) ).isEqualTo( "-123 B" );
		assertThat( FileUtil.getHumanSizeBase2( -1234 ) ).isEqualTo( "-1.2 KiB" );
		assertThat( FileUtil.getHumanSizeBase2( -12345 ) ).isEqualTo( "-12 KiB" );
		assertThat( FileUtil.getHumanSizeBase2( -123456 ) ).isEqualTo( "-120 KiB" );
		assertThat( FileUtil.getHumanSizeBase2( -1234567 ) ).isEqualTo( "-1.2 MiB" );
		assertThat( FileUtil.getHumanSizeBase2( -12345678 ) ).isEqualTo( "-11 MiB" );
		assertThat( FileUtil.getHumanSizeBase2( -123456789 ) ).isEqualTo( "-117 MiB" );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.KiB.getSize() - 1 ) ).isEqualTo( "1023 B" );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.KiB.getSize() ) ).isEqualTo( "1.0 KiB" );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.MiB.getSize() - 1 ) ).isEqualTo( "1023 KiB" );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.MiB.getSize() ) ).isEqualTo( "1.0 MiB" );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.GiB.getSize() - 1 ) ).isEqualTo( "1023 MiB" );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.GiB.getSize() ) ).isEqualTo( "1.0 GiB" );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.TiB.getSize() - 1 ) ).isEqualTo( "1023 GiB" );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.TiB.getSize() ) ).isEqualTo( "1.0 TiB" );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.PiB.getSize() - 1 ) ).isEqualTo( "1023 TiB" );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.PiB.getSize() ) ).isEqualTo( "1.0 PiB" );

		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.EiB.getSize() - 1 ) ).isEqualTo( "1023 PiB" );
		assertThat( FileUtil.getHumanSizeBase2( SizeUnitBase2.EiB.getSize() ) ).isEqualTo( "1.0 EiB" );
	}

	@Test
	void testGetHumanBinSizeCompact() {
		assertThat( FileUtil.getHumanSizeBase2( 0, true ) ).isEqualTo( "0B" );
		assertThat( FileUtil.getHumanSizeBase2( 1, true ) ).isEqualTo( "1B" );
		assertThat( FileUtil.getHumanSizeBase2( 12, true ) ).isEqualTo( "12B" );
		assertThat( FileUtil.getHumanSizeBase2( 123, true ) ).isEqualTo( "123B" );
		assertThat( FileUtil.getHumanSizeBase2( 1234, true ) ).isEqualTo( "1.2K" );
		assertThat( FileUtil.getHumanSizeBase2( 12345, true ) ).isEqualTo( "12K" );
		assertThat( FileUtil.getHumanSizeBase2( 123456, true ) ).isEqualTo( "120K" );
		assertThat( FileUtil.getHumanSizeBase2( 1234567, true ) ).isEqualTo( "1.2M" );
		assertThat( FileUtil.getHumanSizeBase2( 12345678, true ) ).isEqualTo( "11M" );
		assertThat( FileUtil.getHumanSizeBase2( 123456789, true ) ).isEqualTo( "117M" );
	}

	@Test
	void testGetExtensionWithPath() {
		assertThat( FileUtil.getExtension( (Path)null ) ).isNull();
		assertThat( FileUtil.getExtension( Paths.get( "test" ) ) ).isEqualTo( "" );
		assertThat( FileUtil.getExtension( Paths.get( "test.txt" ) ) ).isEqualTo( "txt" );
	}

	@Test
	void testGetExtensionWithName() {
		assertThat( FileUtil.getExtension( (String)null ) ).isNull();
		assertThat( FileUtil.getExtension( "test" ) ).isEqualTo( "" );
		assertThat( FileUtil.getExtension( "test.txt" ) ).isEqualTo( "txt" );
	}

	@Test
	void testRemoveExtensionWithPath() {
		assertThat( FileUtil.removeExtension( (Path)null ) ).isNull();
		assertThat( FileUtil.removeExtension( Paths.get( "test" ) ) ).isEqualTo( Paths.get( "test" ) );
		assertThat( FileUtil.removeExtension( Paths.get( "test.txt" ) ) ).isEqualTo( Paths.get( "test" ) );
	}

	@Test
	void testRemoveExtensionWithName() {
		assertThat( FileUtil.removeExtension( (String)null ) ).isNull();
		assertThat( FileUtil.removeExtension( "test" ) ).isEqualTo( "test" );
		assertThat( FileUtil.removeExtension( "test.txt" ) ).isEqualTo( "test" );
	}

	@Test
	void testSaveAndLoad() throws Exception {
		Path path = FileUtil.createTempFile( PREFIX, "Test" );
		FileUtil.save( path.toString(), path );
		assertThat( FileUtil.load( path ) ).isEqualTo( path.toString() );
	}

	@Test
	void testCopyWithNonExistantFiles() throws Exception {
		assertThat( FileUtil.copy( Paths.get( "/nonexistant" ), Paths.get( "/nonexistant" ) ) ).isEqualTo( false );
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

			assertThat( FileUtil.copy( source, target ) ).isEqualTo( true );

			Path child = target.resolve( source.getFileName() );
			FileInputStream fileInput = new FileInputStream( child.toFile() );
			try( DataInputStream input = new DataInputStream( fileInput ) ) {
				assertThat( input.readLong() ).isEqualTo( time );
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
			assertThat( FileUtil.copy( source, target ) ).isEqualTo( false );
			assertThat( Files.exists( source ) ).isEqualTo( true );
			assertThat( Files.exists( target ) ).isEqualTo( true );
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

			try( Stream<Path> list = Files.list( source0 ) ) {
				assertThat( list.count() ).isEqualTo( 3L );
			}
			try( Stream<Path> list = Files.list( source1 ) ) {
				assertThat( list.count() ).isEqualTo( 2L );
			}

			assertThat( FileUtil.copy( source0, target0, false ) ).isEqualTo( true );

			try( Stream<Path> list = Files.list( target0 ) ) {
				assertThat( list.count() ).isEqualTo( 3L );
			}
			try( Stream<Path> list = Files.list( target1 ) ) {
				assertThat( list.count() ).isEqualTo( 2L );
			}

			assertThat( Files.exists( target0.resolve( leaf0.getFileName() ) ) ).isEqualTo( true );
			assertThat( Files.exists( target0.resolve( leaf1.getFileName() ) ) ).isEqualTo( true );
			assertThat( Files.exists( target1.resolve( leaf2.getFileName() ) ) ).isEqualTo( true );
			assertThat( Files.exists( target1.resolve( leaf3.getFileName() ) ) ).isEqualTo( true );
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

			try( Stream<Path> list = Files.list( source0 ) ) {
				assertThat( list.count() ).isEqualTo( 3L );
			}
			try( Stream<Path> list = Files.list( source1 ) ) {
				assertThat( list.count() ).isEqualTo( 2L );
			}
			assertThat( FileUtil.copy( source0, target, true ) ).isEqualTo( true );

			try( Stream<Path> list = Files.list( target0 ) ) {
				assertThat( list.count() ).isEqualTo( 3L );
			}
			try( Stream<Path> list = Files.list( target1 ) ) {
				assertThat( list.count() ).isEqualTo( 2L );
			}

			assertThat( Files.exists( target0.resolve( leaf0.getFileName() ) ) ).isEqualTo( true );
			assertThat( Files.exists( target0.resolve( leaf1.getFileName() ) ) ).isEqualTo( true );
			assertThat( Files.exists( target1.resolve( leaf2.getFileName() ) ) ).isEqualTo( true );
			assertThat( Files.exists( target1.resolve( leaf3.getFileName() ) ) ).isEqualTo( true );
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

			assertThat( FileUtil.copy( source, target ) ).isEqualTo( 8L );

			try( DataInputStream input = new DataInputStream( new ByteArrayInputStream( target.toByteArray() ) ) ) {
				assertThat( input.readLong() ).isEqualTo( time );
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
		assertThat( Files.exists( zip ) ).isEqualTo( false );

		// Zip the data
		FileUtil.zip( sourceData, zip );
		assertThat( Files.exists( zip ) ).isEqualTo( true );

		try( ZipFile zipFile = new ZipFile( zip.toFile() ) ) {
			// Check that all paths are in the zip file
			zipFile.stream().forEach( entry -> assertThat( paths.contains( entry.getName() ) ).isEqualTo( true ) );

			// Initialize for unzip tests
			assertThat( FileUtil.delete( targetData ) ).isEqualTo( true );
			assertThat( Files.exists( targetData ) ).isEqualTo( false );
			Files.createDirectories( targetData );
			assertThat( Files.exists( targetData ) ).isEqualTo( true );

			// Unzip the data
			FileUtil.unzip( zip, targetData );

			// Check that all the zip entries are in the target
			zipFile.stream().forEach( entry -> assertThat( Files.exists( targetData.resolve( entry.getName() ) ) ).isEqualTo( true ) );

			// Check that the target files match the source files
			Files.walk( sourceData ).forEach( path -> assertThat( Files.exists( targetData.resolve( sourceData.relativize( path ) ) ) ).isEqualTo( true ) );
		} finally {
			if( Files.exists( targetData ) ) FileUtil.delete( targetData );
			assertThat( Files.exists( targetData ) ).isEqualTo( false );
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
				assertThat( paths.contains( expected ) ).isEqualTo( true );
			}
			assertThat( paths.size() ).isEqualTo( 6 );
		} finally {
			FileUtil.delete( sourceRoot );
		}
	}

	@Test
	void testCreateTempFolder() throws Exception {
		Path folder = FileUtil.createTempFolder( PREFIX );
		try {
			assertThat( Files.exists( folder ) ).isEqualTo( true );
			String name = folder.getFileName().toString();
			Path check = Paths.get( System.getProperty( "java.io.tmpdir" ), name );
			assertThat( Files.exists( check ) ).isEqualTo( true );
			assertThat( folder ).isEqualTo( check );
		} finally {
			FileUtil.delete( folder );
		}
	}

	@Test
	void testFindValidParent() {
		Path path = FileUtil.getTempFolder().resolve( "non/existant/path" );
		assertThat( Files.exists( path ) ).isFalse();

		Path valid = FileUtil.findValidParent( path );
		assertThat( Files.exists( valid ) ).isTrue();
		assertThat( Files.isDirectory( valid ) ).isTrue();
	}

	@Test
	void testFindValidParentWithNullString() {
		assertThat( FileUtil.findValidParent( (String)null ) ).isNull();
	}

	@Test
	void testFindValidParentWithNullFile() {
		assertThat( FileUtil.findValidParent( (File)null ) ).isNull();
	}

	@Test
	void testFindValidParentWithNullPath() {
		assertThat( FileUtil.findValidParent( (Path)null ) ).isNull();
	}

	@Test
	void testFindValidFolderWithNullString() {
		assertThat( FileUtil.findValidFolder( (String)null ) ).isNull();
	}

	@Test
	void testFindValidFolderWithNullFile() {
		assertThat( FileUtil.findValidFolder( (File)null ) ).isNull();
	}

	@Test
	void testFindValidFolderWithNullPath() {
		assertThat( FileUtil.findValidFolder( (Path)null ) ).isNull();
	}

	@Test
	void testFindValidFolder() {
		Path path = FileUtil.getTempFolder();
		assertThat( Files.exists( path ) ).isTrue();

		Path valid = FileUtil.findValidFolder( path );
		assertThat( Files.exists( valid ) ).isTrue();
		assertThat( Files.isDirectory( valid ) ).isTrue();
	}

	@Test
	void testFindValidFolderWithWindowsPath() {
		Path path = FileUtil.getTempFolder();
		assertThat( Files.exists( path ) ).isTrue();

		// Switch to Windows file separator for test
		String stringPath = path.toString().replace( '/', '\\' );

		Path valid = FileUtil.findValidFolder( stringPath );
		assertThat( Files.exists( valid ) ).isTrue();
		assertThat( Files.isDirectory( valid ) ).isTrue();
	}

	@Test
	void testFindValidFolderWithSpaceInPath() throws IOException {
		Path path = FileUtil.getTempFolder();
		assertThat( Files.exists( path ) ).isTrue();

		Path folderWithSpace = path.resolve( "test folder" );
		Files.createDirectories( folderWithSpace );
		assertThat( Files.exists( folderWithSpace ) ).isTrue();
		String pathString = folderWithSpace.toString();

		Path valid = FileUtil.findValidFolder( pathString );
		assertThat( Files.exists( valid ) ).isTrue();
		assertThat( Files.isDirectory( valid ) ).isTrue();
	}

	@Test
	void testDeleteWithMissingPath() throws IOException {
		Path path = Paths.get( System.getProperty( "java.io.tmpdir" ), "not-a-valid-path" );
		assertThat( Files.exists( path ) ).isFalse();
		assertThat( FileUtil.delete( path ) ).isTrue();
	}

	@ParameterizedTest
	@MethodSource( "getNextIndexedNameTestData" )
	void testGetNextIndexedName( String name, List<String> names, String expected ) {
		//		String name = "test";
		//		List<String> names = Arrays.asList( "test", "test(1)", "test(3)", "test(4)" );

		assertThat( FileUtil.getNextIndexedName( name, names ) ).isEqualTo( expected );
	}

	private static Stream<Arguments> getNextIndexedNameTestData() {
		return Stream.of(
			Arguments.of( "test", List.of(), "test" ),
			Arguments.of( "test", List.of( "test" ), "test(1)" ),
			Arguments.of( "test.txt", List.of( "test.txt", "test(1).txt" ), "test(2).txt" ),
			Arguments.of( "test", List.of( "test", "test(1)", "test(3)", "test(4)" ), "test(5)" )
		);
	}

	private void assertFileCopy( long time, Path source, Path target ) throws IOException {
		try {
			FileOutputStream fileOutput = new FileOutputStream( source.toFile() );
			try( DataOutputStream output = new DataOutputStream( fileOutput ) ) {
				output.writeLong( time );
			}

			assertThat( FileUtil.copy( source, target ) ).isEqualTo( true );

			FileInputStream fileInput = new FileInputStream( target.toFile() );
			try( DataInputStream input = new DataInputStream( fileInput ) ) {
				assertThat( input.readLong() ).isEqualTo( time );
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
