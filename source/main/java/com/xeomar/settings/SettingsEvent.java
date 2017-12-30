package com.xeomar.settings;

import com.xeomar.product.ProductEvent;

public class SettingsEvent extends ProductEvent {

	public enum Type {
		UPDATED,
		LOADED,
		SAVED
	}

	private Type type;

	private String path;

	private String key;

	private Object oldValue;

	private Object newValue;

	public SettingsEvent( Object source, Type type, String path ) {
		this( source, type, path, null, null, null );
	}

	public SettingsEvent( Object source, Type type, String path, String key, Object oldValue, Object newValue ) {
		super( source );
		this.type = type;
		this.path = path;
		this.key = key;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public Type getType() {
		return type;
	}

	public String getPath() {
		return path;
	}

	public String getKey() {
		return key;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder( super.toString() );
		builder.append( ":" );
		builder.append( type );
		if( path != null ) {
			builder.append( ":" );
			builder.append( path );
		}
		if( key != null ) {
			builder.append( ":" );
			builder.append( key );
		}
		if( type == Type.UPDATED){
			builder.append( ":" );
			builder.append( oldValue );
			builder.append( ":" );
			builder.append( newValue );
		}
		return builder.toString();
	}

}
