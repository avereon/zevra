package com.avereon.settings;

import com.avereon.event.EventHub;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class SettingsEventWatcher extends EventHub<SettingsEvent> implements SettingsListener {

	private List<SettingsEvent> events = new CopyOnWriteArrayList<>();

	public SettingsEventWatcher() {
		register( SettingsEvent.ANY, ( e) -> events.add( e ) );
	}

	public List<SettingsEvent> getEvents() {
		return Collections.unmodifiableList( events );
	}

	public void reset() {
		events.clear();
	}

}
