package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.event.EventHub;
import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.transaction.*;
import com.avereon.util.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * A generic data node supporting getting and setting values, a modified (or
 * dirty) flag, {@link Event events} and {@link Txn transactions}. It is
 * expected that this class be inherited and that sub-classes will be configured
 * to represent specific data types. For example, consider this Person type:
 *
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
 * been modified since {@code setModified( false );} was last called. Note that
 * only modifying keys can affect the modified flag. Using the Person class
 * above if the name is Foo and the modified flag is false, setting the name to
 * Bar will cause the modified flag to be set to true. Setting the name back to
 * Foo will cause the modified flag to be set back to false, since the name was
 * originally Foo.
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
 * transaction is committed. Events are not fired if the transaction is
 * rolled back.
 *
 * <h2>Value Events</h2>
 * Event handlers can also be registered for changes to specific values. This is
 * to handle the case where lambdas are registered for changes the the value.
 * Example:
 * <pre>
 *   person.register( "name", e -> updatePersonName( e.getNewValue() ) );
 * </pre>
 */
public class Node implements TxnEventTarget, Cloneable {

	private static final System.Logger log = Log.get();

	/**
	 * The modified flag key.
	 */
	@Deprecated
	public static final String MODIFIED = "flag.modified";

	/**
	 * A special object to represent previously null values in the modifiedValues
	 * map.
	 */
	private static final Object NULL = new Object();

	/**
	 * The parent of the node.
	 */
	private Node parent;

	/**
	 * The node values.
	 */
	private Map<String, Object> values;

	//	/**
	//	 * The node resources. This map provides a way to associate objects without
	//	 * affecting the data model as a whole. Adding or removing resources does not
	//	 * affect the node state nor does it cause any kind of event. This is simply a
	//	 * storage mechanism.
	//	 */
	//	private Map<String, Object> resources;

	/**
	 * The collection of edges this node is associated with. The node may be the
	 * source or may be the target of the edge.
	 */
	private Set<Edge> edges;

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

	/**
	 * The set of value keys that are read only.
	 */
	private Set<String> readOnlySet;

	private EventHub hub;

	private Map<String, Set<EventHandler<NodeEvent>>> valueChangeHandlers;

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
	 * Is the node modified. The node is modified if any data value has been
	 * modified or any child node has been modified since the last time the
	 * modified flag was cleared.
	 *
	 * @return true if this node or any child nodes are modified, false otherwise.
	 */
	public boolean isModified() {
		return modified;
	}

	public Node() {
		hub = new EventHub();
		valueChangeHandlers = new ConcurrentHashMap<>();
	}

	/**
	 * Set(true) or clear(false) the modified flag.
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

	//	public Set<Edge> getLinks() {
	//		return new HashSet<>( edges );
	//	}
	//
	//	public Edge link( Node target ) {
	//		return link( target, false );
	//	}
	//
	//	public Edge link( Node target, boolean directed ) {
	//		Edge edge = new Edge( this, target, directed );
	//		addEdge( edge );
	//		target.addEdge( edge );
	//		return edge;
	//	}
	//
	//	public void unlink( Node target ) {
	//		// Find all edges where target is a source or target
	//		for( Edge edge : findEdges( this.edges, this, target ) ) {
	//			edge.getSource().removeEdge( edge );
	//			edge.getTarget().removeEdge( edge );
	//		}
	//
	//	}

	//	@Deprecated
	//	public Set<String> getResourceKeys() {
	//		return getValueKeys();
	//	}
	//
	//	@Deprecated
	//	public <T> T getResource( String key ) {
	//		return getValue( key, null );
	//	}
	//
	//	@Deprecated
	//	@SuppressWarnings( "unchecked" )
	//	protected <T> T getResource( String key, T defaultValue ) {
	//		return getValue( key, defaultValue );
	//	}
	//
	//	@Deprecated
	//	public <T> void putResource( String key, T newValue ) {
	//		setValue( key, newValue );
	//	}

	public void refresh() {
		try {
			Txn.create();
			Txn.submit( new RefreshOperation( this ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.log( Log.ERROR, "Error refreshing: " + this, exception );
		}
	}

	@Override
	public void dispatch( TxnEvent event ) {
		if( event instanceof NodeEvent ) dispatch( (NodeEvent)event );
	}

	private void dispatch( NodeEvent event ) {
		if( event.getEventType() == NodeEvent.VALUE_CHANGED ) valueChangeHandlers.getOrDefault( event.getKey(), Set.of() ).forEach( h -> h.handle( event ) );
		hub.dispatch( event );
	}

	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return hub.register( type, handler );
	}

	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return hub.unregister( type, handler );
	}

	Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> getEventHandlers() {
		return hub.getEventHandlers();
	}

	protected EventHub getEventHub() {
		return hub;
	}

	public void register( String key, EventHandler<NodeEvent> handler ) {
		valueChangeHandlers.computeIfAbsent( key, ( k ) -> new CopyOnWriteArraySet<>() ).add( handler );
	}

	public void unregister( String key, EventHandler<? extends NodeEvent> handler ) {
		valueChangeHandlers.getOrDefault( key, Set.of() ).remove( handler );
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
			if( overwrite || getValue( key ) == null ) setValue( key, node.getValue( key ) );
		}

		return (T)this;
	}

	@Override
	public String toString() {
		return toString( false );
	}

	public String toString( String... keys ) {
		return toString( Arrays.asList( keys ) );
	}

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

	public String toString( List<String> keys ) {
		StringBuilder builder = new StringBuilder();

		boolean first = true;
		builder.append( getClass().getSimpleName() );
		builder.append( "[" );
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
		builder.append( "]" );

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

		return hashcode;
	}

	public <T extends Node> Comparator<T> getComparator() {
		return new NodeComparator<>( naturalKeyList );
	}

	protected void definePrimaryKey( String... keys ) {
		if( primaryKeyList == null ) {
			primaryKeyList = List.of( keys );
		} else {
			throw new IllegalStateException( "Primary key already set" );
		}
	}

	protected void defineNaturalKey( String... keys ) {
		if( naturalKeyList == null ) {
			naturalKeyList = List.of( keys );
		} else {
			throw new IllegalStateException( "Natural key already set" );
		}
	}

	protected void defineReadOnly( String... keys ) {
		if( readOnlySet == null ) {
			readOnlySet = Set.of( keys );
		} else {
			throw new IllegalStateException( "Read only keys already set" );
		}
	}

	protected boolean isReadOnly( String key ) {
		return readOnlySet != null && readOnlySet.contains( key );
	}

	protected void addModifyingKeys( String... keys ) {
		if( modifyingKeySet == null ) modifyingKeySet = new CopyOnWriteArraySet<>();
		modifyingKeySet.addAll( Set.of( keys ) );
	}

	protected void removeModifyingKeys( String... keys ) {
		if( modifyingKeySet == null ) return;
		modifyingKeySet.removeAll( Set.of( keys ) );
		if( modifyingKeySet.isEmpty() ) modifyingKeySet = null;
	}

	protected boolean isModifyingKey( String key ) {
		return modifyingKeySet != null && modifyingKeySet.contains( key );
	}

	protected Set<String> getValueKeys() {
		return values == null ? Collections.emptySet() : values.keySet();
	}

	@SuppressWarnings( "unchecked" )
	protected <T> Collection<T> getValues( Class<T> clazz ) {
		return (Collection<T>)getValueKeys().stream().map( this::getValue ).filter( clazz::isInstance ).collect( Collectors.toUnmodifiableSet() );
	}

	protected <T> T getValue( String key ) {
		return getValue( key, null );
	}

	@SuppressWarnings( "unchecked" )
	protected <T> T getValue( String key, T defaultValue ) {
		if( key == null ) throw new NullPointerException( "Value key cannot be null" );

		T value = values == null ? null : (T)values.get( key );

		return value != null ? value : defaultValue;
	}

	protected void setValue( String key, Object newValue ) {
		if( key == null ) throw new NullPointerException( "Value key cannot be null" );
		if( readOnlySet != null && readOnlySet.contains( key ) ) throw new IllegalStateException( "Attempt to set read-only value: " + key );

		try {
			Txn.create();
			Object oldValue = getValue( key );
			if( newValue instanceof Node ) checkForExistingParent( (Node)newValue );
			Txn.submit( new SetValueOperation( this, key, oldValue, newValue ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.log( Log.ERROR, "Error setting flag: " + key, exception );
		}
	}

	protected void clear() {
		try {
			Txn.create();
			getValueKeys().stream().sorted().forEach( k -> setValue( k, null ) );
			Txn.commit();
		} catch( TxnException exception ) {
			log.log( Log.ERROR, "Error clearing values", exception );
		}
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
	public int distanceTo( Node target ) {
		if( this == target ) return 0;

		int count = 0;
		Node node = this;
		while( node != null && node != target ) {
			count++;
			node = node.getParent();
		}

		return node == null ? -1 : count;
	}

	@SuppressWarnings( "unchecked" )
	public <T> T getParent() {
		return (T)parent;
	}

	void setParent( Node parent ) {
		checkForCircularReference( parent );
		this.parent = parent;
	}

	List<Node> getNodePath() {
		return getNodePath( null );
	}

	private List<Node> getNodePath( Node stop ) {
		List<Node> path = new ArrayList<>();
		if( this != stop && parent != null ) path = parent.getNodePath();
		path.add( this );
		return path;
	}

	void addEdge( Edge edge ) {
		if( edges == null ) edges = new CopyOnWriteArraySet<>();
		edges.add( edge );
	}

	void removeEdge( Edge edge ) {
		edges.remove( edge );
	}

	private void doSetSelfModified( boolean newValue ) {
		selfModified = newValue;

		if( !newValue ) {
			modifiedValues = null;
			modifiedChildren = null;
		}

		updateModified();
	}

	//	private <T> void doPutResource( String key, T oldValue, T newValue ) {
	//		if( newValue == null ) {
	//			if( resources != null ) {
	//				resources.remove( key );
	//				if( resources.size() == 0 ) resources = null;
	//			}
	//		} else {
	//			if( resources == null ) resources = new ConcurrentHashMap<>();
	//			resources.put( key, newValue );
	//		}
	//	}

	private void doSetValue( String key, Object oldValue, Object newValue ) {
		if( newValue == null ) {
			if( values == null ) return;
			values.remove( key );
			if( values.size() == 0 ) values = null;
			if( oldValue instanceof Node ) ((Node)oldValue).setParent( null );
		} else {
			if( values == null ) values = new ConcurrentHashMap<>();
			values.put( key, newValue );
			if( newValue instanceof Node ) ((Node)newValue).setParent( this );
		}

		updateModified();
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

	private void checkForExistingParent( Node child ) {
		Node parent = child.getParent();
		if( parent != null ) {
			parent.getValueKeys().forEach( k -> {
				if( parent.getValue( k ).equals( child ) ) parent.setValue( k, null );
			} );
		}
	}

	private void checkForCircularReference( Node node ) {
		Node parent = this;
		while( parent != null ) {
			if( node == parent ) throw new CircularReferenceException( "Circular reference detected in parent path: " + node );
			parent = parent.getParent();
		}
	}

	@Deprecated
	private Set<Edge> findEdges( Set<Edge> edges, Node source, Node target ) {
		Set<Edge> result = new HashSet<>();

		for( Edge edge : edges ) {
			if( edge.getSource() == source && edge.getTarget() == target ) result.add( edge );
			if( edge.getTarget() == source && edge.getSource() == target ) result.add( edge );
		}

		return result;
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
		 * @param event
		 */
		final void fireCascadingEvent( NodeEvent event ) {
			Node node = getNode();
			while( node != null ) {
				fireTargetedEvent( node, event );
				node = node.getParent();
			}
		}

		/**
		 * Fire a sliding event. A sliding event is where a new event of the same
		 * type is generated for the node and each parent.
		 *
		 * @param type
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

		private boolean oldValue;

		private boolean newValue;

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

		private String key;

		private Object oldValue;

		private Object newValue;

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
				if( childAdd ) fireCascadingEvent( new NodeEvent( getNode(), NodeEvent.CHILD_ADDED, key, oldValue, newValue ) );
				if( childRemove ) fireCascadingEvent( new NodeEvent( getNode(), NodeEvent.CHILD_REMOVED, key, oldValue, newValue ) );
				fireCascadingEvent( new NodeEvent( getNode(), NodeEvent.VALUE_CHANGED, key, oldValue, newValue ) );

				Txn.submit( updateModified );
			}

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

	//	private class SetResourceOperation extends NodeTxnOperation {
	//
	//		private String key;
	//
	//		private Object oldValue;
	//
	//		private Object newValue;
	//
	//		SetResourceOperation( Node node, String key, Object oldValue, Object newValue ) {
	//			super( node );
	//			this.key = key;
	//			this.oldValue = oldValue;
	//			this.newValue = newValue;
	//		}
	//
	//		@Override
	//		protected void commit() {
	//			putResource( key, oldValue, newValue );
	//			if( !Objects.equals( oldValue, newValue ) ) fireSlidingEvent( NodeEvent.NODE_CHANGED );
	//		}
	//
	//		@Override
	//		protected void revert() {
	//			putResource( key, newValue, oldValue );
	//		}
	//
	//		private void putResource( String key, Object oldValue, Object newValue ) {
	//			doPutResource( key, oldValue, newValue );
	//		}
	//	}

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

		private boolean oldValue;

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
