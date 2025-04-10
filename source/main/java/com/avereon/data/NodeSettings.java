package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.TypeReference;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The NodeSettings class links data {@link Node} properties to the {@link
 * Settings} interface. In particular, this allows any property on a data node
 * to be treated as a setting.
 */
public class NodeSettings implements Settings {

	private final Node node;

	private final EventHub eventHub;

	public NodeSettings( Node node ) {
		this.node = node;
		this.eventHub = new EventHub();
		node.register( NodeEvent.VALUE_CHANGED, e -> {
			if( e.getNode() == node ) eventHub.dispatch( new SettingsEvent( this, SettingsEvent.CHANGED, this.getPath(), e.getKey(), e.getOldValue(), e.getNewValue() ) );
		} );
	}

	@Override
	public String getName() {
		return node.getCollectionId();
	}

	@Override
	public String getPath() {
		return getName();
	}

	@Override
	public boolean nodeExists( String path ) {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	@Override
	public Settings getNode( String path ) {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	@Override
	public Settings getNode( String parent, String name ) {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	@Override
	public Settings getNode( String path, Map<String, String> values ) {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	@Override
	public List<String> getNodes() {
		throw new UnsupportedOperationException( "Child settings not supported" );
	}

	@Override
	public Set<String> getKeys() {
		return node.getValueKeys();
	}

	@Override
	public boolean exists( String key ) {
		return node.hasKey( key );
	}

	@Override
	public <T> T get( String key, TypeReference<T> type, T defaultValue ) {
		T value = node.getValue( key );
		return value == null ? defaultValue : value;
	}

	@Override
	public Settings set( String key, Object value ) {
		node.setValue( key, value );
		return this;
	}

	@Override
	public Settings copyFrom( Settings settings ) {
		throw new UnsupportedOperationException( "Copy not supported" );
	}

	@Override
	public Settings remove( String key ) {
		node.setValue( key, null );
		return this;
	}

	@Override
	public Settings flush() {
		return this;
	}

	@Override
	public Settings delete() {
		throw new UnsupportedOperationException( "Delete not supported" );
	}

	@Override
	public Map<?, ?> getDefaultValues() {
		throw new UnsupportedOperationException( "Default values not supported" );
	}

	@Override
	public void setDefaultValues( Map<?, ?> defaults ) {
		throw new UnsupportedOperationException( "Default values not supported" );
	}

	@Override
	public void loadDefaultValues( Object source, String path ) {
		throw new UnsupportedOperationException( "Default values not supported" );
	}

	@Override
	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		eventHub.register( type, handler );
		return eventHub;
	}

	@Override
	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		eventHub.unregister( type, handler );
		return eventHub;
	}

	@Override
	public void register( String key, EventHandler<SettingsEvent> handler ) {
		throw new UnsupportedOperationException( "Use Node.register() instead" );
	}

	@Override
	public void unregister( String key, EventHandler<? extends SettingsEvent> handler ) {
		throw new UnsupportedOperationException( "Use Node.unregister() instead" );
	}

	@Override
	public Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		return eventHub.getEventHandlers();
	}

}
