package com.avereon.data;

import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnException;
import lombok.CustomLog;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is the internal implementation of a {@link Node} {@link Set}. In
 * general, implementers should not need to directly work with this class, but
 * its intended used is documented here for reference. Take, for example, an
 * Organization model that allows an arbitrary number of Person nodes. Both
 * Organization and Person should extend from {@link Node} or {@link IdNode}.
 * <pre>
 *   public class Organization extends IdNode {
 *     ...
 *   }
 *   public class Person extends IdNode {
 *     ...
 *   }
 * </pre>
 * When adding the ability to get, add and remove Person objects to the
 * Organization class, the following pattern is typically used:
 * <pre>
 *   public class Organization extends IdNode {
 *     ...
 *     public Set<Person> getPersons() {
 *       return getValues( PERSONS );
 *     }
 *     public void addPerson( Person person ) {
 *       addToSet( PERSONS, person );
 *     }
 *     public void removePerson( Person person ) {
 *       removeFromSet( PERSONS, person );
 *     }
 *     ...
 *   }
 * </pre>
 * If a {@link List} is desired then a {@link Comparator<E>} must be supplied
 * to sort the elements by some attribute in the {@link Node}s. The
 * {@link NodeComparator} class is helpful to easily create a comparator based
 * on know value keys.
 *
 * @param <E> The type of {@link Node}s in the {@link NodeSet}
 */
@CustomLog
@SuppressWarnings( { "SuspiciousToArrayCall" } )
class NodeSet<E extends Node> extends Node implements Set<E> {

	private static final String NODE_SET_MODIFY_FILTER = "node-set-modify-filter";

	private static final boolean USE_CACHE = true;

	private final String key;

	private Set<E> itemCache;

	private boolean dirtyCache;

	private Node priorParent;

	NodeSet( String key ) {
		this.key = key;
		setAllKeysModify();
		addExcludedModifyingKeys( NODE_SET_MODIFY_FILTER );
		itemCache = Set.of();
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public <T> T setValue( String key, T newValue ) {
		if( newValue instanceof Node && getParent() == null ) {
			priorParent.getValue( this.key, () -> priorParent.doSetValue( this.key, null, this ) ).add( (E)newValue );
		} else {
			super.setValue( this.key, key, newValue );
		}

		if( getParent() == null ) log.atWarning().log( "Setting value on detached node set" );

		dirtyCache = true;
		return newValue;
	}

	@Override
	public int size() {
		return getSetValues().size();
	}

	@Override
	public boolean isEmpty() {
		return getSetValues().isEmpty();
	}

	boolean isNodeSetEmpty() {
		return super.isEmpty();
	}

	@Override
	public boolean contains( Object object ) {
		return getSetValues().contains( object );
	}

	@Override
	public Iterator<E> iterator() {
		return getSetValues().iterator();
	}

	@Override
	public void forEach( Consumer<? super E> action ) {
		getSetValues().forEach( action );
	}

	@Override
	public Object[] toArray() {
		return getSetValues().toArray();
	}

	@Override
	public <T> T[] toArray( T[] array ) {
		return getSetValues().toArray( array );
	}

	@Override
	public <T> T[] toArray( IntFunction<T[]> generator ) {
		return getSetValues().toArray( generator );
	}

	@Override
	public boolean add( E node ) {
		return addAll( Set.of( node ) );
	}

	@Override
	public boolean remove( Object object ) {
		return removeAll( Set.of( object ) );
	}

	@Override
	public boolean addAll( Collection<? extends E> collection ) {
		boolean modified = addNodes( key, collection );
		if( modified ) dirtyCache = true;
		return modified;
	}

	private boolean addNodes( String setKey, Collection<? extends Node> collection ) {
		boolean changed = false;

		try( Txn ignored = Txn.create() ) {
			for( Node node : collection ) {
				String key = node.getCollectionId();
				if( hasKey( key ) ) continue;
				Txn.submit( new SetValueOperation( this, setKey, key, null, node ) );
				changed = true;
			}
		} catch( TxnException exception ) {
			log.atSevere().withCause( exception ).log( "Error adding collection" );
			return false;
		}
		return changed;
	}

	@Override
	public boolean removeAll( Collection<?> collection ) {
		boolean modified = removeNodes( key, collection );
		if( modified ) dirtyCache = true;
		return modified;
	}

	private boolean removeNodes( String setKey, Collection<?> collection ) {
		boolean changed = false;
		try( Txn ignored = Txn.create() ) {
			for( Object object : collection ) {
				if( !(object instanceof Node) ) continue;
				Node node = (Node)object;
				String key = node.getCollectionId();
				if( !hasKey( key ) ) continue;
				Txn.submit( new SetValueOperation( this, setKey, key, node, null ) );
				changed = true;
			}
		} catch( TxnException exception ) {
			log.atSevere().withCause( exception ).log( "Error removing collection" );
			return false;
		}
		return changed;
	}

	@Override
	public boolean retainAll( Collection<?> c ) {
		boolean modified = retainNodes( key, c );
		if( modified ) dirtyCache = true;
		return modified;
	}

	@SuppressWarnings( "SuspiciousMethodCalls" )
	private boolean retainNodes( String setKey, Collection<?> collections ) {
		if( collections.size() == 0 ) return false;
		Collection<?> remaining = getValues();
		int originalSize = remaining.size();
		remaining.removeAll( collections );
		removeNodes( setKey, remaining );
		return remaining.size() != originalSize;
	}

	@Override
	public boolean containsAll( Collection<?> collection ) {
		return getValues().containsAll( collection );
	}

	@Override
	public void clear() {
		if( !super.isEmpty() ) dirtyCache = true;
		Txn.run( () -> getValueKeys().stream().sorted().forEach( k -> setValue( key, k, null ) ) );
	}

	@Override
	public Spliterator<E> spliterator() {
		return getSetValues().spliterator();
	}

	@Override
	public Stream<E> stream() {
		return getSetValues().stream();
	}

	@Override
	public Stream<E> parallelStream() {
		return getSetValues().parallelStream();
	}

	public boolean modifyAllowed( Object value ) {
		if( !(value instanceof Node) ) return super.modifyAllowed( value );
		Function<Node, Boolean> filter = getSetModifyFilter();
		return filter == null || filter.apply( (Node)value );
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + this.key + "]";
	}

	@Override
	void doSetParent( Node parent ) {
		if( parent != null ) this.priorParent = parent;
		super.doSetParent( parent );
	}

	void setSetModifyFilter( Function<Node, Boolean> filter ) {
		setValue( NODE_SET_MODIFY_FILTER, filter );
	}

	private Function<Node, Boolean> getSetModifyFilter() {
		return getValue( NODE_SET_MODIFY_FILTER );
	}

	@SuppressWarnings( "unchecked" )
	private Collection<E> getSetValues() {
		if( USE_CACHE ) {
			if( dirtyCache ) {
				Class<E> type = (Class<E>)getClass().getGenericSuperclass();
				itemCache = getValues().parallelStream().filter( type::isInstance ).map( e -> (E)e ).collect( Collectors.toSet() );
				dirtyCache = false;
			}
			return itemCache;
		} else {
			Class<E> type = (Class<E>)getClass().getGenericSuperclass();
			return getValues().parallelStream().filter( type::isInstance ).map( e -> (E)e ).collect( Collectors.toSet() );
		}
	}

}
