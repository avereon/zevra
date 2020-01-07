package com.avereon.settings;

import com.avereon.event.EventHandler;
import com.avereon.util.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A node in a hierarchical collection of settings data. This class allows
 * programs to store and retrieve settings data. The data are stored according
 * to the implementation of each subclass. Typical implementations include
 * in-memory maps, file systems and databases. The user of this class should
 * choose an implementation according the needs of the program.
 * <p>
 * Nodes in a settings tree are named in a fashion similar to files in a
 * hierarchical file system. Every node in a settings tree has a unique
 * absolute path and a name (which does not need to be unique). The root node
 * has the name of the empty string (""). Every other node has a name,
 * specified at the time it is created. The only restrictions on the name are
 * that it cannot be empty and it cannot contain the slash character ('/').
 * <p>
 * A node path name relative to its ancestor is simply the string that must be
 * appended to the ancestor's absolute path name in order to form the node's
 * absolute path name, with the initial slash character,if present, removed.
 * Note that:
 * <ul>
 * <li>No relative path names begin with the slash character.</li>
 * <li>Every node's path name relative to itself is the empty string.</li>
 * <li>Every node's path name relative to its parent is its node name (except
 * for the root node, which does not have a parent).</li>
 * <li>Every node's path name relative to the root is its absolute path name
 * with the initial slash character removed.</li>
 * </ul>
 * Also note that:
 * <ul>
 * <li>No path name contains multiple consecutive slash characters.</li>
 * <li>No path name with the exception of the root's absolute path name ends
 * in the slash character.</li>
 * <li>Any string that conforms to these two rules is a valid path name.</li>
 * </ul>
 * <p>
 * All of the methods that modify settings may operate asynchronously; meaning
 * they may return immediately, and changes will eventually propagate to the
 * backing store with an implementation specific delay. The flush method may be
 * used to synchronously force updates to the backing store. Normal termination
 * of the Java Virtual Machine will not result in the loss of pending updates,
 * therefore, an explicit flush invocation is not required upon termination to
 * ensure that pending updates are made persistent.
 * <p>
 * Settings keys are always strings. There are four categories of supported
 * value types: simple (boolean, character, byte, short, integer, long, float
 * double and string), Java beans, arrays and collections. Arrays and
 * collections are expected to contain beans. All values are stored in a
 * marshalled state using an implementation specific strategy. Typical
 * strategies are JSON and XML.
 * <p>
 * The get method will always return a string. If the original stored value was
 * simple then the returned value will simply be the string representation of
 * that value. If the original value was a bean, array or collection then the
 * marshalled string will be returned. In order to return an unmarshalled value
 * the expanded get with a result type must be used. This method can also be
 * used to convert between simple types like converting a string to a double.
 */
public interface Settings {

	/**
	 * Get the name of the settings node.
	 *
	 * @return The name of the settings node
	 */
	String getName();

	/**
	 * Get the absolute path of the settings node.
	 *
	 * @return The absolute path of the settings node
	 */
	String getPath();

	/**
	 * Check if the node at the specified path exists.
	 *
	 * @param path The node path to check
	 * @return True if the node exists, false otherwise
	 */
	boolean exists( String path );

	/**
	 * Get a settings object for the specified path. If the path starts with the separator character then the path is absolute. If the path does not start with the separator character then the path is relative to this settings node.
	 * <p>
	 * Multiple requests from the same settings tree using the same path return the same settings object.
	 *
	 * @param path The requested path
	 * @return The settings object for the path
	 */
	Settings getNode( String path );

	/**
	 * Get a settings object for the specified parent path and name. If the path starts with the separator character then the path is absolute. If the path does not start with the separator character then the path is relative to this settings
	 * node.
	 * <p>
	 * Multiple requests from the same settings tree using the same path return the same settings object.
	 *
	 * @param parent The parent path
	 * @param name The node name
	 * @return The settings object for the path
	 */
	Settings getNode( String parent, String name );

	/**
	 * Get a settings object for the specified path according to {@link #getNode(String)} with the specified values.
	 *
	 * @param path The requested path
	 * @param values The initial values of the settings
	 * @return The settings object for the path
	 */
	Settings getNode( String path, Map<String, String> values );

	/**
	 * Get the existing child node names of this settings node.
	 *
	 * @return The names of the existing child nodes
	 */
	List<String> getNodes();

	/**
	 * Get the value keys for this settings node.
	 *
	 * @return The value keys for this settings node
	 */
	Set<String> getKeys();

	/**
	 * Shortcut to calling get( key, String.class ). Calling this method for
	 * non-simple values is undefined.
	 *
	 * @param key The value key
	 * @return The value as a string
	 */
	String get( String key );

	/**
	 * Shortcut to calling get( key, String.class ). Calling this method for
	 * non-simple values is undefined.
	 *
	 * @param key The value key
	 * @param defaultValue The default value if the setting value is null
	 * @return The value as a string
	 */
	String get( String key, String defaultValue );

	/**
	 * Get a value from the settings node. This method is useful for specifying
	 * that the return type be a simple data type.
	 * <p>
	 * Example:
	 * <pre>
	 * int count = settings.get( "count", Integer.class );
	 * </pre>
	 *
	 * @param key The key of the setting value
	 * @param type The class type of the return value
	 * @param <T> The class type of the return value
	 * @return The setting value
	 */
	<T> T get( String key, Class<T> type );

	<T> T get( String key, Class<T> type, T defaultValue );

	/**
	 * Get a value from the settings node. This method is needed for specifying
	 * a return type of a collection using generic specification.
	 * <p>
	 * Example:
	 * <pre>
	 * Map&lt;String,PojoBean&gt; beans = settings.get( "beans", new TypeReference&lt;Map&lt;String,PojoBean&gt;&gt;(){} );
	 * </pre>
	 *
	 * @param key The key of the setting value
	 * @param type The class type of the return value
	 * @param <T> The class type of the return value
	 * @return The setting value
	 */
	<T> T get( String key, TypeReference<T> type );

	/**
	 * Get a value from the settings node or the default value if the settings value is null.
	 *
	 * @param key The key of the setting value
	 * @param type The class type of the return value
	 * @param defaultValue The default value if the setting value is null
	 * @param <T> The class type of the return value
	 * @return The setting value
	 */
	<T> T get( String key, TypeReference<T> type, T defaultValue );

	/**
	 * Set a value in the settings node.
	 *
	 * @param key The value key
	 * @param value The value
	 */
	Settings set( String key, Object value );

	/**
	 * Copy the values from the specified settings to this settings.
	 *
	 * @param settings The setting from which to get values
	 * @return This settings object
	 */
	Settings copyFrom( Settings settings );

	/**
	 * Remove a value from the settings node. This is the same as setting the
	 * value to null.
	 *
	 * @param key The value key
	 */
	Settings remove( String key );

	/**
	 * Flush the settings values. For settings implementations that store values this method should be used to store the values promptly.
	 */
	Settings flush();

	/**
	 * Delete this settings node.
	 */
	Settings delete();

	/**
	 * Get the default values for this settings node.
	 *
	 * @return The default values map
	 */
	Map<String, Object> getDefaultValues();

	/**
	 * Set the default values for this settings node.
	 *
	 * @param defaults The default values map
	 */
	void setDefaultValues( Map<String, Object> defaults );

	/**
	 * Add a settings listener to this node. The settings listener will not receive event from child nodes.
	 *
	 * @param listener The settings listener
	 */
	void addSettingsListener( EventHandler<SettingsEvent> listener );

	/**
	 * Remove a settings listener from this node.
	 *
	 * @param listener The settings listener
	 */
	void removeSettingsListener( EventHandler<SettingsEvent> listener );

	@SuppressWarnings( "unused" )
	static void print( Settings settings ) {
		System.out.println( "settings( " + settings.getPath() + " ) {" );
		for( String key : settings.getKeys() ) {
			System.out.println( "  " + key + " = " + settings.get( key ) );
		}
		System.out.println( "}" );
	}

}
