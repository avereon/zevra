package com.avereon.settings;

import com.avereon.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StoredSettingsTest extends BaseSettingsTest {

	private Path path;

	@BeforeEach
	void setup() throws Exception {
		path = FileUtil.createTempFolder( SETTINGS_NAME );
		settings = new StoredSettings( path );
	}

	@AfterEach
	void cleanup() throws Exception {
		FileUtil.delete( path );
	}

	@Test
	void testSaveAfterDelete() {
		settings.set( "test", "1" );
		assertTrue( Files.exists( path ));
		settings.delete();
		assertFalse( Files.exists( path ));
		settings.set( "test", "2" );
		settings.flush();
		assertFalse( Files.exists( path ));
	}

}
