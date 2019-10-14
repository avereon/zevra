package com.avereon.settings;

import com.avereon.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

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

}
