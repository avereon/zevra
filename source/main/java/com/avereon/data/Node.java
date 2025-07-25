package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import com.avereon.transaction.*;
import lombok.CustomLog;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A generic data node supporting getting and setting values, a modified (or
 * dirty) flag, {@link NodeEvent events} and {@link Txn transactions}. It is
 * expected that this class be inherited and that subclasses will be configured
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
 * By default, no values will cause the modified flag to change. In order for a
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
 * to handle the case where lambdas are registered for changes the value.
 * Example:
 * <pre>
 *   person.register( "name", e -&gt; displayPersonName( e.getNewValue() ) );
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
@CustomLog
public class Node implements TxnEventTarget, Cloneable, Comparable<Node> {

	/**
	 * A special object to represent previously null values in the modifiedValues
	 * map.
	 */
	private static final Object WAS_PREVIOUSLY_NULL = new Object();

	/**
	 * The node event hub.
	 */
	private final EventHub hub;

	/**
	 * The node value change handlers, a special set of handlers for value changes.
	 */
	private final Map<Object, Map<String, Set<EventHandler<NodeEvent>>>> valueChangeHandlers;

	/**
	 * The node id.
	 */
	private String collectionId;

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
	private List<String> primaryKeySet;

	/**
	 * The list of value keys that specify the natural key.
	 */
	private List<String> naturalKeySet;

	/**
	 * The set of value keys that are used to compute hashCode and equals.
	 */
	private Set<String> hashEqualsKeyList;

	/**
	 * The set of value keys intentionally allowed to modify the node.
	 */
	private Set<String> includedModifyingKeySet;

	/**
	 * The set of value keys intentionally not allowed to modify the node.
	 */
	private Set<String> excludedModifyingKeySet;

	private boolean allKeysModify;

	/**
	 * The set of value keys that are read only.
	 */
	private Set<String> readOnlySet;

	/**
	 * The internally calculated modified flag used to allow for fast read rates.
	 * This is updated when the self modified flag, values or children are changed.
	 * -- GETTER --
	 * Is the node modified. The node is modified if any data value has been
	 * modified or any child node has been modified since the last time the
	 * modified flag was cleared.
	 */
	@Getter
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

	private Comparator<Node> comparator;

	/**
	 * Create a new, generic, empty data node. It is generally expected that the
	 * Node class will be inherited, instead of used directly, but there is no
	 * restriction creating "generic" nodes.
	 */
	public Node() {
		this.collectionId = UUID.randomUUID().toString();
		this.hub = new EventHub();
		this.valueChangeHandlers = new WeakHashMap<>();
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
		Txn.run( () -> Txn.submit( new SetSelfModifiedOperation( this, selfModified, newValue ) ) );
	}

	void setAllKeysModify() {
		this.allKeysModify = true;
	}

	/**
	 * Subclasses may implement extra rules regarding the modification of this
	 * node. For example, if a particular value in a field would not allow the
	 * node to be modified then that can be implemented here.
	 *
	 * @param value The node value to use for checking if modify is allowed
	 * @return True if the node can be modified, false otherwise
	 */
	public boolean modifyAllowed( Object value ) {
		return true;
	}

	/**
	 * Request that a {@link NodeEvent#NODE_CHANGED} event occur. This is usually
	 * used to cause the node handlers to run if they were added after the node
	 * was changed. This is common during node initialization where the state is
	 * set, the modified flag cleared, the handlers added and then this method is
	 * called.
	 */
	public void refresh() {
		fireHoppingEvent( new NodeEvent( this, NodeEvent.NODE_CHANGED ) );
	}

	/**
	 * Dispatch a {@link TxnEvent} to the data node. This method should not be
	 * called by other classes other than the {@link Txn transaction} classes.
	 *
	 * @param event The transaction event
	 */
	@Override
	public void dispatch( TxnEvent event ) {
		boolean isNodeEvent = event instanceof NodeEvent;

		if( isNodeEvent ) doDispatchToNode( (NodeEvent)event );
		hub.dispatch( event );
		if( !isNodeEvent && getParent() != null ) getParent().dispatch( event );
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
	 * Register an event handler with this data node.
	 *
	 * @param owner The owner of the handler, used to remove the handler when the owner is garbage collected
	 * @param type The event type
	 * @param handler The event handler
	 * @param <T> The type of event to handle
	 * @return The data node's {@link EventHub}
	 */
	public <T extends Event> EventHub register( Object owner, EventType<? super T> type, EventHandler<? super T> handler ) {
		return hub.register( owner, type, handler );
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
	 * Unregister an event handler from this data node.
	 *
	 * @param owner The owner of the handler, used to remove the handler when the owner is garbage collected
	 * @param type The event type
	 * @param handler The event handler
	 * @param <T> The type of event to handle
	 * @return The data node's {@link EventHub}
	 */
	public <T extends Event> EventHub unregister( Object owner, EventType<? super T> type, EventHandler<? super T> handler ) {
		return hub.unregister( owner, type, handler );
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
		register( this, key, handler );
	}

	/**
	 * Register a value changed event handler for a specific value key. This is
	 * useful to register lambda style event handlers for specific value changes.
	 * <p>
	 * NOTE: These handlers only receive value changed events that happen on this
	 * node not on any child nodes like normal listeners.
	 * </p>
	 * @param owner The owner of the handler, used to remove the handler when the owner is garbage collected
	 * @param key The value key
	 * @param handler The value changed handler
	 */
	public void register( Object owner, String key, EventHandler<NodeEvent> handler ) {
		// The owner is the "owner" of the handler. When the owner is garbage
		// collected the handler will be removed from the valueChangeHandlers map.
		Map<String, Set<EventHandler<NodeEvent>>> keyHandlers = valueChangeHandlers.computeIfAbsent( owner, ( _ ) -> new HashMap<>() );
		Set<EventHandler<NodeEvent>> handlers = keyHandlers.computeIfAbsent( key, ( _ ) -> new CopyOnWriteArraySet<>() );
		handlers.add( handler );
	}

	/**
	 * Unregister a value changed event handler for a specific value key.
	 *
	 * @param key The value key
	 * @param handler The value changed handler
	 */
	public void unregister( String key, EventHandler<NodeEvent> handler ) {
		unregister( this, key, handler );
	}

	/**
	 * Unregister a value changed event handler for a specific value key.
	 *
	 * @param owner The owner of the handler, used to remove the handler when the owner is garbage collected
	 * @param key The value key
	 * @param handler The value changed handler
	 */
	public void unregister( Object owner, String key, EventHandler<NodeEvent> handler ) {
		Map<String, Set<EventHandler<NodeEvent>>> keyHandlers = valueChangeHandlers.get( owner );
		if( keyHandlers == null ) return;
		Set<EventHandler<NodeEvent>> handlers = keyHandlers.get( key );
		if( handlers == null ) return;
		handlers.remove( handler );
		if( handlers.isEmpty() ) keyHandlers.remove( key );
		if( keyHandlers.isEmpty() ) valueChangeHandlers.remove( owner );
	}

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
			// Do not overwrite primary key values
			if( isPrimaryKey( key ) ) continue;
			// Copy non-primary key values
			if( overwrite || getValue( key ) == null ) setValue( key, node.getValue( key ) );
		}

		return (T)this;
	}

	@Override
	@SuppressWarnings( "MethodDoesntCallSuperMethod" )
	public Object clone() {
		try {
			getClass().getConstructor().newInstance();
			Node clone = getClass().getConstructor().newInstance();
			clone.copyFrom( this );
			return clone;
		} catch( Exception exception ) {
			log.atWarn( exception );
		}
		return null;
	}

	/**
	 * Get a string representation of this node. By default, this implementation
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

		if( allValues ) {
			keys.addAll( getValueKeys() );
		} else {
			keys.addAll( getPrimaryKey() );
			keys.addAll( getNaturalKey() );
			keys.addAll( getModifyingKeys() );
		}

		Collections.sort( keys );

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

	public boolean equals( Object object, String... keys ) {
		return equals( object, Set.of( keys ) );
	}

	public boolean equals( Object object, Set<String> keys ) {
		if( object == null || this.getClass() != object.getClass() ) return false;

		Node that = (Node)object;
		Set<String> mismatchedKeys = keys.stream().filter( k -> !Objects.equals( this.getValue( k ), that.getValue( k ) ) ).collect( Collectors.toSet() );

		return mismatchedKeys.isEmpty();
	}

	@Override
	public boolean equals( Object object ) {
		if( object == null || this.getClass() != object.getClass() ) return false;

		if( hashEqualsKeyList == null ) {
			hashEqualsKeyList = new HashSet<>();
			hashEqualsKeyList.addAll( getPrimaryKey() );
			hashEqualsKeyList.addAll( getNaturalKey() );
		}

		return equals( object, hashEqualsKeyList );
	}

	@Override
	public int hashCode() {
		int hashcode = 0;

		if( hashEqualsKeyList == null ) {
			hashEqualsKeyList = new HashSet<>();
			hashEqualsKeyList.addAll( getPrimaryKey() );
			hashEqualsKeyList.addAll( getNaturalKey() );
		}

		for( String key : hashEqualsKeyList ) {
			Object value = getValue( key );
			if( value != null ) hashcode ^= value.hashCode();
		}

		return hashcode == 0 ? super.hashCode() : hashcode;
	}

	@Override
	public int compareTo( Node that ) {
		if( comparator == null ) comparator = getNaturalComparator();
		return comparator.compare( this, that );
	}

	/**
	 * Get a comparator using the natural key values to compare.
	 *
	 * @param <T> The node type
	 * @return A node comparator
	 */
	public <T extends Node> Comparator<T> getNaturalComparator() {
		return new NodeComparator<>( getNaturalKey() );
	}

	protected List<String> getPrimaryKey() {
		return primaryKeySet == null ? List.of() : Collections.unmodifiableList( primaryKeySet );
	}

	protected boolean isPrimaryKey( String key ) {
		return getPrimaryKey().contains( key );
	}

	/**
	 * Define the primary key for this type. This method is usually called from
	 * the class constructor.
	 *
	 * @param keys The value keys to use as the primary key
	 */
	protected void definePrimaryKey( String... keys ) {
		primaryKeySet = List.of( keys );
		hashEqualsKeyList = null;
	}

	protected List<String> getNaturalKey() {
		return naturalKeySet == null ? List.of() : Collections.unmodifiableList( naturalKeySet );
	}

	/**
	 * Define the primary key for this type. This method is usually called from
	 * the class constructor.
	 *
	 * @param keys The value keys to use as the natural key
	 */
	protected void defineNaturalKey( String... keys ) {
		naturalKeySet = List.of( keys );
		hashEqualsKeyList = null;
	}

	protected Set<String> getReadOnlyKeys() {
		return readOnlySet == null ? Set.of() : Collections.unmodifiableSet( readOnlySet );
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
	@SuppressWarnings( "SameParameterValue" )
	protected boolean isReadOnly( String key ) {
		return getReadOnlyKeys().contains( key );
	}

	public Set<String> getModifyingKeys() {
		return getValueKeys().stream().filter( this::isModifyingKey ).collect( Collectors.toSet() );
	}

	/**
	 * Add value keys that can change the modified flag if the value is changed.
	 *
	 * @param keys The value keys
	 */
	public void addModifyingKeys( String... keys ) {
		if( includedModifyingKeySet == null ) includedModifyingKeySet = new CopyOnWriteArraySet<>();
		includedModifyingKeySet.addAll( Set.of( keys ) );
	}

	/**
	 * Remove value keys that can change the modified flag if the value is changed.
	 *
	 * @param keys The value keys
	 */
	public void removeModifyingKeys( String... keys ) {
		if( includedModifyingKeySet == null ) return;
		includedModifyingKeySet.removeAll( Set.of( keys ) );
		if( includedModifyingKeySet.isEmpty() ) includedModifyingKeySet = null;
	}

	public void addExcludedModifyingKeys( String... keys ) {
		if( excludedModifyingKeySet == null ) excludedModifyingKeySet = new CopyOnWriteArraySet<>();
		excludedModifyingKeySet.addAll( Set.of( keys ) );
	}

	/**
	 * Remove value keys not allowed to change the modified flag if the value is
	 * changed.
	 *
	 * @param keys The value keys
	 */
	@SuppressWarnings( "unused" )
	public void removeExcludedModifyingKeys( String... keys ) {
		if( excludedModifyingKeySet == null ) return;
		excludedModifyingKeySet.removeAll( Set.of( keys ) );
		if( excludedModifyingKeySet.isEmpty() ) excludedModifyingKeySet = null;
	}

	/**
	 * Check if the value key is a modifying value key.
	 *
	 * @param key The value key
	 * @return True if the value key is a modifying value key, false otherwise
	 */
	public boolean isModifyingKey( String key ) {
		return (allKeysModify || keyIncludedInModify( key )) && !keyExcludedFromModify( key );
	}

	private boolean keyIncludedInModify( String key ) {
		return Optional.ofNullable( includedModifyingKeySet ).map( s -> s.contains( key ) ).orElse( false );
	}

	private boolean keyExcludedFromModify( String key ) {
		return Optional.ofNullable( excludedModifyingKeySet ).map( s -> s.contains( key ) ).orElse( false );
	}

	private boolean isReadOnlyKey( String key ) {
		return getReadOnlyKeys().contains( key );
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
	protected Collection<Object> getValues() {
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
		if( value == null ) return;
		getValue( key, () -> doSetValue( key, null, new NodeSet<>( key ) ) ).add( value );
	}

	protected <T extends Node> void removeFromSet( String key, T value ) {
		NodeSet<T> set = getValue( key );
		if( set == null ) return;
		if( set.remove( value ) && set.isNodeSetEmpty() ) doSetValue( key, set, null );
	}

	protected <T extends Node> void addToSet( String key, Collection<T> values ) {
		if( values.isEmpty() ) return;
		getValue( key, () -> doSetValue( key, null, new NodeSet<>( key ) ) ).addAll( values );
	}

	protected <T extends Node> void removeFromSet( String key, Collection<T> values ) {
		NodeSet<T> set = getValue( key );
		if( set == null ) return;
		if( set.removeAll( values ) && set.isNodeSetEmpty() ) doSetValue( key, set, null );
	}

	protected <T extends Node> void clearSet( String key ) {
		NodeSet<T> set = getValue( key );
		if( set == null ) return;
		set.clear();
		doSetValue( key, set, null );
	}

	protected void setSetModifyFilter( String key, Function<Node, Boolean> filter ) {
		getValue( key, () -> doSetValue( key, null, new NodeSet<>( key ) ) ).setSetModifyFilter( filter );
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
	 * If the specified key is present (not null), attempts to recompute
	 * its value using the given mapping function and enters it into this map. If
	 * the new value is {@code null} the entry is removed.
	 *
	 * @param key The value key
	 * @param function The function to compute a value
	 * @param <T> The value type
	 * @return The value
	 */
	@SuppressWarnings( "unused" )
	protected <T> T computeIfPresent( String key, BiFunction<String, ? super T, ? extends T> function ) {
		Objects.requireNonNull( function );
		T value = getValue( key );
		if( value == null ) return null;
		return setValue( key, function.apply( key, value ) );
	}

	public boolean isSet( String key ) {
		return values != null && values.containsKey( key );
	}

	public boolean isNotSet( String key ) {
		return values == null || !values.containsKey( key );
	}

	/**
	 * Get the value at the specific key.
	 *
	 * @param key The value key
	 * @param <T> The value type
	 * @return The value
	 */
	public <T> T getValue( String key ) {
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
	public <T> T getValue( String key, T defaultValue ) {
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
	public <T> T getValue( String key, Supplier<T> supplier ) {
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
	public <T> T setValue( String key, T newValue ) {
		return setValue( null, key, newValue );
	}

	<T> T setValue( String setKey, String key, T newValue ) {
		if( key == null ) throw new NullPointerException( "Value key cannot be null" );
		if( isReadOnlyKey( key ) ) throw new IllegalStateException( "Attempt to set read-only value: " + key );

		Txn.run( () -> Txn.submit( new SetValueOperation( this, setKey, key, getValue( key ), newValue ) ) );

		return newValue;
	}

	protected Map<String, Object> asMap( String... keys ) {
		return Arrays.stream( keys ).filter( k -> values.get( k ) != null ).collect( Collectors.toMap( k -> k, k -> values.get( k ) ) );
	}

	/**
	 * Remove all values from this node.
	 */
	protected void clear() {
		Txn.run( () -> getValueKeys().stream().sorted().forEach( k -> setValue( k, null ) ) );
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
		return collectionId;
	}

	void setCollectionId( String id ) {
		this.collectionId = Objects.requireNonNull( id );
	}

	protected boolean isModifiedBySelf() {
		return selfModified;
	}

	protected boolean isModifiedByValue() {
		return getModifiedValueCount() > 0;
	}

	protected boolean isModifiedByChild() {
		return getModifiedChildCount() > 0;
	}

	protected int getModifiedValueCount() {
		return modifiedValues == null ? 0 : modifiedValues.size();
	}

	protected int getModifiedChildCount() {
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
		if( parent instanceof NodeSet ) return parent.getParent();
		return (T)parent;
	}

	@SuppressWarnings( "unchecked" )
	<T extends Node> T getTrueParent() {
		return (T)parent;
	}

	@SuppressWarnings( "StatementWithEmptyBody" )
	void doSetParent( Node parent ) {
		checkForCircularReference( parent );

		if( this.parent != null ) {
			// Disconnect
		}

		// Set the parent
		this.parent = parent;

		if( this.parent != null ) {
			// Connect
		}
	}

	List<Node> getNodePath() {
		return getNodePath( null );
	}

	@SuppressWarnings( "SameParameterValue" )
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

		updateInternalModified();
	}

	<S, T> T doSetValue( String key, S oldValue, T newValue ) {
		if( newValue == null ) {
			if( values == null ) return null;
			values.remove( key );
			if( values.isEmpty() ) values = null;
			if( oldValue instanceof Node ) doRemoveFromParent( (Node)oldValue, true );
		} else {
			if( values == null ) values = new ConcurrentHashMap<>();
			if( newValue instanceof Node ) doRemoveFromParent( (Node)newValue, false );
			values.put( key, newValue );
			if( newValue instanceof Node ) ((Node)newValue).doSetParent( this );
		}

		updateInternalModified();

		return newValue;
	}

	private void doRemoveFromParent( Node child, boolean quiet ) {
		Node parent = child.getParent();
		if( parent != null ) {
			parent.getValueKeys().stream().filter( k -> parent.getValue( k ).equals( child ) ).forEach( k -> {
				if( quiet ) {
					parent.doSetValue( k, child, null );
				} else {
					parent.setValue( k, null );
				}
			} );
			child.doSetParent( null );
		}
	}

	private void doSetChildModified( Node child, boolean newModified ) {
		// Update the modified children set
		if( values.containsValue( child ) ) this.modifiedChildren = updateModifiedSet( modifiedChildren, child, newModified );
		updateInternalModified();
	}

	/**
	 * This implementation is different from calling isModified() because this
	 * implementation uses the internal flags for the value, not just the
	 * computed modified flag.
	 *
	 * @return If the node is modified
	 */
	private boolean isInternalModified() {
		return isModifiedBySelf() | isModifiedByValue() | isModifiedByChild();
	}

	private void updateInternalModified() {
		modified = isInternalModified();
	}

	private <T> Set<T> updateModifiedSet( Set<T> set, T child, boolean newValue ) {
		if( newValue ) {
			if( set == null ) set = new CopyOnWriteArraySet<>();
			set.add( child );
		} else if( set != null ) {
			set.remove( child );
			if( set.isEmpty() ) set = null;
		}

		return set;
	}

	/**
	 * Dispatch a {@link NodeEvent} to the data node. This method should not be
	 * called by other classes other than the {@link Node data} class.
	 *
	 * @param event The data node event
	 */
	private void doDispatchToNode( NodeEvent event ) {
		// Dispatch to value change handlers only when the event is on itself
		boolean self = event.getNode() == this;
		boolean valueChanged = event.getEventType() == NodeEvent.VALUE_CHANGED;
		if( self && valueChanged ) {
			ConcurrentModificationException exception;
			do {
				try {
					exception = null;

					// Determine the value key
					String key = event.getKey();

					// Collect all handlers for the key
					Set<EventHandler<NodeEvent>> valueHandlers = new HashSet<>();
					valueChangeHandlers.values().forEach( map -> {
						if( map.containsKey( key ) ) valueHandlers.addAll( map.get( key ) );
					} );

					// Dispatch the event to all handlers
					valueHandlers.forEach( v -> v.handle( event ) );
				} catch( ConcurrentModificationException cme ) {
					exception = cme;
				}
			} while( exception != null );
		}
	}

	private void checkForCircularReference( Node parent ) {
		checkForCircularReference( parent, this );
	}

	private static void checkForCircularReference( Node parent, Node node ) {
		Node next = parent;
		while( next != null ) {
			if( node == next ) throw new CircularReferenceException( "Circular reference detected in parent path: " + node );
			next = next.getParent();
		}
	}

	/**
	 * Fire a hopping event. A hopping event is where a new event of the
	 * same type is generated for the node and each parent.
	 *
	 * @param event The event
	 */
	private void fireHoppingEvent( NodeEvent event ) {
		Node node = this;
		while( node != null ) {
			fireTargetedEvent( node, new NodeEvent( node, event.getEventType() ) );
			node = node.getParent();
		}
	}

	private void fireTargetedEvent( Node target, NodeEvent event ) {
		target.getEventHub().dispatch( event );
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
		 * Fire a sliding event. A sliding event is where the same event is sent
		 * to the node and all parents.
		 *
		 * @param event The event
		 */
		protected final void fireSlidingEvent( NodeEvent event ) {
			Node node = event.getNode();
			while( node != null ) {
				fireTargetedEvent( node, event );
				node = node.getParent();
			}
		}

		/**
		 * Fire a hopping event. A hopping event is where a new event of the
		 * same type is generated for the node and each parent.
		 *
		 * @param event The event
		 */
		final void fireHoppingEvent( NodeEvent event ) {
			Node node = getNode();
			while( node != null ) {
				fireTargetedEvent( node, new NodeEvent( node, event.getEventType() ) );
				node = node.getParent();
			}
		}

		@SuppressWarnings( "SameParameterValue" )
		final void fireDroppingEvent( EventType<NodeEvent> type ) {
			Node source = getNode();
			Node target = getNode();

			if( source instanceof NodeSet ) source = source.getParent();
			if( source == null ) return;

			fireDroppingEvent( target, new NodeEvent( source, type ) );
		}

		final void fireDroppingEvent( Node target, NodeEvent event ) {
			if( target == null || target.values == null ) return;

			NodeEvent newEvent = new NodeEvent( event.getNode(), event.getEventType() );

			for( Object value : target.values.values() ) {
				if( value instanceof Node child ) {
					if( child instanceof NodeSet ) {
						for( Node setValue : (NodeSet<?>)value ) {
							child = setValue;
							fireTargetedEvent( child, newEvent );
							fireDroppingEvent( child, newEvent );
						}
					} else {
						fireTargetedEvent( child, newEvent );
						fireDroppingEvent( child, newEvent );
					}
				}
			}
		}

		final void fireTargetedEvent( Node target, NodeEvent event ) {
			getResult().addEvent( target, event );
		}

	}

	static class SetSelfModifiedOperation extends NodeTxnOperation {

		private final boolean oldValue;

		private final boolean newValue;

		SetSelfModifiedOperation( Node node, boolean oldValue, boolean newValue ) {
			super( node );
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		protected SetSelfModifiedOperation commit() {
			// This operation must be created before any changes are made
			boolean modifyAllowed = getNode().modifyAllowed( oldValue ) & getNode().modifyAllowed( newValue );
			UpdateModifiedOperation updateModified = new UpdateModifiedOperation( getNode() );
			getNode().doSetSelfModified( newValue );

			// Propagate the modified false flag value to children
			if( !newValue && getNode().values != null ) {
				// NOTE Cannot use parallelStream here because it causes out-of-order events
				getNode().values.values().stream().filter( v -> v instanceof Node ).map( v -> (Node)v ).filter( Node::isModified ).forEach( this::doClearChildModifiedFlag );
			}

			if( modifyAllowed ) updateModified.commit();

			getResult().addEventsFrom( updateModified );

			return this;
		}

		private void doClearChildModifiedFlag( Node child ) {
			getResult().addEventsFrom( new SetSelfModifiedOperation( child, child.selfModified, false ).commit() );
		}

		@Override
		protected SetSelfModifiedOperation revert() {
			getNode().doSetSelfModified( oldValue );
			return this;
		}

		@Override
		public String toString() {
			return "set flag modified " + oldValue + " -> " + newValue;
		}

	}

	static class SetValueOperation extends NodeTxnOperation {

		private final String setKey;

		private final String key;

		private final Object oldValue;

		private final Object newValue;

		SetValueOperation( Node node, String setKey, String key, Object oldValue, Object newValue ) {
			super( node );
			this.setKey = setKey;
			this.key = key;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		@Override
		protected SetValueOperation commit() throws TxnException {
			if( Objects.equals( getNode().getValue( key ), newValue ) ) return this;

			// This operation must be created before any changes are made
			boolean modifyAllowed = getNode().modifyAllowed( oldValue ) & getNode().modifyAllowed( newValue );
			UpdateModifiedOperation updateModified = new UpdateModifiedOperation( getNode() );
			getNode().doSetValue( key, oldValue, newValue );
			if( newValue instanceof Node && ((Node)newValue).getTrueParent() == null ) throw new RuntimeException( "Node parent not set correctly" );

			if( modifyAllowed && getNode().isModifyingKey( key ) ) {
				// If the preValue is null that means the value for this key has not been modified since the last transaction
				Object preValue = getNode().modifiedValues == null ? null : getNode().modifiedValues.get( key );

				boolean previouslyUnmodified = preValue == null;
				boolean modifiedToPriorValue = Objects.equals( preValue == WAS_PREVIOUSLY_NULL ? null : preValue, newValue );

				// Update the modified value map
				if( previouslyUnmodified ) {
					if( getNode().modifiedValues == null ) getNode().modifiedValues = new ConcurrentHashMap<>();
					getNode().modifiedValues.putIfAbsent( key, oldValue == null ? WAS_PREVIOUSLY_NULL : oldValue );
				} else if( modifiedToPriorValue && getNode().modifiedValues != null ) {
					getNode().modifiedValues.remove( key );
					if( getNode().modifiedValues.isEmpty() ) getNode().modifiedValues = null;
				}
			}

			if( modifyAllowed ) updateModified.commit();

			boolean childAdd = oldValue == null && newValue instanceof Node;
			boolean childRemove = newValue == null && oldValue instanceof Node;
			if( childAdd ) {
				fireTargetedEvent( (Node)newValue, new NodeEvent( (Node)newValue, NodeEvent.ADDED ) );
				fireSlidingEvent( new NodeEvent( getNode(), NodeEvent.CHILD_ADDED, setKey, key, null, newValue ) );
			} else if( childRemove ) {
				fireTargetedEvent( (Node)oldValue, new NodeEvent( (Node)oldValue, NodeEvent.REMOVED ) );
				fireSlidingEvent( new NodeEvent( getNode(), NodeEvent.CHILD_REMOVED, setKey, key, oldValue, null ) );
			} else {
				fireDroppingEvent( NodeEvent.PARENT_CHANGED );
			}

			fireSlidingEvent( new NodeEvent( getNode(), NodeEvent.VALUE_CHANGED, setKey, key, oldValue, newValue ) );
			getResult().addEventsFrom( updateModified );
			fireHoppingEvent( new NodeEvent( getNode(), NodeEvent.NODE_CHANGED ) );

			return this;
		}

		@Override
		protected SetValueOperation revert() {
			getNode().doSetValue( key, newValue, oldValue );
			return this;
		}

		@Override
		public String toString() {
			return "set value " + key + " " + oldValue + " -> " + newValue;
		}

	}

	@SuppressWarnings( "unused" )
	static class RefreshOperation extends NodeTxnOperation {

		RefreshOperation( Node node ) {
			super( node );
		}

		@Override
		protected RefreshOperation commit() {
			fireHoppingEvent( new NodeEvent( getNode(), NodeEvent.NODE_CHANGED ) );
			return this;
		}

		@Override
		protected RefreshOperation revert() {
			return this;
		}

	}

	static class UpdateModifiedOperation extends NodeTxnOperation {

		private final boolean oldModified;

		UpdateModifiedOperation( Node node ) {
			super( node );
			oldModified = node.isModified();
		}

		@Override
		protected UpdateModifiedOperation commit() {
			getNode().updateInternalModified();

			boolean newModified = getNode().isModified();

			if( newModified != oldModified ) {
				updateParentsModified( newModified );
				fireEvent( new NodeEvent( getNode(), newModified ? NodeEvent.MODIFIED : NodeEvent.UNMODIFIED ) );
				fireHoppingEvent( new NodeEvent( getNode(), NodeEvent.NODE_CHANGED ) );
			}

			return this;
		}

		private void updateParentsModified( boolean newModified ) {
			Node node = getNode();
			Node parent = node.getTrueParent();
			while( parent != null ) {
				if( !parent.modifyAllowed( node ) ) break;
				boolean oldParentModified = parent.isModified();
				parent.doSetChildModified( node, newModified );
				boolean newParentModified = parent.isModified();
				boolean parentChanged = oldParentModified != newParentModified;
				if( parentChanged ) fireTargetedEvent( parent, new NodeEvent( parent, newParentModified ? NodeEvent.MODIFIED : NodeEvent.UNMODIFIED ) );
				node = parent;
				parent = parent.getTrueParent();
			}
		}

		@Override
		protected UpdateModifiedOperation revert() {
			return this;
		}

		@Override
		public String toString() {
			return "update modified from " + oldModified;
		}

	}

}
