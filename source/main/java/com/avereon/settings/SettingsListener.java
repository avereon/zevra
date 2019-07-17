package com.avereon.settings;

import com.avereon.product.ProductEventListener;

public interface SettingsListener extends ProductEventListener<SettingsEvent> {

	void handleEvent( SettingsEvent event );

}
