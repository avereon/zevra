package com.avereon.settings;

import com.avereon.event.EventType;

public class SettingsEventType extends EventType {

	public static EventType ANY = new EventType( EventType.ROOT, "SETTINGS" );

	public static EventType CHANGED = new EventType( ANY, "CHANGED" );

	public static EventType LOADED = new EventType( ANY, "LOADED" );

	public static EventType SAVED = new EventType( ANY, "SAVED" );

	private SettingsEventType( EventType parent, String name ) {
		super( parent, name );
	}

}
