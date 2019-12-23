package com.avereon.settings;

import com.avereon.event.EventType;

public class SettingsEventType extends EventType<SettingsEvent> {

	public static EventType<SettingsEvent> CHANGED = new EventType<>( SettingsEvent.ANY, "CHANGED" );

	public static EventType<SettingsEvent> LOADED = new EventType<>( SettingsEvent.ANY, "LOADED" );

	public static EventType<SettingsEvent> SAVED = new EventType<>( SettingsEvent.ANY, "SAVED" );

	private SettingsEventType( EventType<SettingsEvent> parent, String name ) {
		super( parent, name );
	}

}
