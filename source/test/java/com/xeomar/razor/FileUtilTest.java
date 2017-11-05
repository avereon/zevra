package com.xeomar.razor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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
