package com.avereon.settings;

import com.avereon.util.PathUtil;
import com.avereon.util.TextUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapSettings extends AbstractSettings {

	// The map of settings. Should only be stored in the root node
	private Map<String, Settings> settings;

	private MapSettings root;

	private String path;

	// The settings node values
	private Map<String, String> values;

	// The settings defaults.
	private Map<String, Object> defaultValues;

	public MapSettings() {
		this( null, "/", new HashMap<>() );
	}

	private MapSettings( MapSettings root, String path, Map<String, String> values ) {
		if( root == null ) {
			this.settings = new ConcurrentHashMap<>();
			this.root = this;
		} else {
			this.root = root;
		}
		this.path = path;
		this.values = new ConcurrentHashMap<>();
		if( values != null ) this.values.putAll( values );
		this.root.settings.put( path, this );
	}

	@Override
	public String getName() {
		return path.equals( PathUtil.ROOT ) ? PathUtil.EMPTY : PathUtil.getName( path );
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean exists( String path ) {
		return root.settings.get( getNodePath( this.path, path ) ) != null;
	}

	@Override
	public Settings getNode( String path ) {
		return getNode( path, (Map<String, String>)null );
	}

	@Override
	public Settings getNode( String path, Map<String, String> values ) {
		String nodePath = getNodePath( this.path, path );

		// Get or create settings node
		Settings child = root.settings.get( nodePath );

		if( child == null ) child = new MapSettings( root, nodePath, values );

		return child;
	}

	@Override
	public List<String> getNodes() {
		List<String> children = new ArrayList<>();

		for( String childPath : root.settings.keySet() ) {
			if( !childPath.startsWith( path ) ) continue;
			String child = PathUtil.getChild( path, childPath );
			if( !TextUtil.isEmpty( child ) && !children.contains( child ) ) children.add( child );
		}

		return children;
	}

	@Override
	public Set<String> getKeys() {
		return values.keySet();
	}

	@Override
	protected void setValue( String key, String value ) {
		if( value == null ) {
			values.remove( key );
		} else {
			values.put( key, value );
		}
	}

	@Override
	protected String getValue( String key ) {
		return values.get( key );
	}

	@Override
	public void flush() {}

	@Override
	public void delete() {
		root.settings.remove( getPath() );
	}

}
