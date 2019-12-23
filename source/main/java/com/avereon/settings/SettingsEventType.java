package com.avereon.settings;

import com.avereon.event.EventType;

public class SettingsEventType extends EventType<SettingsEvent> {

	public static EventType<SettingsEvent> ANY = new EventType<>( EventType.ROOT, "SETTINGS" );

	public static EventType<SettingsEvent> CHANGED = new EventType<>( ANY, "CHANGED" );

	public static EventType<SettingsEvent> LOADED = new EventType<>( ANY, "LOADED" );

	public static EventType<SettingsEvent> SAVED = new EventType<>( ANY, "SAVED" );

	private SettingsEventType( EventType<SettingsEvent> parent, String name ) {
		super( parent, name );
	}

}
