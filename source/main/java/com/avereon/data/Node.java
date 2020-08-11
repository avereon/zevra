package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import com.avereon.transaction.*;
import com.avereon.util.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A generic data node supporting getting and setting values, a modified (or
 * dirty) flag, {@link NodeEvent events} and {@link Txn transactions}. It is
 * expected that this class be inherited and that sub-classes will be configured
 * to represent specific data types. For example, consider this Person type:
 * <p>
 * <pre>
 * public class Person extends Node {
 *
 *   public Person( String id ) {
 *     definePrimaryKey( "id" );
 *     defineNaturalKey( "name" );
 *     setModifyingKeys( "name" );
 *     setValue( "id", id );
 *   }
 *
 *   public String getId() {
 *     return getValue( "id" );
 *   }
 *
 *   public String getName() {
 *     return getValue( "name" );
 *   }
 *
 *   public Person setName( String name ) {
 *     setValue( "name", name );
 *     return this;
 *   }
 *
 *   ...
 *
 * }
 * </pre>
 * <p>
 * This class defines a Person type with a primary key of &quot;id&quot; and a
 * natural key of &quot;name&quot;. Both primary keys and natural keys are use
 * in the calculation of {@link #hashCode()} and {@link #equals(Object)}. It is
 * good practice to always define a single value primary key. It is not
 * required to define a natural key.
 *
 * <h2>The Modified Flag</h2>
 * The modified flag allows users of the data type to know if an instance has
 * been modified since {@link #setModified(boolean) setModifed(false)} was last
 * called. Note that
 * only {@link #addModifyingKeys(String...) modifying values} can affect the
 * modified flag. Using the Person class
 * above, if the name is "Foo" and the modified flag is false, setting the name to
 * "Bar" will cause the modified flag to be set to true. Setting the name back to
 * "Foo" will cause the modified flag to be set back to false, since the name was
 * originally "Foo".
 *
 * <h2>Modifying Values</h2>
 * By default no values will cause the modified flag to change. In order for a
 * change in a value to cause a change in the modified flag it must be
 * configured as a modifying value with the {@link #addModifyingKeys(String...)}
 * method. Note in the example above that the "name" value was set as a
 * modifying key. This means that a change to the name value will cause the
 * modified flag to change, but changes with other values will not cause the
 * modified flag to change.
 *
 * <h2>Events</h2>
 * Events are produced for almost any action on a data node. Exactly what events
 * are produced can be rather complex depending on the data structure, the
 * transaction state and the actions taken. However, simple situations should be
 * straightforward. For example, setting a value will cause a
 * {@link NodeEvent#VALUE_CHANGED} event and a {@link NodeEvent#NODE_CHANGED}
 * event. If the value was a modifying value then a {@link NodeEvent#MODIFIED}
 * or {@link NodeEvent#UNMODIFIED} event would also be produced. Events
 * produced during an active {@link Txn transaction} are not fired until the
 * transaction is committed. Events are not fired if the transaction fails and
 * is rolled back.
 *
 * <h2>Value Events</h2>
 * Event handlers can also be registered for changes to specific values. This is
 * to handle the case where lambdas are registered for changes the the value.
 * Example:
 * <pre>
 *   person.register( "name", e -> displayPersonName( e.getNewValue() ) );
 * </pre>
 *
 * <h2>Transactions</h2>
 * Multiple changes to a data node can be grouped together using {@link Txn
 * transactions}. Using the {@link Txn} class to create and commit a transaction
 * is fairly simple:
 * <p>
 * <pre>
 *   Txn.create();
 *   person.setName( "Bar" );
 *   person.setFavoriteColor( "green" );
 *   Txn.commit();
 * </pre>
 * As noted above, events produced during an active {@link Txn transaction} are
 * not fired until the transaction is committed. Events are not fired if the
 * transaction fails and is rolled back.
 *
 * <h2>Child Data Nodes</h2>
 * Adding a Node to a Node is a common practice. This allows for structured
 * data models. This mostly affects how the modified flag and events are
 * handled. If a child node is modified, the parent node is also modified. Also,
 * some events that occur on a child node are also bubbled up to the parent
 * node. Particularly, {@link NodeEvent#VALUE_CHANGED} events are propagated to
 * parent nodes.
 */
public class Node implements TxnEventTarget, Cloneable {

	private static final System.Logger log = Log.get();

	/**
	 * A special object to represent previously null values in the modifiedValues
	 * map.
	 */
	private static final Object NULL = new Object();

	/**
	 * The node event hub.
	 */
	private final EventHub hub;

	/**
	 * The node value change handlers, a special set of handlers for value changes.
	 */
	private final Map<String, Map<EventHandler<NodeEvent>, EventHandler<NodeEvent>>> valueChangeHandlers;

	/**
	 * The node id.
	 */
	private String id;

	/**
	 * The parent of the node.
	 */
	private Node parent;

	/**
	 * The node values.
	 */
	private Map<String, Object> values;

	/**
	 * The list of value keys that specify the primary key.
	 */
	private List<String> primaryKeyList;

	/**
	 * The list of value keys that specify the natural key.
	 */
	private List<String> naturalKeyList;

	/**
	 * The set of value keys that can modify the node.
	 */
	private Set<String> modifyingKeySet;

	private boolean allKeysModify;

	/**
	 * The set of value keys that are read only.
	 */
	private Set<String> readOnlySet;

	/**
	 * The internally calculated modified flag used to allow for fast read rates.
	 * This is updated when the self modified flag, values or children are changed.
	 */
	private boolean modified;

	// The node self modified flag.
	private boolean selfModified;

	/**
	 * The map of values that are modified since the modified flag was last
	 * cleared. This map is set to null when the modified flag is cleared.
	 */
	private Map<String, Object> modifiedValues;

	/**
	 * The count of child nodes that are modified since the modified flag was last
	 * cleared. This set is set to null when the modified flag is cleared.
	 */
	private Set<Node> modifiedChildren;

	/**
	 * Create a new, generic, empty data node. It is generally expected that the
	 * Node class will be inherited, instead of used directly, but there is no
	 * restriction creating "generic" nodes.
	 */
	public Node() {
		this.hub = new EventHub();
		this.valueChangeHandlers = new ConcurrentHashMap<>();
		this.id = NodeSet.PREFIX + UUID.randomUUID().toString();
	}

	/**
	 * Is the node modified. The node is modified if any data value has been
	 * modified or any child node has been modified since the last time the
	 * modified flag was cleared.
	 *
	 * @return true if this node or any child nodes are modified, false otherwise.
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Set or clear the modified flag. Usually this method is used to clear the
	 * modified flag by setting the value to false. But it is also allowed to
	 * explicitly set the modified value to true for any reason to mark the node
	 * modified.
	 *
	 * @param newValue The new modified flag value
	 */
	public void setModified( boolean newValue ) {
		boolean oldValue = selfModified;

		try {
			Txn.create();
			Txn.submit( new SetSelfModifiedOperation( this, oldValue, newValue ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.log( Log.ERROR, "Error setting flag: modified", exception );
		}
	}

	void setAllKeysModify() {
		this.allKeysModify = true;
	}

	/**
	 * Request that a {@link NodeEvent#NODE_CHANGED} event occur. This is usually
	 * used to cause the node handlers to run if they were added after the node
	 * was changed. This is common during node initialization where the state is
	 * set, the modified flag cleared, the handlers added and then this method is
	 * called.
	 */
	public void refresh() {
		try {
			Txn.create();
			Txn.submit( new RefreshOperation( this ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.log( Log.ERROR, "Error refreshing: " + this, exception );
		}
	}

	/**
	 * Dispatch a {@link TxnEvent} to the data node. This method should not be
	 * called by other classes other than the {@link Txn transaction} classes.
	 *
	 * @param event The transaction event
	 */
	@Override
	public void dispatch( TxnEvent event ) {
		if( event instanceof NodeEvent ) dispatch( (NodeEvent)event );
	}

	/**
	 * Dispatch a {@link NodeEvent} to the data node. This method should not be
	 * called by other classes other than the {@link Node data} classes.
	 *
	 * @param event The data node event
	 */
	private void dispatch( NodeEvent event ) {
		// Dispatch to value change handlers only when the event is on itself
		boolean self = event.getNode() == this;
		boolean valueChanged = event.getEventType() == NodeEvent.VALUE_CHANGED;
		if( self && valueChanged ) {
			ConcurrentModificationException exception;
			do {
				try {
					exception = null;
					var valueHandlers = new HashMap<>( valueChangeHandlers.getOrDefault( event.getKey(), Map.of() ) );
					valueHandlers.forEach( ( k, v ) -> v.handle( event ) );
				} catch( ConcurrentModificationException cme ) {
					exception = cme;
				}
			} while( exception != null );
		}

		hub.dispatch( event );
	}

	/**
	 * Register an event handler with this data node.
	 *
	 * @param type The event type
	 * @param handler The event handler
	 * @param <T> The type of event to handle
	 * @return The data node's {@link EventHub}
	 */
	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return hub.register( type, handler );
	}

	/**
	 * Unregister an event handler from this data node.
	 *
	 * @param type The event type
	 * @param handler The event handler
	 * @param <T> The type of event to handle
	 * @return The data node's {@link EventHub}
	 */
	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return hub.unregister( type, handler );
	}

	/**
	 * Get a map of all the event handlers for this node by event type.
	 *
	 * @return A map of the event handlers keyed by event type
	 */
	Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		return hub.getEventHandlers();
	}

	/**
	 * Get this data node's {@link EventHub}
	 *
	 * @return The data node event hub
	 */
	protected EventHub getEventHub() {
		return hub;
	}

	/**
	 * Register a value changed event handler for a specific value key. This is
	 * useful to register lambda style event handlers for specific value changes.
	 * <p>
	 * NOTE: These handlers only receive value changed events that happen on this
	 * node not on any child nodes like normal listeners.
	 * </p>
	 *
	 * @param key The value key
	 * @param handler The value changed handler
	 */
	public void register( String key, EventHandler<NodeEvent> handler ) {
		valueChangeHandlers.computeIfAbsent( key, ( k ) -> new WeakHashMap<>() ).put( handler, handler );
	}

	/**
	 * Unregister a value changed event handler for a specific value key.
	 *
	 * @param key The value key
	 * @param handler The value changed handler
	 */
	public void unregister( String key, EventHandler<NodeEvent> handler ) {
		valueChangeHandlers.getOrDefault( key, Map.of() ).remove( handler );
	}

	//	public <T extends Event> void register( String key, EventType<? super T> type, EventHandler<NodeEvent> handler ) {
	//		valueChangeHandlers.computeIfAbsent( key, ( k ) -> new WeakHashMap<>() ).put( handler, handler );
	//	}
	//
	//	public <T extends Event> void unregister( String key, EventType<? super T> type, EventHandler<NodeEvent> handler ) {
	//		valueChangeHandlers.getOrDefault( key, Map.of() ).remove( handler );
	//	}

	/**
	 * Copy the values and resources from the specified node. This method will
	 * only fill in missing values and resources from the specified node.
	 *
	 * @param node The node from which to copy values and resources
	 */
	public <T extends Node> T copyFrom( Node node ) {
		return copyFrom( node, false );
	}

	/**
	 * Copy the values and resources from the specified node. If overwrite is true
	 * this method will replace any values or resources with the specified nodes
	 * values and resources. Otherwise, this method will only fill in missing
	 * values and resources from the specified node.
	 *
	 * @param node The node from which to copy values and resources
	 * @param overwrite Should the new values overwrite existing values
	 */
	@SuppressWarnings( "unchecked" )
	public <T extends Node> T copyFrom( Node node, boolean overwrite ) {
		// Clone values
		for( String key : node.getValueKeys() ) {
			if( overwrite || getValue( key ) == null ) setValue( key, node.getValue( key ) );
		}

		return (T)this;
	}

	/**
	 * Get a string representation of this node. By default this implementation
	 * only returns the primary and natural keys and values. For a full list of
	 * values use {@link #toString(boolean) toString(true)}. For a list of
	 * specific values use {@link #toString(List)}.
	 *
	 * @return A string representation of this node
	 */
	@Override
	public String toString() {
		return toString( false );
	}

	/**
	 * Get a string representation of this node, optionally including all values.
	 *
	 * @param allValues Whether to include all values or not
	 * @return A string representation of this node
	 */
	public String toString( boolean allValues ) {
		List<String> keys = new ArrayList<>();
		if( primaryKeyList != null ) keys.addAll( primaryKeyList );
		if( naturalKeyList != null ) keys.addAll( naturalKeyList );
		if( allValues ) {
			keys = new ArrayList<>();
			if( values != null ) keys.addAll( values.keySet() );
			Collections.sort( keys );
		}

		return toString( keys );
	}

	/**
	 * Get a string representation of this node but only with the requested
	 * value keys included.
	 *
	 * @param keys The value keys to include
	 * @return A string representation of this node
	 */
	public String toString( String... keys ) {
		return toString( Arrays.asList( keys ) );
	}

	/**
	 * Get a string representation of this node but only with the requested
	 * value keys included.
	 *
	 * @param keys The value keys to include
	 * @return A string representation of this node
	 */
	public String toString( List<String> keys ) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		builder.append( getClass().getSimpleName() );
		builder.append( "{" );
		if( keys != null ) {
			for( String key : keys ) {
				Object value = getValue( key );
				if( value == null ) continue;
				if( !first ) builder.append( "," );
				builder.append( key );
				builder.append( "=" );
				builder.append( value );
				first = false;
			}
		}
		builder.append( "}" );

		return builder.toString();
	}

	@Override
	public boolean equals( Object object ) {
		if( object == null || this.getClass() != object.getClass() ) return false;

		Node that = (Node)object;
		if( primaryKeyList != null ) {
			for( String key : primaryKeyList ) {
				if( !Objects.equals( this.getValue( key ), that.getValue( key ) ) ) return false;
			}
		}
		if( naturalKeyList != null ) {
			for( String key : naturalKeyList ) {
				if( !Objects.equals( this.getValue( key ), that.getValue( key ) ) ) return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int hashcode = 0;

		if( primaryKeyList != null ) {
			for( String key : primaryKeyList ) {
				Object value = getValue( key );
				if( value != null ) hashcode ^= value.hashCode();
			}
		}
		if( naturalKeyList != null ) {
			for( String key : naturalKeyList ) {
				Object value = getValue( key );
				if( value != null ) hashcode ^= value.hashCode();
			}
		}

		return hashcode == 0 ? super.hashCode() : hashcode;
	}

	/**
	 * Get a comparator using the natural key values to compare.
	 *
	 * @param <T> The node type
	 * @return A node comparator
	 */
	public <T extends Node> Comparator<T> getComparator() {
		return new NodeComparator<>( naturalKeyList );
	}

	protected List<String> getPrimaryKey() {
		return Collections.unmodifiableList( primaryKeyList );
	}

	/**
	 * Define the primary key for this type. This method is usually called from
	 * the class constructor.
	 *
	 * @param keys The value keys to use as the primary key
	 */
	protected void definePrimaryKey( String... keys ) {
		primaryKeyList = List.of( keys );
	}

	protected List<String> getNaturalKey() {
		return Collections.unmodifiableList( naturalKeyList );
	}

	/**
	 * Define the primary key for this type. This method is usually called from
	 * the class constructor.
	 *
	 * @param keys The value keys to use as the natural key
	 */
	protected void defineNaturalKey( String... keys ) {
		naturalKeyList = List.of( keys );
	}

	protected Set<String> getReadOnlyKeys() {
		return Collections.unmodifiableSet( readOnlySet );
	}

	/**
	 * Define what value keys should be marked read-only.
	 *
	 * @param keys The value keys to mark as read-only
	 */
	protected void defineReadOnly( String... keys ) {
		readOnlySet = Set.of( keys );
	}

	/**
	 * Check if the value key is read-only.
	 *
	 * @param key The value key
	 * @return True if the value key is read-only, false otherwise
	 */
	protected boolean isReadOnly( String key ) {
		return readOnlySet != null && readOnlySet.contains( key );
	}

	/**
	 * Add value keys that can change the modified flag if the value is changed.
	 *
	 * @param keys The value keys
	 */
	protected void addModifyingKeys( String... keys ) {
		if( modifyingKeySet == null ) modifyingKeySet = new CopyOnWriteArraySet<>();
		modifyingKeySet.addAll( Set.of( keys ) );
	}

	/**
	 * Remove value keys that can change the modified flag if the value is changed.
	 *
	 * @param keys The value keys
	 */
	protected void removeModifyingKeys( String... keys ) {
		if( modifyingKeySet == null ) return;
		modifyingKeySet.removeAll( Set.of( keys ) );
		if( modifyingKeySet.isEmpty() ) modifyingKeySet = null;
	}

	/**
	 * Check if the value key is a modifying value key.
	 *
	 * @param key The value key
	 * @return True if the value key is a modifying value key, false otherwise
	 */
	protected boolean isModifyingKey( String key ) {
		return allKeysModify || Optional.ofNullable( modifyingKeySet ).map( s -> s.contains( key ) ).orElse( false );
	}

	/**
	 * Get the set of value keys.
	 *
	 * @return The set of value keys
	 */
	protected Set<String> getValueKeys() {
		return Collections.unmodifiableSet( Optional.ofNullable( values ).map( Map::keySet ).orElse( Set.of() ) );
	}

	/**
	 * Determine if a particular value key is set.
	 *
	 * @param key The key to check
	 * @return True if the key is set, false otherwise
	 */
	protected boolean exists( String key ) {
		return getValueKeys().contains( key );
	}

	/**
	 * Get all the values.
	 *
	 * @return A collection of all the values
	 */
	protected Collection<?> getValues() {
		return values == null ? Set.of() : values.values();
	}

	/**
	 * Get the values of a specific type
	 *
	 * @param clazz The type of value to find
	 * @param <T> The value type
	 * @return A collection of the values of the specific type
	 */
	@SuppressWarnings( "unchecked" )
	protected <T> Collection<T> getValues( Class<T> clazz ) {
		return (Collection<T>)getValues().stream().filter( clazz::isInstance ).collect( Collectors.toUnmodifiableSet() );
	}

	protected <T extends Node> Set<T> getValues( String key ) {
		return exists( key ) ? Collections.unmodifiableSet( getValue( key ) ) : Set.of();
	}

	protected <T extends Node> List<T> getValueList( String key, Comparator<T> comparator ) {
		List<T> list = new ArrayList<>( getValues( key ) );
		list.sort( comparator );
		return list;
	}

	protected <T extends Node> void addToSet( String key, T value ) {
		getValue( key, () -> doSetValue( key, null, new NodeSet<>() ) ).add( value );
	}

	protected <T extends Node> void removeFromSet( String key, T value ) {
		NodeSet<T> set = getValue( key );
		if( set == null ) return;
		set.remove( value );
		if( set.isEmpty() ) doSetValue( key, set, null );
	}

	/**
	 * If the specified key is not already associated with a value (or is mapped
	 * to {@code null}), attempts to compute its value using the given mapping
	 * function and enters it into this map unless {@code null}.
	 *
	 * @param key The value key
	 * @param function The function to compute a value
	 * @param <T> The value type
	 * @return The value
	 */
	protected <T> T computeIfAbsent( String key, Function<String, ? extends T> function ) {
		T value = getValue( key );
		if( value != null ) return value;
		return setValue( key, function.apply( key ) );
	}

	/**
	 * If the specified key is present (not {@link null}), attempts to recompute
	 * its value using the given mapping function and enters it into this map. If
	 * the new value is {@code null} the entry is removed.
	 *
	 * @param key The value key
	 * @param function The function to compute a value
	 * @param <T> The value type
	 * @return The value
	 */
	protected <T> T computeIfPresent( String key, BiFunction<String, ? super T, ? extends T> function ) {
		Objects.requireNonNull( function );
		T value = getValue( key );
		if( value == null ) return null;
		return setValue( key, function.apply( key, value ) );
	}

	/**
	 * Get the value at the specific key.
	 *
	 * @param key The value key
	 * @param <T> The value type
	 * @return The value
	 */
	protected <T> T getValue( String key ) {
		return getValue( key, (T)null );
	}

	/**
	 * Get the value for the specific key or the default value if the value has
	 * not been previously set. Note that this method does not set the value.
	 *
	 * @param key The value key
	 * @param defaultValue The default value
	 * @param <T> The value type
	 * @return The value
	 */
	protected <T> T getValue( String key, T defaultValue ) {
		return getValue( key, () -> defaultValue );
	}

	/**
	 * Get the value for the specific key or compute the default value with the
	 * specified {@link Supplier} if the value has not been previously set. Note
	 * that this method does not set the value.
	 *
	 * @param key The value key
	 * @param supplier The default value {@link Supplier}
	 * @param <T> The value type
	 * @return The value
	 */
	@SuppressWarnings( "unchecked" )
	protected <T> T getValue( String key, Supplier<T> supplier ) {
		Objects.requireNonNull( key, "Value key cannot be null" );
		T value = values == null ? null : (T)values.get( key );
		return value != null ? value : supplier != null ? supplier.get() : null;
	}

	/**
	 * Set the value at the specific key.
	 *
	 * @param key The value key
	 * @param newValue The value
	 */
	protected <T> T setValue( String key, T newValue ) {
		if( key == null ) throw new NullPointerException( "Value key cannot be null" );
		if( readOnlySet != null && readOnlySet.contains( key ) ) throw new IllegalStateException( "Attempt to set read-only value: " + key );

		try {
			Txn.create();
			Object oldValue = getValue( key );
			if( newValue instanceof Node ) removeFromParent( (Node)newValue );
			Txn.submit( new SetValueOperation( this, key, oldValue, newValue ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.log( Log.ERROR, "Error setting key: " + key, exception );
		}
		return newValue;
	}

	protected Map<String, Object> asMap( String... keys ) {
		return Arrays.stream( keys ).filter( k -> values.get( k )!= null ).collect( Collectors.toMap( k -> k, k ->  values.get( k ) ) );
	}

	protected boolean addNodes( Collection<? extends Node> collection ) {
		boolean changed = false;
		try {
			Txn.create();
			for( Node node : collection ) {
				String key = node.getCollectionId();
				if( hasKey( key ) ) continue;
				Object oldValue = getValue( key );
				removeFromParent( node );
				Txn.submit( new SetValueOperation( this, key, oldValue, node ) );
				changed = true;
			}
			Txn.commit();
		} catch( TxnException exception ) {
			log.log( Log.ERROR, "Error adding collection", exception );
			return false;
		}
		return changed;
	}

	protected boolean removeNodes( Collection<?> collection ) {
		boolean changed = false;
		try {
			Txn.create();
			for( Object object : collection ) {
				if( !(object instanceof Node) ) continue;
				Node node = (Node)object;
				String key = node.getCollectionId();
				if( !hasKey( key ) ) continue;
				Object oldValue = getValue( key );
				Txn.submit( new SetValueOperation( this, key, oldValue, null ) );
				changed = true;
			}
			Txn.commit();
		} catch( TxnException exception ) {
			log.log( Log.ERROR, "Error removing collection", exception );
			return false;
		}
		return changed;
	}

	/**
	 * Remove all values from this node.
	 */
	protected void clear() {
		try {
			Txn.create();
			getValueKeys().stream().sorted().forEach( k -> setValue( k, null ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.log( Log.ERROR, "Error clearing values", exception );
		}
	}

	@SuppressWarnings( "unchecked" )
	protected <T> Stream<T> streamNodes() {
		return (Stream<T>)(values == null ? Set.of() : values.values()).stream();
	}

	protected boolean isEmpty() {
		return values == null || values.isEmpty();
	}

	protected int size() {
		return values == null ? 0 : values.size();
	}

	protected boolean hasKey( String key ) {
		return values != null && values.containsKey( key );
	}

	String getCollectionId() {
		return id;
	}

	Node setCollectionId( String id ) {
		this.id = Objects.requireNonNull( id );
		return this;
	}

	boolean isModifiedByValue() {
		return getModifiedValueCount() > 0;
	}

	boolean isModifiedByChild() {
		return getModifiedChildCount() > 0;
	}

	int getModifiedValueCount() {
		return modifiedValues == null ? 0 : modifiedValues.size();
	}

	int getModifiedChildCount() {
		return modifiedChildren == null ? 0 : modifiedChildren.size();
	}

	/**
	 * Gets the number of parents in the tree between this node and the specified
	 * node inclusive. If the specified node is this node then it returns 0. If
	 * The specified node is not a parent of this node then it returns -1.
	 */
	int distanceTo( Node target ) {
		if( this == target ) return 0;

		int count = 0;
		Node node = this;
		while( node != null && node != target ) {
			count++;
			node = node.getParent();
		}

		return node == null ? -1 : count;
	}

	/**
	 * Get the parent node of this node or null if the node does not have a
	 * parent.
	 *
	 * @param <T> The parent type
	 * @return The parent node or null
	 */
	@SuppressWarnings( "unchecked" )
	public <T extends Node> T getParent() {
		return (T)parent;
	}

	void setParent( Node parent ) {
		checkForCircularReference( parent );
		this.parent = parent;
	}

	List<Node> getNodePath() {
		return getNodePath( null );
	}

	List<Node> getNodePath( Node stop ) {
		List<Node> path = new ArrayList<>();
		if( this != stop && parent != null ) path = parent.getNodePath();
		path.add( this );
		return path;
	}

	private void doSetSelfModified( boolean newValue ) {
		selfModified = newValue;

		if( !newValue ) {
			modifiedValues = null;
			modifiedChildren = null;
		}

		updateModified();
	}

	private <S, T> T doSetValue( String key, S oldValue, T newValue ) {
		if( newValue == null ) {
			if( values == null ) return null;
			values.remove( key );
			if( values.size() == 0 ) values = null;
			if( oldValue instanceof Node ) ((Node)oldValue).setParent( null );
		} else {
			if( values == null ) values = new ConcurrentHashMap<>();
			values.put( key, newValue );
			if( newValue instanceof Node ) ((Node)newValue).setParent( this );
		}

		updateModified();

		return newValue;
	}

	private boolean doSetChildModified( Node child, boolean modified ) {
		boolean previousModified = isModified();

		// Search the value map for the modified child
		for( String key : values.keySet() ) {
			Object value = values.get( key );
			if( value != child ) continue;
			modifiedChildren = updateSet( modifiedChildren, child, modified );
		}

		updateModified();

		return isModified() != previousModified;
	}

	private void updateModified() {
		modified = selfModified || isModifiedByValue() || isModifiedByChild();
	}

	private <T> Set<T> updateSet( Set<T> set, T child, boolean newValue ) {
		if( newValue ) {
			if( set == null ) set = new CopyOnWriteArraySet<>();
			set.add( child );
		} else {
			if( set != null ) {
				set.remove( child );
				if( set.size() == 0 ) set = null;
			}
		}

		return set;
	}

	private void removeFromParent( Node child ) {
		Node parent = child.getParent();
		if( parent != null ) {
			parent.getValueKeys().forEach( k -> {
				if( parent.getValue( k ).equals( child ) ) parent.setValue( k, null );
			} );
		}
	}

	private void checkForCircularReference( Node node ) {
		Node next = this;
		while( next != null ) {
			if( node == next ) throw new CircularReferenceException( "Circular reference detected in parent path: " + node );
			next = next.getParent();
		}
	}

	private static abstract class NodeTxnOperation extends TxnOperation {

		NodeTxnOperation( Node node ) {
			super( node );
		}

		final Node getNode() {
			return (Node)getTarget();
		}

		final void fireEvent( NodeEvent event ) {
			fireTargetedEvent( getNode(), event );
		}

		/**
		 * Fire a cascading event. A cascading event is where the same event is sent
		 * to the node and all parents.
		 *
		 * @param event The event
		 */
		protected final void fireCascadingEvent( NodeEvent event ) {
			Node node = event.getNode();
			while( node != null ) {
				fireTargetedEvent( node, event );
				node = node.getParent();
			}
		}

		/**
		 * Fire a sliding event. A sliding event is where a new event of the same
		 * type is generated for the node and each parent.
		 *
		 * @param type The event type
		 */
		final void fireSlidingEvent( EventType<NodeEvent> type ) {
			Node node = getNode();
			while( node != null ) {
				fireTargetedEvent( node, new NodeEvent( node, type ) );
				node = node.getParent();
			}
		}

		final void fireTargetedEvent( Node target, NodeEvent event ) {
			getResult().addEvent( target, event );
		}

	}

	private class SetSelfModifiedOperation extends NodeTxnOperation {

		private final boolean oldValue;

		private final boolean newValue;

		private SetSelfModifiedOperation( Node node, boolean oldValue, boolean newValue ) {
			super( node );
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		protected void commit() throws TxnException {
			boolean currentValue = modified;

			// This operation must be created before any changes are made
			UpdateModifiedOperation updateModified = new UpdateModifiedOperation( Node.this );

			// Even if the flag value does not change, doSetModified should be called
			doSetSelfModified( newValue );

			// Propagate the flag value to children
			if( values != null && !newValue ) {
				// Clear the modified flag of any child nodes
				for( Object value : values.values() ) {
					if( value instanceof Node ) {
						Node child = (Node)value;
						if( child.isModified() ) {
							child.doSetSelfModified( false );
							fireTargetedEvent( child, new NodeEvent( child, NodeEvent.UNMODIFIED ) );
							fireTargetedEvent( child, new NodeEvent( child, NodeEvent.NODE_CHANGED ) );
						}
					}
				}
			}

			if( newValue != currentValue ) {
				fireEvent( new NodeEvent( getNode(), newValue ? NodeEvent.MODIFIED : NodeEvent.UNMODIFIED ) );
				fireEvent( new NodeEvent( getNode(), NodeEvent.NODE_CHANGED ) );
				Txn.submit( updateModified );
			}
		}

		@Override
		protected void revert() {
			doSetSelfModified( oldValue );
		}

		@Override
		public String toString() {
			return "set flag  modified " + oldValue + " -> " + newValue;
		}

	}

	private class SetValueOperation extends NodeTxnOperation {

		private final String key;

		private final Object oldValue;

		private final Object newValue;

		private SetValueOperation( Node node, String key, Object oldValue, Object newValue ) {
			super( node );
			this.key = key;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		protected void commit() throws TxnException {
			if( Objects.equals( getValue( key ), newValue ) ) return;

			// This operation must be created before any changes are made
			UpdateModifiedOperation updateModified = new UpdateModifiedOperation( Node.this );

			doSetValue( key, oldValue, newValue );

			if( isModifyingKey( key ) ) {
				// Update the modified value map
				Object preValue = modifiedValues == null ? null : modifiedValues.get( key );
				if( preValue == null ) {
					// Only add the value if there is not an existing previous value
					if( modifiedValues == null ) modifiedValues = new ConcurrentHashMap<>();
					modifiedValues.put( key, oldValue == null ? NULL : oldValue );
				} else if( Objects.equals( preValue == NULL ? null : preValue, newValue ) ) {
					if( modifiedValues != null ) {
						modifiedValues.remove( key );
						if( modifiedValues.size() == 0 ) modifiedValues = null;
					}
				}

				boolean childAdd = oldValue == null && newValue instanceof Node;
				boolean childRemove = newValue == null && oldValue instanceof Node;
				if( childAdd ) {
					fireTargetedEvent( (Node)newValue, new NodeEvent( (Node)newValue, NodeEvent.ADDED ) );
					fireCascadingEvent( new NodeEvent( getNode(), NodeEvent.CHILD_ADDED, key, null, newValue ) );
				}
				if( childRemove ) {
					fireTargetedEvent( (Node)oldValue, new NodeEvent( (Node)oldValue, NodeEvent.REMOVED ) );
					fireCascadingEvent( new NodeEvent( getNode(), NodeEvent.CHILD_REMOVED, key, oldValue, null ) );
				}
				Txn.submit( updateModified );
			}

			fireCascadingEvent( new NodeEvent( getNode(), NodeEvent.VALUE_CHANGED, key, oldValue, newValue ) );

			fireSlidingEvent( NodeEvent.NODE_CHANGED );
		}

		@Override
		protected void revert() {
			doSetValue( key, newValue, oldValue );
		}

		@Override
		public String toString() {
			return "set value " + key + " " + oldValue + " -> " + newValue;
		}

	}

	@SuppressWarnings( "InnerClassMayBeStatic" )
	private class RefreshOperation extends NodeTxnOperation {

		RefreshOperation( Node node ) {
			super( node );
		}

		@Override
		protected void commit() {
			fireSlidingEvent( NodeEvent.NODE_CHANGED );
		}

		@Override
		protected void revert() {}
	}

	private class UpdateModifiedOperation extends NodeTxnOperation {

		private final boolean oldValue;

		UpdateModifiedOperation( Node node ) {
			super( node );
			// Check only the modified values
			oldValue = isModifiedByValue();
		}

		@Override
		protected void commit() {
			// Check only the modified values
			boolean newValue = isModifiedByValue();

			// Check if the modified values should change the modified flag
			if( newValue != oldValue ) {
				doSetSelfModified( newValue );
				fireEvent( new NodeEvent( getNode(), newValue ? NodeEvent.MODIFIED : NodeEvent.UNMODIFIED ) );
			}

			// Check all the parents for modification
			Node node = getNode();
			Node parent = node.getParent();
			while( parent != null ) {
				boolean priorModified = parent.isModified();
				boolean parentChanged = parent.doSetChildModified( node, newValue );
				if( parentChanged ) fireTargetedEvent( parent, new NodeEvent( parent, !priorModified ? NodeEvent.MODIFIED : NodeEvent.UNMODIFIED ) );
				node = parent;
				parent = parent.getParent();
			}

			fireSlidingEvent( NodeEvent.NODE_CHANGED );
		}

		@Override
		protected void revert() {
			doSetSelfModified( oldValue );
		}

		@Override
		public String toString() {
			return "update modified from " + oldValue;
		}

	}

}
