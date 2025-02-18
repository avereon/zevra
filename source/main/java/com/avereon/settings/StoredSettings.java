package com.avereon.settings;

import com.avereon.util.DelayedAction;
import com.avereon.util.FileUtil;
import com.avereon.util.PathUtil;
import com.avereon.util.TypeReference;
import lombok.CustomLog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;

@CustomLog
public class StoredSettings extends AbstractSettings {

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
		if( root.settings.get( nodePath ) != null ) return true;

		// Check the file system
		Path folder = root.folder.resolve( nodePath.substring( 1 ) );
		return Files.exists( folder );
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
			log.atFiner().log( "node=%s", nodePath );
			node = new StoredSettings( root, nodePath, folder, values, action.getExecutor() );
		}
		// Create missing parents
		String parentPath = PathUtil.getParent( nodePath );
		folder = folder.getParent();
		while( parentPath != null && root.settings.get( parentPath ) == null ) {
			log.atFiner().log( "parent=%s", parentPath );
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
				log.atWarning().withCause( exception ).log( "Unable to list paths: %s", folder );
			}
		}

		String nodePath = this.path;
		int length = PathUtil.ROOT.equals( nodePath ) ? 1 : nodePath.length() + 1;
		List<String> internalNames = root.settings.keySet().stream().filter( k -> !k.equals( nodePath ) ).filter( k -> k.startsWith( nodePath ) ).map( n -> {
			// Parse out the child node name
			int index = n.indexOf( "/", length + 1 );
			if( index < 0 ) return n.substring( length );
			return n.substring( length, index );
		} ).distinct().toList();

		return Stream.concat( externalNames.stream(), internalNames.stream() ).distinct().toList();
	}

	@Override
	public Set<String> getKeys() {
		Set<String> keys = new HashSet<>();
		for( Object key : values.keySet() ) {
			keys.add( key.toString() );
		}
		return keys;
	}

	public long getMinFlushLimit() {
		return action.getMinTriggerLimit();
	}

	public void setMinFlushLimit( long limit ) {
		action.setMinTriggerLimit( limit );
	}

	public long getMaxFlushLimit() {
		return action.getMaxTriggerLimit();
	}

	public void setMaxFlushLimit( long limit ) {
		action.setMaxTriggerLimit( limit );
	}

	@Override
	protected void setValue( String key, String value ) {
		if( value == null ) {
			values.remove( key );
			Path path = getJsonFilePath( key );
			if( Files.exists( path ) ) {
				try {
					Files.delete( path );
				} catch( IOException exception ) {
					log.atError( exception ).log( "Error removing value at=%s", path );
				}
			}
		} else {
			values.setProperty( key, value );
		}
		action.request();
	}

	@Override
	protected String getValue( String key ) {
		return values.getProperty( key );
	}

	@Override
	protected <S> S getBean( String key, TypeReference<S> type ) {
		return load( key, type, () -> super.getBean( key, type ) );
	}

	@Override
	protected <S> void setBean( String key, S bean ) {
		save( key, bean );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	protected <S> Object[] getArray( String key, TypeReference<S> type ) {
		return (Object[])load( key, type, () -> (S)super.getArray( key, type ) );
	}

	@Override
	protected void setArray( String key, Object[] array ) {
		save( key, array );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	protected <S> Collection<?> getCollection( String key, TypeReference<S> type ) {
		return (Collection<?>)load( key, type, () -> (S)super.getCollection( key, type ) );
	}

	@Override
	protected void setCollection( String key, Collection<?> collection ) {
		save( key, collection );
	}

	@Override
	@SuppressWarnings( "unchecked" )
	protected <S> Map<?, ?> getMap( String key, TypeReference<S> type ) {
		return (Map<?, ?>)load( key, type, () -> (S)super.getMap( key, type ) );
	}

	@Override
	protected void setMap( String key, Map<?, ?> map ) {
		save( key, map );
	}

	protected <S> S load( String key, TypeReference<S> type, Supplier<S> supplier ) {
		Path path = getJsonFilePath( key );
		if( !Files.exists( path ) ) return supplier.get();
		try {
			String value = FileUtil.load( path );
			return unmarshallValue( value, type );
		} catch( IOException exception ) {
			log.atError( exception ).log( "Error loading value at=%s", path );
		}
		return null;
	}

	private void save( String key, Object value ) {
		Path path = getJsonFilePath( key );
		try {
			Files.createDirectories( path.getParent() );
			FileUtil.save( marshallValue( value ), path );
		} catch( IOException exception ) {
			log.atError( exception ).log( "Error saving value at=%s", path );
		}
	}

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
				log.atWarning().log( "Unable to delete file=%s", file );
			}

			// Delete the folder if empty
			if( getNodes().isEmpty() ) FileUtil.delete( folder );
		} catch( IOException exception ) {
			log.atSevere().withCause( exception ).log( "Unable to delete settings folder=%s", folder );
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
			log.atSevere().withCause( exception ).log( "Error loading settings file=%s", file );
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
			log.atSevere().withCause( exception ).log( "Error saving settings file=%s", file );
		}
	}

	private Path getFile() {
		return folder.resolve( SETTINGS_FILE_NAME );
	}

	private Path getJsonFilePath( String key ) {
		return folder.resolve( key + ".json" );
	}

}
