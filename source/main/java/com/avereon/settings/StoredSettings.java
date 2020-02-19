package com.avereon.settings;

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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class StoredSettings extends AbstractSettings {

	private static final System.Logger log = Log.get();

	/**
	 * Data will be persisted at most this fast.
	 */
	private static final long MIN_PERSIST_LIMIT = 100;

	/**
	 * Data will be persisted at least this often.
	 */
	private static final long MAX_PERSIST_LIMIT = 5000;

	private static final String SETTINGS_EXTENSION = ".properties";

	private static final String SETTINGS_FILE_NAME = "settings" + SETTINGS_EXTENSION;

	private static Timer timer = new Timer( StoredSettings.class.getSimpleName(), true );

	// Settings map store in root node
	private Map<String, StoredSettings> settings;

	private ExecutorService executor;

	private StoredSettings root;

	private String path;

	private Path folder;

	private Properties values;

	private AtomicLong lastDirtyTime = new AtomicLong();

	private AtomicLong lastValueTime = new AtomicLong();

	private AtomicLong lastStoreTime = new AtomicLong();

	private final Object scheduleLock = new Object();

	private SaveTask task;

	public StoredSettings( Path folder ) {
		this( folder, null );
	}

	private StoredSettings( Path folder, ExecutorService executor ) {
		this( null, "/", folder, null, executor );
	}

	private StoredSettings( StoredSettings root, String path, Path folder, Map<String, String> values, ExecutorService executor ) {
		if( folder.getFileName().equals( SETTINGS_FILE_NAME ) ) throw new RuntimeException( "Folder ends with " + SETTINGS_FILE_NAME );
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
		this.executor = executor;
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
	public boolean exists( String path ) {
		String nodePath = getNodePath( this.path, path );
		return root.settings.get( nodePath ) != null;
	}

	/**
	 * This checks if the node folder exists for the specified settings path. It
	 * is possible, for new nodes or recently removed nodes, that this this method
	 * return a different value than {@link #exists} if the changes have not yet
	 * been flushed.
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
		String nodePath = getNodePath( this.path, path );

		// Get or create settings node
		Settings child = root.settings.get( nodePath );
		// The substring(1) removes the leading slash
		if( child == null ) child = new StoredSettings( root, nodePath, root.folder.resolve( nodePath.substring( 1 ) ), values, executor );

		return child;
	}

	@Override
	public List<String> getNodes() {
		List<String> names = new CopyOnWriteArrayList<>();

		if( !Files.exists( folder ) ) return names;
		try( Stream<Path> list = Files.list( folder ) ) {
			list.parallel().forEach( path -> names.add( path.getFileName().toString() ) );
		} catch( IOException exception ) {
			log.log( Log.WARN, "Unable to list paths: " + folder, exception );
		}

		return names;
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

		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave( false );
	}

	@Override
	protected String getValue( String key ) {
		return values.getProperty( key );
	}

	// NEXT Override setArray and/or setCollection to store state in individual files

	// NEXT Override getArray and/or getCollection to retrieve state from individual files

	@Override
	public Settings flush() {
		scheduleSave( true );
		return this;
	}

	@Override
	public synchronized Settings delete() {
		synchronized( scheduleLock ) {
			if( task != null ) task.cancel();
		}

		root.settings.remove( getPath() );
		try {
			// Delete the file
			FileUtil.delete( getFile() );

			// Delete the folder if empty
			if( getNodes().size() == 0 ) FileUtil.delete( folder );
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Unable to delete settings folder: " + folder, exception );
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
		Path file = getFile();
		try {
			Files.createDirectories( file.getParent() );
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Error saving settings file: " + file, exception );
		}
		try( FileOutputStream output = new FileOutputStream( file.toFile() ) ) {
			values.store( output, null );
			lastStoreTime.set( System.currentTimeMillis() );
			getEventHub().dispatch( new SettingsEvent( this, SettingsEvent.SAVED, getPath() ) );
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Error saving settings file: " + file, exception );
		}
	}

	private void scheduleSave( boolean immediate ) {
		synchronized( scheduleLock ) {
			long storeTime = lastStoreTime.get();
			long dirtyTime = lastDirtyTime.get();

			// If there are no changes since the last store time just return
			if( !immediate && (dirtyTime < storeTime) ) return;

			long valueTime = lastValueTime.get();
			long softNext = valueTime + MIN_PERSIST_LIMIT;
			long hardNext = Math.max( dirtyTime, storeTime ) + MAX_PERSIST_LIMIT;
			long nextTime = Math.min( softNext, hardNext );
			long taskTime = task == null ? 0 : task.scheduledExecutionTime();

			// If the existing task time is already set to the next time just return
			if( !immediate && (taskTime == nextTime) ) return;

			// Cancel the existing task and schedule a new one
			if( task != null ) task.cancel();
			task = new SaveTask();
			if( immediate ) {
				task.run();
			} else {
				timer.schedule( task, new Date( nextTime ) );
			}
		}
	}

	private Path getFile() {
		return folder.resolve( SETTINGS_FILE_NAME );
	}

	private class SaveTask extends TimerTask {

		@Override
		public void run() {
			// If there is an executor, use it to run the task, otherwise run the task on the timer thread
			if( executor != null && !executor.isShutdown() ) {
				executor.submit( StoredSettings.this::save );
			} else {
				save();
			}
		}

	}

}
