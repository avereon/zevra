package com.xeomar.settings;

import com.xeomar.product.ProductEvent;

public class SettingsEvent extends ProductEvent {

	public enum Type {
		CHANGED,
		LOADED,
		SAVED
	}

	private Type type;

	private String path;

	private String key;

	private Object newValue;

	public SettingsEvent( Object source, Type type, String path ) {
		this( source, type, path, null, null );
	}

	public SettingsEvent( Object source, Type type, String path, String key, Object newValue ) {
		super( source );
		this.type = type;
		this.path = path;
		this.key = key;
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
		if( type == Type.CHANGED ){
			builder.append( ":" );
			builder.append( newValue );
		}
		return builder.toString();
	}

}
