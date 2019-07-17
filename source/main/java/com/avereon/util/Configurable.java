package com.avereon.util;

import com.avereon.settings.Settings;

public interface Configurable {

	void setSettings( Settings settings );

	Settings getSettings();

}
