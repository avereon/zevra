package com.xeomar.settings;

import com.xeomar.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractSettings implements Settings {

	private static final Logger log = LoggerFactory.getLogger( AbstractSettings.class );

	private Set<SettingsListener> listeners;

	protected AbstractSettings() {
		this.listeners = new CopyOnWriteArraySet<>();
	}

	@Override
	public Settings getNode( String parent, String name ) {
		return getNode( PathUtil.resolve( parent, name ) );
	}

	@Override
	public Boolean getBoolean( String key ) {
		return getBoolean( key, null );
	}

	@Override
	public Boolean getBoolean( String key, Boolean defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Boolean.parseBoolean( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	public Integer getInteger( String key ) {
		return getInteger( key, null );
	}

	@Override
	public Integer getInteger( String key, Integer defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Integer.parseInt( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	public Long getLong( String key ) {
		return getLong( key, null );
	}

	@Override
	public Long getLong( String key, Long defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Long.parseLong( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	public Float getFloat( String key ) {
		return getFloat( key, null );
	}

	@Override
	public Float getFloat( String key, Float defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Float.parseFloat( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	public Double getDouble( String key ) {
		return getDouble( key, null );
	}

	@Override
	public Double getDouble( String key, Double defaultValue ) {
		String value = get( key );
		if( value == null ) return defaultValue;
		try {
			return Double.parseDouble( value );
		} catch( NumberFormatException exception ) {
			return null;
		}
	}

	@Override
	public void addSettingsListener( SettingsListener listener ) {
		listeners.add( listener );
	}

	@Override
	public void removeSettingsListener( SettingsListener listener ) {
		listeners.remove( listener );
	}

	Set<SettingsListener> getListeners() {
		return listeners;
	}

	String getNodePath( String root, String path ) {
		return PathUtil.normalize( PathUtil.isAbsolute( path ) ? path : PathUtil.resolve( root, path ) );
	}

}
