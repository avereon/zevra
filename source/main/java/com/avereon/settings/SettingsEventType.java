package com.avereon.settings;

import com.avereon.event.EventType;

public class SettingsEventType extends EventType {

	public static EventType SETTINGS = new EventType( EventType.ROOT, "SETTINGS" );

	public static EventType CHANGED = new EventType( SETTINGS, "CHANGED" );

	public static EventType LOADED = new EventType( SETTINGS, "LOADED" );

	public static EventType SAVED = new EventType( SETTINGS, "SAVED" );

	private SettingsEventType( EventType parent, String name ) {
		super( parent, name );
	}

}
