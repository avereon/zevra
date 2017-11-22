package com.xeomar.settings;

import com.xeomar.util.FileUtil;
import org.junit.After;
import org.junit.Before;

import java.nio.file.Path;

public class StoredSettingsTest extends BaseSettingsTest {

	private Path path;

	@Before
	public void setup() throws Exception {
		path = FileUtil.createTempFolder( SETTINGS_NAME );
		settings = new StoredSettings( path );
	}

	@After
	public void cleanup() throws Exception {
		FileUtil.deleteOnExit( path );
	}

}
