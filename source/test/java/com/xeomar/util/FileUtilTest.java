package com.xeomar.util;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class FileUtilTest {

	private static final String PREFIX = "com-xeomar-util-test-";

	@Before
	public void setup() throws Exception {
		removeTempFiles();
	}

	@After
	public void cleanup() throws Exception {
		removeTempFiles();
	}

	@Test
	public void testGetHumanSize() throws Exception {
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

		assertThat( FileUtil.getHumanSize( SizeUnit.KB.getSize() - 1 ), is( "999B" ) );
		assertThat( FileUtil.getHumanSize( SizeUnit.KB.getSize() ), is( "1.0KB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnit.MB.getSize() - 1 ), is( "999KB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnit.MB.getSize() ), is( "1.0MB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnit.GB.getSize() - 1 ), is( "999MB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnit.GB.getSize() ), is( "1.0GB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnit.TB.getSize() - 1 ), is( "999GB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnit.TB.getSize() ), is( "1.0TB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnit.PB.getSize() - 1 ), is( "999TB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnit.PB.getSize() ), is( "1.0PB" ) );

		assertThat( FileUtil.getHumanSize( SizeUnit.EB.getSize() - 1 ), is( "999PB" ) );
		assertThat( FileUtil.getHumanSize( SizeUnit.EB.getSize() ), is( "1.0EB" ) );
	}

	@Test
	public void testGetHumanBinSize() throws Exception {
		assertThat( FileUtil.getHumanBinSize( 0 ), is( "0B" ) );
		assertThat( FileUtil.getHumanBinSize( 1 ), is( "1B" ) );
		assertThat( FileUtil.getHumanBinSize( 12 ), is( "12B" ) );
		assertThat( FileUtil.getHumanBinSize( 123 ), is( "123B" ) );
		assertThat( FileUtil.getHumanBinSize( 1234 ), is( "1.2KiB" ) );
		assertThat( FileUtil.getHumanBinSize( 12345 ), is( "12KiB" ) );
		assertThat( FileUtil.getHumanBinSize( 123456 ), is( "120KiB" ) );
		assertThat( FileUtil.getHumanBinSize( 1234567 ), is( "1.2MiB" ) );
		assertThat( FileUtil.getHumanBinSize( 12345678 ), is( "11MiB" ) );
		assertThat( FileUtil.getHumanBinSize( 123456789 ), is( "117MiB" ) );

		assertThat( FileUtil.getHumanBinSize( SizeUnit.KiB.getSize() - 1 ), is( "1023B" ) );
		assertThat( FileUtil.getHumanBinSize( SizeUnit.KiB.getSize() ), is( "1.0KiB" ) );

		assertThat( FileUtil.getHumanBinSize( SizeUnit.MiB.getSize() - 1 ), is( "1023KiB" ) );
		assertThat( FileUtil.getHumanBinSize( SizeUnit.MiB.getSize() ), is( "1.0MiB" ) );

		assertThat( FileUtil.getHumanBinSize( SizeUnit.GiB.getSize() - 1 ), is( "1023MiB" ) );
		assertThat( FileUtil.getHumanBinSize( SizeUnit.GiB.getSize() ), is( "1.0GiB" ) );

		assertThat( FileUtil.getHumanBinSize( SizeUnit.TiB.getSize() - 1 ), is( "1023GiB" ) );
		assertThat( FileUtil.getHumanBinSize( SizeUnit.TiB.getSize() ), is( "1.0TiB" ) );

		assertThat( FileUtil.getHumanBinSize( SizeUnit.PiB.getSize() - 1 ), is( "1023TiB" ) );
		assertThat( FileUtil.getHumanBinSize( SizeUnit.PiB.getSize() ), is( "1.0PiB" ) );

		assertThat( FileUtil.getHumanBinSize( SizeUnit.EiB.getSize() - 1 ), is( "1023PiB" ) );
		assertThat( FileUtil.getHumanBinSize( SizeUnit.EiB.getSize() ), is( "1.0EiB" ) );
	}

	@Test
	public void testGetExtensionWithPath() throws Exception {
		assertThat( FileUtil.getExtension( (Path)null ), is( nullValue() ) );
		assertThat( FileUtil.getExtension( Paths.get( "test" ) ), is( "" ) );
		assertThat( FileUtil.getExtension( Paths.get( "test.txt" ) ), is( "txt" ) );
	}

	@Test
	public void testGetExtensionWithName() throws Exception {
		assertThat( FileUtil.getExtension( (String)null ), is( nullValue() ) );
		assertThat( FileUtil.getExtension( "test" ), is( "" ) );
		assertThat( FileUtil.getExtension( "test.txt" ), is( "txt" ) );
	}

	@Test
	public void testRemoveExtensionWithPath() throws Exception {
		assertThat( FileUtil.removeExtension( (Path)null ), is( nullValue() ) );
		assertThat( FileUtil.removeExtension( Paths.get( "test" ) ), is( Paths.get( "test" ) ) );
		assertThat( FileUtil.removeExtension( Paths.get( "test.txt" ) ), is( Paths.get( "test" ) ) );
	}

	@Test
	public void testRemoveExtensionWithName() throws Exception {
		assertThat( FileUtil.removeExtension( (String)null ), is( nullValue() ) );
		assertThat( FileUtil.removeExtension( "test" ), is( "test" ) );
		assertThat( FileUtil.removeExtension( "test.txt" ), is( "test" ) );
	}

	@Test
	public void testSaveAndLoad() throws Exception {
		Path path = Files.createTempFile( PREFIX, "Test" );
		FileUtil.save( path.toString(), path );
		assertThat( FileUtil.load( path ), is( path.toString() ) );
	}

	@Test
	public void testCopyWithNonExistantFiles() throws Exception {
		assertThat( FileUtil.copy( Paths.get( "/nonexistant" ), Paths.get( "/nonexistant" ) ), is( false ) );
	}

	@Test
	public void testCopyFileToFile() throws Exception {
		long time = System.currentTimeMillis();
		Path source = Files.createTempFile( PREFIX, "copyFileToFileSource" );
		Path target = Files.createTempFile( PREFIX, "copyFileToFileTarget" );
		FileOutputStream fileOutput = new FileOutputStream( source.toFile() );
		DataOutputStream output = new DataOutputStream( fileOutput );
		output.writeLong( time );
		output.close();

		assertThat( FileUtil.copy( source, target ), is( true ) );

		FileInputStream fileInput = new FileInputStream( target.toFile() );
		DataInputStream input = new DataInputStream( fileInput );
		assertThat( input.readLong(), is( time ) );
		input.close();

		source.toFile().deleteOnExit();
		target.toFile().deleteOnExit();
	}

	@Test
	public void testCopyFileToNewFile() throws Exception {
		long time = System.currentTimeMillis();
		Path source = Files.createTempFile( PREFIX, "copyFileToFileSource" );
		Path temp = Files.createTempFile( PREFIX, "copyFileToFileTarget" );
		Path target = temp.getParent().resolve( "copyFileToNewFileTarget" );
		FileOutputStream fileOutput = new FileOutputStream( source.toFile() );
		DataOutputStream output = new DataOutputStream( fileOutput );
		output.writeLong( time );
		output.close();

		assertThat( FileUtil.copy( source, target ), is( true ) );

		FileInputStream fileInput = new FileInputStream( target.toFile() );
		DataInputStream input = new DataInputStream( fileInput );
		assertThat( input.readLong(), is( time ) );
		input.close();

		source.toFile().deleteOnExit();
		temp.toFile().deleteOnExit();
		target.toFile().deleteOnExit();
	}

	@Test
	public void testCopyFileToFolder() throws Exception {
//		long time = System.currentTimeMillis();
//		Path source = Files.createTempFile( PREFIX, "copyFileToFolderSource" );
//		Path target = FileUtil.createTempFolder( PREFIX, "copyFileToFolderTarget" );
//		FileOutputStream fileOutput = new FileOutputStream( source.toFile() );
//		DataOutputStream output = new DataOutputStream( fileOutput );
//		output.writeLong( time );
//		output.close();
//
//		assertThat( FileUtil.copy( source, target ), is( true ) );
//
//		Path child = target.resolve( source.getFileName() );
//		FileInputStream fileInput = new FileInputStream( child.toFile() );
//		DataInputStream input = new DataInputStream( fileInput );
//		assertThat( input.readLong(), is( time ) );
//		input.close();
//
//		source.toFile().deleteOnExit();
//		target.toFile().deleteOnExit();
	}

	@Test
	public void testCopyFolderToFile() throws Exception {
		//		File source = FileUtil.createTempFolder( PREFIX, "copyFolderToFileSource" );
		//		File target = File.createTempFile( PREFIX, "copyFolderToFileTarget" );
		//		assertThat( FileUtil.copy( source, target ), is( false ) );
		//		assertThat( source.exists(), is( true ) );
		//		assertThat( target.exists(), is( true ) );
		//
		//		source.deleteOnExit();
		//		target.deleteOnExit();
	}

	@Test
	public void testCopyFolderToFolder() throws Exception {
		//		File parent0 = FileUtil.createTempFolder( PREFIX, "copyFolderToFolderParent0" );
		//		File parent1 = FileUtil.createTempFolder( PREFIX, "copyFolderToFolderParent1", parent0 );
		//		File leaf0 = File.createTempFile( PREFIX, "copyFolderToFolderLeaf0", parent0 );
		//		File leaf1 = File.createTempFile( PREFIX, "copyFolderToFolderLeaf1", parent0 );
		//		File leaf2 = File.createTempFile( PREFIX, "copyFolderToFolderLeaf2", parent1 );
		//		File leaf3 = File.createTempFile( PREFIX, "copyFolderToFolderLeaf3", parent1 );
		//		assertThat( parent0.listFiles().length, is( 3 ) );
		//		assertThat( parent1.listFiles().length, is( 2 ) );
		//
		//		File target = FileUtil.createTempFolder( PREFIX, "copyFolderToFolderTarget" );
		//
		//		assertThat( FileUtil.copy( parent0, target, false ), is( true ) );
		//
		//		File target1 = new File( target, parent1.getName() );
		//		assertThat( target.listFiles().length, is( 3 ) );
		//		assertThat( target1.listFiles().length, is( 2 ) );
		//		assertThat( new File( target, leaf0.getName() ).exists(), is( true ) );
		//		assertThat( new File( target, leaf1.getName() ).exists(), is( true ) );
		//		assertThat( new File( target1, leaf2.getName() ).exists(), is( true ) );
		//		assertThat( new File( target1, leaf3.getName() ).exists(), is( true ) );
		//
		//		parent0.deleteOnExit();
		//		parent1.deleteOnExit();
	}

	@Test
	public void testCopyFolderToFolderWithSourceFolder() throws Exception {
		//		Path parent0 = FileUtil.createTempFolder( PREFIX, "copyFolderToFolderParent0" );
		//		Path parent1 = FileUtil.createTempFolder( parent0, PREFIX, "copyFolderToFolderParent1" );
		//		Path leaf0 = Files.createTempFile( parent0, PREFIX, "copyFolderToFolderLeaf0" );
		//		Path leaf1 = Files.createTempFile( parent0, PREFIX, "copyFolderToFolderLeaf1" );
		//		Path leaf2 = Files.createTempFile( parent1, PREFIX, "copyFolderToFolderLeaf2" );
		//		Path leaf3 = Files.createTempFile( parent1, PREFIX, "copyFolderToFolderLeaf3" );
		//		assertThat( Files.list( parent0 ).count(), is( 3 ) );
		//		assertThat( Files.list( parent1 ).count(), is( 2 ) );
		//
		//		Path target = FileUtil.createTempFolder( PREFIX, "copyFolderToFolderTarget" );

		//		assertThat( FileUtil.copy( parent0, target, true ), is( true ) );
		//
		//		File target0 = new File( target, parent0.getName() );
		//		File target1 = new File( target0, parent1.getName() );
		//		assertThat( target0.listFiles().length, is( 3 ) );
		//		assertThat( target1.listFiles().length, is( 2 ) );
		//		assertThat( new File( target0, leaf0.getName() ).exists(), is( true ) );
		//		assertThat( new File( target0, leaf1.getName() ).exists(), is( true ) );
		//		assertThat( new File( target1, leaf2.getName() ).exists(), is( true ) );
		//		assertThat( new File( target1, leaf3.getName() ).exists(), is( true ) );
		//
		//		parent0.deleteOnExit();
		//		parent1.deleteOnExit();
	}

	@Test
	public void testCopyFileToOutputStream() throws Exception {
		//		long time = System.currentTimeMillis();
		//		File source = File.createTempFile( PREFIX, "copyFileToFileSource" );
		//		ByteArrayOutputStream target = new ByteArrayOutputStream();
		//		FileOutputStream fileOutput = new FileOutputStream( source );
		//		DataOutputStream output = new DataOutputStream( fileOutput );
		//		output.writeLong( time );
		//		output.close();
		//
		//		assertThat( FileUtil.copy( source, target ), is( 8 ) );
		//
		//		DataInputStream input = new DataInputStream( new ByteArrayInputStream( target.toByteArray() ) );
		//		assertThat( input.readLong(), is( time ) );
		//		input.close();
		//
		//		source.deleteOnExit();
	}

	@Test
	public void testZipAndUnzip() throws Exception {
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
			if( Files.exists( targetData ) ) FileUtils.forceDelete( targetData.toFile() );
			assertThat( Files.exists( targetData ), is( false ) );
		}
	}

	@Test
	public void testListPaths() throws Exception {
		Path sourceRoot = Files.createTempDirectory( getClass().getSimpleName() );
		try {
			Path sourceFile1 = Files.createTempFile( sourceRoot, getClass().getSimpleName(), "" );
			Path sourceFile2 = Files.createTempFile( sourceRoot, getClass().getSimpleName(), "" );
			Path sourceSubFolder = Files.createTempDirectory( sourceRoot, getClass().getSimpleName() );
			Path sourceFile3 = Files.createTempFile( sourceSubFolder, getClass().getSimpleName(), "" );
			Path sourceFile4 = Files.createTempFile( sourceSubFolder, getClass().getSimpleName(), "" );

			List<Path> paths = FileUtil.listPaths( sourceRoot );

			for( Path expected : List.of( sourceRoot, sourceFile1, sourceFile2, sourceSubFolder, sourceFile3, sourceFile4 ) ) {
				assertThat( paths.contains( expected ), is( true ) );
			}
			assertThat( paths.size(), is( 6 ) );
		} finally {
			FileUtils.forceDelete( sourceRoot.toFile() );
		}
	}

	@Test
	public void testCreateTempFolder() throws Exception {
		Path folder = FileUtil.createTempFolder( PREFIX );
		assertThat( Files.exists( folder ), is( true ) );
		String name = folder.getFileName().toString();
		Path check = Paths.get( System.getProperty( "java.io.tmpdir" ), name );
		assertThat( Files.exists( check ), is( true ) );
		assertThat( folder, is( check ) );
		Files.deleteIfExists( folder );
	}

	private void removeTempFiles() throws Exception {
		Path tmp = Paths.get( System.getProperty( "java.io.tmpdir" ) );
		DirectoryStream<Path> stream = Files.newDirectoryStream( tmp, PREFIX + "*" );
		for( Path entry : stream ) {
			Files.deleteIfExists( entry );
		}
	}

}
