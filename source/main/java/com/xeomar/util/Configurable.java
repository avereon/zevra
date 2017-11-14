package com.xeomar.util;

import com.xeomar.settings.Settings;

public interface Configurable {

	void setSettings( Settings settings );

	Settings getSettings();

}
