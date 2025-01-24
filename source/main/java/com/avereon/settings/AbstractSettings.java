package com.avereon.settings;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import com.avereon.util.PathUtil;
import com.avereon.util.TextUtil;
import com.avereon.util.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.CustomLog;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@CustomLog
public abstract class AbstractSettings implements Settings {

	private static final Map<Class<?>, OutboundConverter> outboundConverters;

	private static final Map<Class<?>, InboundConverter> inboundConverters;

	private Map<?, ?> defaultValues;

	private boolean deleted;

	@Getter
	private final EventHub eventHub;

	private final Map<String, Set<EventHandler<SettingsEvent>>> valueChangeHandlers;

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
		this.eventHub = new EventHub();
		this.valueChangeHandlers = new ConcurrentHashMap<>();
		this.eventHub.register( SettingsEvent.CHANGED, this::dispatch );
	}

	@Override
	public boolean exists( String path ) {
		return getValue( path ) != null;
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
		} else if( Collection.class.isAssignableFrom( typeClass ) ) {
			value = getCollection( key, type );
		} else if( Map.class.isAssignableFrom( typeClass ) ) {
			value = getMap( key, type );
		} else if( typeClass.isArray() ) {
			value = getArray( key, type );
		} else {
			value = getBean( key, type );
		}

		// If the value is still null use the default value
		if( value == null ) value = defaultValue;

		return (T)value;
	}

	private String getDefault( String key ) {
		Map<?, ?> defaultValues = getDefaultValues();
		if( defaultValues == null ) return null;

		Object objectValue = defaultValues.get( key );
		return objectValue == null ? null : objectValue.toString();
	}

	@Override
	public Settings set( String key, Object value ) {
		String oldValue = get( key );
		String newValue = null;

		if( value == null ) {
			setValue( key, null );
		} else if( outboundConverters.containsKey( value.getClass() ) ) {
			setValue( key, newValue = outboundConverters.get( value.getClass() ).convert( value ) );
		} else if( value instanceof Collection ) {
			setCollection( key, (Collection<?>)value );
		} else if( value instanceof Map ) {
			setMap( key, (Map<?, ?>)value );
		} else if( value.getClass().isArray() ) {
			setArray( key, (Object[])value );
		} else {
			setBean( key, value );
		}

		// Settings change event should only be fired if the values are different
		if( !Objects.equals( oldValue, newValue ) ) getEventHub().dispatch( new SettingsEvent( this, SettingsEvent.CHANGED, getPath(), key, value ) );

		return this;
	}

	@Override
	public Settings copyFrom( Settings settings ) {
		// Shallow
		for( String key : settings.getKeys() ) {
			set( key, settings.get( key ) );
		}
		// Deep
		for( String child : settings.getNodes() ) {
			getNode( child ).copyFrom( settings.getNode( child ) );
		}
		return this;
	}

	@Override
	public Settings remove( String key ) {
		set( key, null );
		return this;
	}

	/**
	 * Override this method to optimize retrieving simple values.
	 *
	 * @param key The value key
	 * @return The unmarshalled simple value
	 */
	protected abstract String getValue( String key );

	/**
	 * Override this method to optimize retrieving map values.
	 *
	 * @param key The value key
	 * @return The unmarshalled collection value
	 */
	protected <S> S getBean( String key, TypeReference<S> type ) {
		return unmarshallValue( getValue( key ), type );
	}

	/**
	 * Override this method to optimize retrieving array values.
	 *
	 * @param key The value key
	 * @return The unmarshalled array value
	 */
	protected <S> Object[] getArray( String key, TypeReference<S> type ) {
		return (Object[])unmarshallValue( getValue( key ), type );
	}

	/**
	 * Override this method to optimize retrieving collection values.
	 *
	 * @param key The value key
	 * @return The unmarshalled collection value
	 */
	protected <S> Collection<?> getCollection( String key, TypeReference<S> type ) {
		return (Collection<?>)unmarshallValue( getValue( key ), type );
	}

	/**
	 * Override this method to optimize retrieving map values.
	 *
	 * @param key The value key
	 * @return The marshalled collection value
	 */
	protected <S> Map<?, ?> getMap( String key, TypeReference<S> type ) {
		return (Map<?, ?>)unmarshallValue( getValue( key ), type );
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
	protected <S> void setBean( String key, S value ) {
		setValue( key, marshallValue( value ) );
	}

	/**
	 * Override this method to optimize storing array values.
	 *
	 * @param key The settings value key
	 * @param array The settings value
	 */
	protected void setArray( String key, Object[] array ) {
		setValue( key, marshallValue( array ) );
	}

	/**
	 * Override this method to optimize storing collection values.
	 *
	 * @param key The settings value key
	 * @param collection The settings value
	 */
	protected void setCollection( String key, Collection<?> collection ) {
		setValue( key, marshallValue( collection ) );
	}

	/**
	 * Override this method to optimize storing map values.
	 *
	 * @param key The settings value key
	 * @param map The settings value
	 */
	protected void setMap( String key, Map<?, ?> map ) {
		setValue( key, marshallValue( map ) );
	}

	protected String marshallValue( Object value ) {
		try {
			return new ObjectMapper().writeValueAsString( value );
		} catch( JsonProcessingException exception ) {
			log.atWarning().withCause( exception ).log( "Error marshalling value" );
			return null;
		}
	}

	protected <T> T unmarshallValue( String value, TypeReference<T> type ) {
		if( value == null ) return null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readerFor( mapper.constructType( type.getType() ) ).readValue( value );
		} catch( IOException exception ) {
			log.atWarning().withCause( exception ).log( "Error unmarshalling value" );
			return null;
		}
	}

	protected boolean isDeleted() {
		return deleted;
	}

	protected void setDeleted() {
		this.deleted = true;
	}

	@Override
	public Settings getNode( String parent, String name ) {
		return getNode( PathUtil.resolve( parent, name ) );
	}

	@Override
	public Map<?, ?> getDefaultValues() {
		return defaultValues;
	}

	@Override
	public void setDefaultValues( Map<?, ?> defaultValues ) {
		this.defaultValues = defaultValues == null ? null : Collections.unmodifiableMap( defaultValues );
	}

	@Override
	public void loadDefaultValues( Object source, String path ) throws IOException {
		Properties properties = new Properties();
		InputStream defaultSettingsInput = source.getClass().getResourceAsStream( path );
		if( defaultSettingsInput != null ) properties.load( new InputStreamReader( defaultSettingsInput, TextUtil.CHARSET ) );
		setDefaultValues( properties );
	}

	@Override
	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {return eventHub.register( type, handler );}

	@Override
	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {return eventHub.unregister( type, handler );}

	@Override
	public void register( String key, EventHandler<SettingsEvent> handler ) {
		valueChangeHandlers.computeIfAbsent( key, ( k ) -> new CopyOnWriteArraySet<>() ).add( handler );
	}

	@Override
	public void unregister( String key, EventHandler<? extends SettingsEvent> handler ) {
		valueChangeHandlers.getOrDefault( key, Set.of() ).remove( handler );
	}

	@Override
	public Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		return eventHub.getEventHandlers();
	}

	String getNodePath( String root, String path ) {
		return PathUtil.normalize( PathUtil.isAbsolute( path ) ? path : PathUtil.resolve( root, path ) );
	}

	private void dispatch( SettingsEvent event ) {
		valueChangeHandlers.getOrDefault( event.getKey(), Set.of() ).forEach( h -> h.handle( event ) );
	}

	private interface OutboundConverter {

		String convert( Object value );

	}

	private interface InboundConverter {

		Object convert( String value );

	}

}
