package com.xeomar.settings;

import com.xeomar.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractSettings implements Settings {

	private static final Logger log = LoggerFactory.getLogger( AbstractSettings.class );

	// The settings node values
	private Map<String, String> values;

	// The settings defaults.
	private Map<String, String> defaultValues;

	private Set<SettingsListener> listeners;

	protected AbstractSettings() {
		this.listeners = new CopyOnWriteArraySet<>();
	}

	protected abstract String getValue( String key );

	protected abstract void setValue( String key, String value );

	@Override
	public void set( String key, Object value ) {
		String oldValue;

		if( value instanceof Collection ) {
			// NEXT Handle storing collections differently
			// Find a way to store the collection as JSON
			oldValue = null;
			//oldValue = getValues( key );
			//setValues( key, value );
		} else {
			oldValue= getValue( key );
			setValue( key, value == null ? null : String.valueOf( value ) );
		}

		if( !Objects.equals( oldValue, value ) ) new SettingsEvent( this, SettingsEvent.Type.UPDATED, getPath(), key, oldValue, value ).fire( getListeners() );
	}

	@Override
	public Settings getNode( String parent, String name ) {
		return getNode( PathUtil.resolve( parent, name ) );
	}

	@Override
	public String getString( String key ) {
		return getString( key, null );
	}

	@Override
	public String getString( String key, String defaultValue ) {
		String value = getValue( key );
		Map<String, String> defaultValues = getDefaultValues();
		if( value == null && defaultValues != null ) value = defaultValues.get( key );
		if( value == null && defaultValue != null ) value = defaultValue.toString();
		return value;
	}

	@Override
	public Boolean getBoolean( String key ) {
		return getBoolean( key, null );
	}

	@Override
	public Boolean getBoolean( String key, Boolean defaultValue ) {
		String value = getString( key );
		if( value == null ) return defaultValue;
		return Boolean.parseBoolean( value );
	}

	@Override
	public Integer getInteger( String key ) {
		return getInteger( key, null );
	}

	@Override
	public Integer getInteger( String key, Integer defaultValue ) {
		String value = getString( key );
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
		String value = getString( key );
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
		String value = getString( key );
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
		String value = getString( key );
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
