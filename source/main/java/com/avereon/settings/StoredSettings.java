package com.avereon.settings;

import com.avereon.util.DelayedAction;
import com.avereon.util.FileUtil;
import com.avereon.util.Log;
import com.avereon.util.PathUtil;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StoredSettings extends AbstractSettings {

	private static final System.Logger log = Log.get();

	/**
	 * Data will be persisted at most this often.
	 */
	private static final long MIN_PERSIST_LIMIT = 1000;

	/**
	 * Data will be persisted at least this often.
	 */
	private static final long MAX_PERSIST_LIMIT = 5000;

	private static final String SETTINGS_EXTENSION = ".properties";

	private static final String SETTINGS_FILE_NAME = "settings" + SETTINGS_EXTENSION;

	private final DelayedAction action;

	// Settings map store in root node
	private Map<String, StoredSettings> settings;

	private final StoredSettings root;

	private final String path;

	private final Path folder;

	private final Properties values;

	public StoredSettings( Path folder ) {
		this( folder, null );
	}

	private StoredSettings( Path folder, ExecutorService executor ) {
		this( null, "/", folder, null, executor );
	}

	private StoredSettings( StoredSettings root, String path, Path folder, Map<String, String> values, ExecutorService executor ) {
		if( folder.getFileName().toString().equals( SETTINGS_FILE_NAME ) ) throw new RuntimeException( "Folder ends with " + SETTINGS_FILE_NAME );
		if( root == null ) {
			this.settings = new ConcurrentHashMap<>();
			this.root = this;
		} else {
			this.root = root;
		}
		this.path = path;
		this.folder = folder;
		this.values = new Properties();
		if( values != null ) this.values.putAll( values );
		this.root.settings.put( path, this );

		this.action = new DelayedAction( executor, this::save );
		this.action.setMinTriggerLimit( MIN_PERSIST_LIMIT );
		this.action.setMaxTriggerLimit( MAX_PERSIST_LIMIT );

		load();
	}

	@Override
	public String getName() {
		return path.equals( PathUtil.ROOT ) ? PathUtil.EMPTY : folder.getFileName().toString();
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean nodeExists( String path ) {
		String nodePath = getNodePath( this.path, path );
		return root.settings.get( nodePath ) != null;
	}

	/**
	 * This checks if the node folder exists for the specified settings path. It is possible, for new nodes or recently removed nodes, that this this method
	 * return a different value than {@link #nodeExists} if the changes have not yet been
	 * flushed.
	 *
	 * @param path The child node settings path
	 * @return True if the node folder exists, false otherwise
	 */
	public boolean fileExists( String path ) {
		String nodePath = getNodePath( this.path, path );
		// The substring(1) removes the leading slash
		return Files.exists( root.folder.resolve( nodePath.substring( 1 ) ) );
	}

	@Override
	public Settings getNode( String path ) {
		return getNode( path, (Map<String, String>)null );
	}

	@Override
	public Settings getNode( String path, Map<String, String> values ) {
		// Get or create settings node
		String nodePath = getNodePath( this.path, path );
		Settings node = root.settings.get( nodePath );
		Path folder = root.folder.resolve( nodePath.substring( 1 ) );
		if( node == null ) {
			log.log( Log.TRACE, "node=" + nodePath );
			node = new StoredSettings( root, nodePath, folder, values, action.getExecutor() );
		}

		// Create missing parents
		String parentPath = PathUtil.getParent( nodePath );
		folder = folder.getParent();
		while( parentPath != null && root.settings.get( parentPath ) == null ) {
			log.log( Log.TRACE, "node=" + parentPath );
			new StoredSettings( root, parentPath, folder, null, action.getExecutor() );
			parentPath = PathUtil.getParent( parentPath );
			folder = folder.getParent();
		}

		return node;
	}

	@Override
	public List<String> getNodes() {
		List<String> externalNames = new CopyOnWriteArrayList<>();
		if( Files.exists( folder ) ) {
			try( Stream<Path> list = Files.list( folder ) ) {
				list.parallel().filter( Files::isDirectory ).forEach( path -> externalNames.add( path.getFileName().toString() ) );
			} catch( IOException exception ) {
				log.log( Log.WARN, "Unable to list paths: " + folder, exception );
			}
		}

		String nodePath = this.path;
		int length = PathUtil.ROOT.equals( nodePath ) ? 1 : nodePath.length() + 1;
		List<String> internalNames = root.settings.keySet().stream().filter( k -> !k.equals( nodePath ) ).filter( k -> k.startsWith( nodePath ) ).map( n -> {
			// Parse out the child node name
			int index = n.indexOf( "/", length + 1 );
			if( index < 0 ) return n.substring( length );
			return n.substring( length, index );
		} ).distinct().collect( Collectors.toList() );

		return Stream.concat( externalNames.stream(), internalNames.stream() ).distinct().collect( Collectors.toList() );
	}

	@Override
	public Set<String> getKeys() {
		Set<String> keys = new HashSet<>();
		for( Object key : values.keySet() ) {
			keys.add( key.toString() );
		}
		return keys;
	}

	@Override
	protected void setValue( String key, String value ) {
		if( value == null ) {
			values.remove( key );
		} else {
			values.setProperty( key, value );
		}
		action.update();
	}

	@Override
	protected String getValue( String key ) {
		return values.getProperty( key );
	}

	// NEXT Override setArray and/or setCollection to store state in individual files

	// NEXT Override getArray and/or getCollection to retrieve state from individual files

	@Override
	public Settings flush() {
		action.trigger();
		return this;
	}

	@Override
	public synchronized Settings delete() {
		action.cancel();
		setDeleted();
		getNodes().stream().map( this::getNode ).forEach( Settings::delete );

		try {
			// Delete the file
			Path file = getFile();
			if( !FileUtil.delete( file ) ) {
				log.log( Log.WARN, "Unable to delete file=" + file );
			}

			// Delete the folder if empty
			if( getNodes().size() == 0 ) FileUtil.delete( folder );
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Unable to delete settings folder: " + folder, exception );
		} finally {
			root.settings.remove( getPath() );
		}

		return this;
	}

	@Override
	public String toString() {
		return folder.toString();
	}

	private synchronized void load() {
		Path file = getFile();
		if( !Files.exists( file ) || Files.isDirectory( file ) ) return;
		try( FileInputStream input = new FileInputStream( file.toFile() ) ) {
			values.load( input );
			getEventHub().dispatch( new SettingsEvent( this, SettingsEvent.LOADED, getPath() ) );
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Error loading settings file: " + file, exception );
		}
	}

	private synchronized void save() {
		if( isDeleted() ) return;

		Path file = getFile();
		try {
			Files.createDirectories( file.getParent() );
			try( FileOutputStream output = new FileOutputStream( file.toFile() ) ) {
				values.store( output, null );
				getEventHub().dispatch( new SettingsEvent( this, SettingsEvent.SAVED, getPath() ) );
			}
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Error saving settings file: " + file, exception );
		}
	}

	private Path getFile() {
		return folder.resolve( SETTINGS_FILE_NAME );
	}

}
