package com.avereon.settings;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class SettingsEvent extends Event {

	public static final EventType<SettingsEvent> SETTINGS = new EventType<>( EventType.ROOT, "SETTINGS" );

	public static final EventType<SettingsEvent> ANY = SETTINGS;

	public static final EventType<SettingsEvent> SAVED = new EventType<>( ANY, "SAVED" );

	public static final EventType<SettingsEvent> CHANGED = new EventType<>( ANY, "CHANGED" );

	public static final EventType<SettingsEvent> LOADED = new EventType<>( ANY, "LOADED" );

	private String path;

	private String key;

	private Object newValue;

	public SettingsEvent( Object source, EventType<SettingsEvent> type, String path ) {
		this( source, type, path, null, null );
	}

	public SettingsEvent( Object source, EventType<SettingsEvent> type, String path, String key, Object newValue ) {
		super( source, type );
		this.path = path;
		this.key = key;
		this.newValue = newValue;
	}

	public String getPath() {
		return path;
	}

	public String getKey() {
		return key;
	}

	public Object getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( super.toString() );
		builder.append( ":" );
		builder.append( getEventType() );
		if( path != null ) {
			builder.append( ":" );
			builder.append( path );
		}
		if( key != null ) {
			builder.append( ":" );
			builder.append( key );
		}
		if( getEventType() == CHANGED ){
			builder.append( ":" );
			builder.append( newValue );
		}
		return builder.toString();
	}

}
