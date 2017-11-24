package com.xeomar.settings;

import com.xeomar.product.ProductEventListener;

public interface SettingsListener extends ProductEventListener<SettingsEvent> {

	void eventOccurred( SettingsEvent event );

}
