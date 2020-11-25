package com.avereon.settings;

import com.avereon.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoredSettingsTest extends BaseSettingsTest {

	private static final String SETTINGS_NAME = "AvereonSettingsTest";

	private Path path;

	@BeforeEach
	void setup() throws Exception {
		path = FileUtil.createTempFolder( SETTINGS_NAME );
		settings = new StoredSettings( path );
	}

	@AfterEach
	void cleanup() throws Exception {
		settings.delete();
		if( Files.exists( path ) ) throw new IllegalStateException( "File still exists: " + path );
		Thread.sleep( 100 );
		if( Files.exists( path ) ) throw new IllegalStateException( "File came back after delete: " + path );
	}

	@Test
	void testGetNodesFromFolder() throws IOException {
		assertThat( settings.getNodes().size(), is( 0 ) );
		Path childFolder = path.resolve( "children" );
		Path childSettings = childFolder.resolve( "settings.properties" );
		Files.createDirectory( childFolder );
		Files.createFile( childSettings );
		assertTrue( Files.exists( childSettings ) );
		assertThat( settings.getNodes().size(), is( 1 ) );
	}

	@Test
	void testSaveAfterDelete() {
		settings.set( "test", "1" );
		assertTrue( Files.exists( path ) );
		settings.delete();
		assertFalse( Files.exists( path ) );
		settings.set( "test", "2" );
		settings.flush();
		assertFalse( Files.exists( path ) );
	}

}
