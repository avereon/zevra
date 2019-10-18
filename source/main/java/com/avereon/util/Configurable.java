package com.avereon.util;

import com.avereon.settings.Settings;

@Deprecated
public interface Configurable {

	@Deprecated
	void setSettings( Settings settings );

	@Deprecated
	Settings getSettings();

}
