package com.xeomar.settings;

import com.xeomar.util.FileUtil;
import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

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

	/**
	 * Data will be persisted at most this fast.
	 */
	private static final long MIN_PERSIST_LIMIT = 100;

	/**
	 * Data will be persisted at least this often.
	 */
	private static final long MAX_PERSIST_LIMIT = 5000;

	private static final String SETTINGS_EXTENSION = ".properties";

	private static Logger log = LogUtil.get( StoredSettings.class );

	private static Timer timer = new Timer( StoredSettings.class.getSimpleName(), true );

	// Settings map store in root node
	private Map<String, StoredSettings> settings;

	private ExecutorService executor;

	private StoredSettings root;

	private String path;

	private Path folder;

	private Properties values;

	private Map<String, String> defaultValues;

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
		return folder.getFileName().toString();
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean exists( String path ) {
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
			log.warn( "Unable to list paths: " + folder, exception );
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
	public void set( String key, Object value ) {
		String oldValue = values.getProperty( key );
		String newValue = value == null ? null : String.valueOf( value );
		if( value == null ) {
			values.remove( key );
		} else {
			values.setProperty( key, newValue );
		}
		if( !Objects.equals( oldValue, value ) ) fireEvent( new SettingsEvent( this, SettingsEvent.Type.UPDATED, getPath(), key, oldValue, newValue ) );

		lastValueTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastStoreTime.get() ) lastDirtyTime.set( lastValueTime.get() );
		scheduleSave( false );
	}

	@Override
	@Deprecated
	public String get( String key ) {
		return get( key, null );
	}

	@Override
	@Deprecated
	public String get( String key, Object defaultValue ) {
		String value = values.getProperty( key );
		if( value == null && defaultValues != null ) value = defaultValues.get( key );
		if( value == null ) value = defaultValue == null ? null : defaultValue.toString();
		return value;
	}

	public Map<String, String> getDefaultValues() {
		return defaultValues;
	}

	public void setDefaultValues( Map<String, String> settings ) {
		this.defaultValues = settings;
	}

	@Override
	public void flush() {
		scheduleSave( true );
	}

	@Override
	public synchronized void delete() {
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
			log.error( "Unable to delete settings folder: " + folder, exception );
		}
	}

	@Override
	public String toString() {
		return folder.toString();
	}

	private synchronized void load() {
		Path path = getFile();
		if( !Files.exists( path ) ) return;
		try( FileInputStream input = new FileInputStream( path.toFile() ) ) {
			values.load( input );
			fireEvent( new SettingsEvent( this, SettingsEvent.Type.LOADED, getPath() ) );
		} catch( IOException exception ) {
			log.error( "Error loading settings file: " + path, exception );
		}
	}

	private synchronized void save() {
		Path path = getFile();
		try {
			Files.createDirectories( path.getParent() );
		} catch( IOException exception ) {
			log.error( "Error saving settings file: " + path, exception );
		}
		try( FileOutputStream output = new FileOutputStream( path.toFile() ) ) {
			values.store( output, null );
			lastStoreTime.set( System.currentTimeMillis() );
			fireEvent( new SettingsEvent( this, SettingsEvent.Type.SAVED, getPath() ) );
		} catch( IOException exception ) {
			log.error( "Error saving settings file: " + path, exception );
		}
	}

	private void scheduleSave( boolean force ) {
		synchronized( scheduleLock ) {
			long storeTime = lastStoreTime.get();
			long dirtyTime = lastDirtyTime.get();

			// If there are no changes since the last store time just return
			if( !force && (dirtyTime < storeTime) ) return;

			long valueTime = lastValueTime.get();
			long softNext = valueTime + MIN_PERSIST_LIMIT;
			long hardNext = Math.max( dirtyTime, storeTime ) + MAX_PERSIST_LIMIT;
			long nextTime = Math.min( softNext, hardNext );
			long taskTime = task == null ? 0 : task.scheduledExecutionTime();

			// If the existing task time is already set to the next time just return
			if( !force && (taskTime == nextTime) ) return;

			// Cancel the existing task and schedule a new one
			if( task != null ) task.cancel();
			task = new SaveTask();
			if( force ) {
				task.run();
			} else {
				timer.schedule( task, new Date( nextTime ) );
			}
		}
	}

	private Path getFile() {
		return folder.resolve( "settings" + SETTINGS_EXTENSION );
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
