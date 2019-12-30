package com.avereon.settings;

import com.avereon.event.EventHandler;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class SettingsEventWatcher implements EventHandler<SettingsEvent> {

	private List<SettingsEvent> events = new CopyOnWriteArrayList<>();

	@Override
	public void handle( SettingsEvent event ) {
		events.add( event );
	}

	public List<SettingsEvent> getEvents() {
		return Collections.unmodifiableList( events );
	}

	public void reset() {
		events.clear();
	}

}
