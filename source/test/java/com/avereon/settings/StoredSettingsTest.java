package com.avereon.settings;

import com.avereon.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat( settings.getNodes().size() ).isEqualTo( 0 );
		Path childFolder = path.resolve( "children" );
		Path childSettings = childFolder.resolve( "settings.properties" );
		Files.createDirectory( childFolder );
		Files.createFile( childSettings );
		assertThat( Files.exists( childSettings ) ).isTrue();
		assertThat( settings.getNodes().size() ).isEqualTo( 1 );
	}

	@Test
	void testSaveAfterDelete() {
		settings.set( "test", "1" );
		assertThat( Files.exists( path ) ).isTrue();
		settings.delete();
		assertThat( Files.exists( path ) ).isFalse();
		settings.set( "test", "2" );
		settings.flush();
		assertThat( Files.exists( path ) ).isFalse();
	}

}
