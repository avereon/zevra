package com.xeomar.razor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FileUtilTest {

	private static final String PREFIX = "com-xeomar-razor-test-";

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
		assertEquals( "0B", FileUtil.getHumanSize( 0 ) );
		assertEquals( "1B", FileUtil.getHumanSize( 1 ) );
		assertEquals( "12B", FileUtil.getHumanSize( 12 ) );
		assertEquals( "123B", FileUtil.getHumanSize( 123 ) );
		assertEquals( "1.2KB", FileUtil.getHumanSize( 1234 ) );
		assertEquals( "12KB", FileUtil.getHumanSize( 12345 ) );
		assertEquals( "123KB", FileUtil.getHumanSize( 123456 ) );
		assertEquals( "1.2MB", FileUtil.getHumanSize( 1234567 ) );
		assertEquals( "12MB", FileUtil.getHumanSize( 12345678 ) );
		assertEquals( "123MB", FileUtil.getHumanSize( 123456789 ) );

		assertEquals( "999B", FileUtil.getHumanSize( SizeUnit.KB.getSize() - 1 ) );
		assertEquals( "1.0KB", FileUtil.getHumanSize( SizeUnit.KB.getSize() ) );

		assertEquals( "999KB", FileUtil.getHumanSize( SizeUnit.MB.getSize() - 1 ) );
		assertEquals( "1.0MB", FileUtil.getHumanSize( SizeUnit.MB.getSize() ) );

		assertEquals( "999MB", FileUtil.getHumanSize( SizeUnit.GB.getSize() - 1 ) );
		assertEquals( "1.0GB", FileUtil.getHumanSize( SizeUnit.GB.getSize() ) );

		assertEquals( "999GB", FileUtil.getHumanSize( SizeUnit.TB.getSize() - 1 ) );
		assertEquals( "1.0TB", FileUtil.getHumanSize( SizeUnit.TB.getSize() ) );

		assertEquals( "999TB", FileUtil.getHumanSize( SizeUnit.PB.getSize() - 1 ) );
		assertEquals( "1.0PB", FileUtil.getHumanSize( SizeUnit.PB.getSize() ) );

		assertEquals( "999PB", FileUtil.getHumanSize( SizeUnit.EB.getSize() - 1 ) );
		assertEquals( "1.0EB", FileUtil.getHumanSize( SizeUnit.EB.getSize() ) );
	}

	@Test
	public void testGetHumanBinSize() throws Exception {
		assertEquals( "0B", FileUtil.getHumanBinSize( 0 ) );
		assertEquals( "1B", FileUtil.getHumanBinSize( 1 ) );
		assertEquals( "12B", FileUtil.getHumanBinSize( 12 ) );
		assertEquals( "123B", FileUtil.getHumanBinSize( 123 ) );
		assertEquals( "1.2KiB", FileUtil.getHumanBinSize( 1234 ) );
		assertEquals( "12KiB", FileUtil.getHumanBinSize( 12345 ) );
		assertEquals( "120KiB", FileUtil.getHumanBinSize( 123456 ) );
		assertEquals( "1.2MiB", FileUtil.getHumanBinSize( 1234567 ) );
		assertEquals( "11MiB", FileUtil.getHumanBinSize( 12345678 ) );
		assertEquals( "117MiB", FileUtil.getHumanBinSize( 123456789 ) );

		assertEquals( "1023B", FileUtil.getHumanBinSize( SizeUnit.KiB.getSize() - 1 ) );
		assertEquals( "1.0KiB", FileUtil.getHumanBinSize( SizeUnit.KiB.getSize() ) );

		assertEquals( "1023KiB", FileUtil.getHumanBinSize( SizeUnit.MiB.getSize() - 1 ) );
		assertEquals( "1.0MiB", FileUtil.getHumanBinSize( SizeUnit.MiB.getSize() ) );

		assertEquals( "1023MiB", FileUtil.getHumanBinSize( SizeUnit.GiB.getSize() - 1 ) );
		assertEquals( "1.0GiB", FileUtil.getHumanBinSize( SizeUnit.GiB.getSize() ) );

		assertEquals( "1023GiB", FileUtil.getHumanBinSize( SizeUnit.TiB.getSize() - 1 ) );
		assertEquals( "1.0TiB", FileUtil.getHumanBinSize( SizeUnit.TiB.getSize() ) );

		assertEquals( "1023TiB", FileUtil.getHumanBinSize( SizeUnit.PiB.getSize() - 1 ) );
		assertEquals( "1.0PiB", FileUtil.getHumanBinSize( SizeUnit.PiB.getSize() ) );

		assertEquals( "1023PiB", FileUtil.getHumanBinSize( SizeUnit.EiB.getSize() - 1 ) );
		assertEquals( "1.0EiB", FileUtil.getHumanBinSize( SizeUnit.EiB.getSize() ) );
	}

	@Test
	public void testGetExtensionWithFile() throws Exception {
		assertEquals( "Incorrect extension.", null, FileUtil.getExtension( (Path)null ) );
		assertEquals( "Incorrect extension.", "", FileUtil.getExtension( Paths.get( "test" ) ) );
		assertEquals( "Incorrect extension.", "txt", FileUtil.getExtension( Paths.get( "test.txt" ) ) );
	}

	@Test
	public void testGetExtensionWithName() throws Exception {
		assertEquals( "Incorrect extension.", null, FileUtil.getExtension( (String)null ) );
		assertEquals( "Incorrect extension.", "", FileUtil.getExtension( "test" ) );
		assertEquals( "Incorrect extension.", "txt", FileUtil.getExtension( "test.txt" ) );
	}

	@Test
	public void testCreateTempFolder() throws Exception {
		Path folder = FileUtil.createTempFolder( PREFIX );
		assertThat( Files.exists( folder ), is( true ) );
		String name = folder.getFileName().toString();
		Path check = Paths.get( System.getProperty( "java.io.tmpdir" ), name );
		assertThat( Files.exists( check ), is( true ) );
		assertEquals( check, folder );
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
