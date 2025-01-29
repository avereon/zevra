package com.avereon.settings;

import com.avereon.event.Event;
import com.avereon.event.EventType;
import lombok.Getter;

@Getter
public class SettingsEvent extends Event {

	public static final EventType<SettingsEvent> SETTINGS = new EventType<>( Event.ANY, "SETTINGS" );

	public static final EventType<SettingsEvent> ANY = SETTINGS;

	public static final EventType<SettingsEvent> SAVED = new EventType<>( SETTINGS, "SAVED" );

	public static final EventType<SettingsEvent> CHANGED = new EventType<>( SETTINGS, "CHANGED" );

	public static final EventType<SettingsEvent> LOADED = new EventType<>( SETTINGS, "LOADED" );

	private final String path;

	private final String key;

	private final Object oldValue;

	private final Object newValue;

	public SettingsEvent( Settings settings, EventType<SettingsEvent> type, String path ) {
		this( settings, type, path, null, null );
	}

	public SettingsEvent( Settings settings, EventType<SettingsEvent> type, String path, String key, Object newValue ) {
		this( settings, type, path, key, null, newValue );
	}

	public SettingsEvent( Settings settings, EventType<SettingsEvent> type, String path, String key, Object oldValue, Object newValue ) {
		super( settings, type );
		this.path = path;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Settings getSettings() {
		return (Settings)getSource();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public EventType<SettingsEvent> getEventType() {
		return (EventType<SettingsEvent>)super.getEventType();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( super.toString() );
		if( path != null ) builder.append( " path=" ).append( path );
		if( key != null ) builder.append( " key=" ).append( key );
		if( getEventType() == CHANGED ) builder.append( " old=" ).append( oldValue ).append( " new=" ).append( newValue );
		return builder.toString();
	}

}
