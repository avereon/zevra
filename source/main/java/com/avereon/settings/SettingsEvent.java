package com.avereon.settings;

import com.avereon.event.Event;
import com.avereon.event.EventType;

public class SettingsEvent extends Event {

	private String path;

	private String key;

	private Object newValue;

	public SettingsEvent( Object source, EventType type, String path ) {
		this( source, type, path, null, null );
	}

	public SettingsEvent( Object source, EventType type, String path, String key, Object newValue ) {
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
		if( getEventType() == SettingsEventType.CHANGED ){
			builder.append( ":" );
			builder.append( newValue );
		}
		return builder.toString();
	}

}
