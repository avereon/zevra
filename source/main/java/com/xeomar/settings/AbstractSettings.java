package com.xeomar.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xeomar.util.LogUtil;
import com.xeomar.util.PathUtil;
import com.xeomar.util.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractSettings implements Settings {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final Map<Class<?>, OutboundConverter> outboundConverters;

	private static final Map<Class<?>, InboundConverter> inboundConverters;

	private Map<String, Object> defaultValues;

	private Set<SettingsListener> listeners;

	static {
		outboundConverters = new HashMap<>();
		outboundConverters.put( Boolean.class, String::valueOf );
		outboundConverters.put( Character.class, String::valueOf );
		outboundConverters.put( Byte.class, String::valueOf );
		outboundConverters.put( Short.class, String::valueOf );
		outboundConverters.put( Integer.class, String::valueOf );
		outboundConverters.put( Long.class, String::valueOf );
		outboundConverters.put( Float.class, String::valueOf );
		outboundConverters.put( Double.class, String::valueOf );
		outboundConverters.put( String.class, ( value ) -> (String)value );
		outboundConverters.put( URI.class, Object::toString );
		outboundConverters.put( File.class, ( value ) -> ((File)value).toURI().toString() );

		inboundConverters = new HashMap<>();
		inboundConverters.put( Boolean.class, ( value ) -> value == null ? null : Boolean.parseBoolean( value ) );
		inboundConverters.put( Character.class, ( value ) -> value == null ? null : value.charAt( 0 ) );
		inboundConverters.put( Byte.class, ( value ) -> value == null ? null : Byte.parseByte( value ) );
		inboundConverters.put( Short.class, ( value ) -> value == null ? null : Short.parseShort( value ) );
		inboundConverters.put( Integer.class, ( value ) -> value == null ? null : Integer.parseInt( value ) );
		inboundConverters.put( Long.class, ( value ) -> value == null ? null : Long.parseLong( value ) );
		inboundConverters.put( Float.class, ( value ) -> value == null ? null : Float.parseFloat( value ) );
		inboundConverters.put( Double.class, ( value ) -> value == null ? null : Double.parseDouble( value ) );
		inboundConverters.put( String.class, ( value ) -> value );
		inboundConverters.put( URI.class, ( value ) -> value == null ? null : URI.create( value ) );
		inboundConverters.put( File.class, ( value ) -> value == null ? null : new File( URI.create( value ) ) );
	}

	protected AbstractSettings() {
		this.listeners = new CopyOnWriteArraySet<>();
	}

	@Override
	public String get( String key ) {
		return get( key, String.class );
	}

	@Override
	public String get( String key, String defaultValue ) {
		return get( key, String.class, defaultValue );
	}

	@Override
	public <T> T get( String key, Class<T> type ) {
		return get( key, new TypeReference<T>( type ) {} );
	}

	@Override
	public <T> T get( String key, Class<T> type, T defaultValue ) {
		return get( key, new TypeReference<T>( type ) {}, defaultValue );
	}

	@Override
	public <T> T get( String key, TypeReference<T> type ) {
		return get( key, type, null );
	}

	@SuppressWarnings( "unchecked" )
	public <T> T get( String key, TypeReference<T> type, T defaultValue ) {
		Object value;
		Class<T> typeClass = type.getTypeClass();
		InboundConverter converter = inboundConverters.get( typeClass );

		// Unmarshall the value
		if( converter != null ) {
			value = converter.convert( getValue( key ) );
			if( value == null ) value = converter.convert( getDefault( key ) );
		} else if( typeClass.isArray() ) {
			value = unmarshallValue( getArray( key ), type, getDefault( key ) );
		} else if( typeClass.isAssignableFrom( Collection.class ) || typeClass.isAssignableFrom( Map.class ) ) {
			value = unmarshallValue( getCollection( key ), type, getDefault( key ) );
		} else {
			value = unmarshallValue( getBean( key ), type, getDefault( key ) );
		}

		// If the value is still null use the default value
		if( value == null ) value = defaultValue;

		return (T)value;
	}

	private String getDefault( String key ) {
		Map<String, Object> defaultValues = getDefaultValues();
		if( defaultValues == null ) return null;

		Object objectValue = defaultValues.get( key );
		return objectValue == null ? null : objectValue.toString();
	}

	@Override
	public void set( String key, Object value ) {
		String oldValue = get( key );
		String newValue = null;

		if( value == null ) {
			removeValue( key );
		} else if( outboundConverters.keySet().contains( value.getClass() ) ) {
			setValue( key, newValue = outboundConverters.get( value.getClass() ).convert( value ) );
		} else if( value instanceof Collection || value instanceof Map ) {
			setCollection( key, newValue = marshallValue( value ) );
		} else if( value.getClass().isArray() ) {
			setArray( key, newValue = marshallValue( value ) );
		} else {
			setBean( key, newValue = marshallValue( value ) );
		}

		// FIXME Settings change event should only be fired if the values are different
		boolean different = Objects.equals( oldValue, newValue );
		new SettingsEvent( this, SettingsEvent.Type.CHANGED, getPath(), key, value ).fire( getListeners() );
	}

	/**
	 * Override this method to optimize retrieving simple values.
	 *
	 * @param key The value key
	 * @return The marshalled simple value
	 */
	protected abstract String getValue( String key );

	/**
	 * Override this method to optimize retrieving bean values.
	 *
	 * @param key The value key
	 * @return The marshalled bean value
	 */
	protected String getBean( String key ) {
		return getValue( key );
	}

	/**
	 * Override this method to optimize retrieving array values.
	 *
	 * @param key The value key
	 * @return The marshalled array value
	 */
	protected String getArray( String key ) {
		return getValue( key );
	}

	/**
	 * Override this method to optimize retrieving collection values.
	 *
	 * @param key The value key
	 * @return The marshalled collection value
	 */
	protected String getCollection( String key ) {
		return getValue( key );
	}

	/**
	 * Override this method to optimize storing simple values.
	 *
	 * @param key The settings value key
	 * @param value The settings value
	 */
	protected abstract void setValue( String key, String value );

	/**
	 * Override this method to optimize storing bean values.
	 *
	 * @param key The settings value key
	 * @param value The settings value
	 */
	protected void setBean( String key, String value ) {
		setValue( key, value );
	}

	/**
	 * Override this method to optimize storing array values.
	 *
	 * @param key The settings value key
	 * @param value The settings value
	 */
	protected void setArray( String key, String value ) {
		setValue( key, value );
	}

	/**
	 * Override this method to optimize storing collection values.
	 *
	 * @param key The settings value key
	 * @param value The settings value
	 */
	protected void setCollection( String key, String value ) {
		setValue( key, value );
	}

	/**
	 * Override this method to optimize removing a value without knowing what
	 * category it belongs to.
	 *
	 * @param key The value key
	 */
	protected void removeValue( String key ) {
		setValue( key, null );
		setCollection( key, null );
		setArray( key, null );
		setBean( key, null );
	}

	protected String marshallValue( Object value ) {
		try {
			return new ObjectMapper().writeValueAsString( value );
		} catch( JsonProcessingException exception ) {
			log.warn( "Error marshalling value", exception );
			return null;
		}
	}

	@SuppressWarnings( "unchecked" )
	protected <T> T unmarshallValue( String value, TypeReference<T> type, String defaultValue ) {
		if( value == null ) value = defaultValue;
		if( value == null ) return null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			return (T)mapper.readerFor( mapper.constructType( type.getType() ) ).readValue( value );
		} catch( IOException exception ) {
			log.warn( "Error unmarshalling value", exception );
			return null;
		}
	}

	@Override
	public Settings getNode( String parent, String name ) {
		return getNode( PathUtil.resolve( parent, name ) );
	}

	@Override
	public Map<String, Object> getDefaultValues() {
		return defaultValues;
	}

	@Override
	public void setDefaultValues( Map<String, Object> defaultValues ) {
		this.defaultValues = defaultValues;
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

	private interface OutboundConverter {

		String convert( Object value );

	}

	private interface InboundConverter {

		Object convert( String value );

	}

}
