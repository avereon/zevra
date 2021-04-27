package com.avereon.data;

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
@SuppressWarnings( { "SuspiciousToArrayCall" } )
class NodeSet<E extends Node> extends Node implements Set<E> {

	private static final String NODE_SET_MODIFY_FILTER = "node-set-modify-filter";

	private final String name;

	private Set<E> itemCache;

	private boolean dirtyCache;

	NodeSet( String key ) {
		this.name = key;
		setAllKeysModify();
		addExcludedModifyingKeys( NODE_SET_MODIFY_FILTER );
		itemCache = Set.of();
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
		boolean modified = addNodes( collection );
		if( modified ) dirtyCache = true;
		return modified;
	}

	@Override
	public boolean removeAll( Collection<?> collection ) {
		boolean modified = removeNodes( collection );
		if( modified ) dirtyCache = true;
		return modified;
	}

	@Override
	public boolean retainAll( Collection<?> c ) {
		boolean modified = retainNodes( c );
		if( modified ) dirtyCache = true;
		return modified;
	}

	@Override
	public boolean containsAll( Collection<?> collection ) {
		return getValues().containsAll( collection );
	}

	@Override
	public void clear() {
		if( !super.isEmpty() ) dirtyCache = true;
		super.clear();
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
		return getClass().getSimpleName() + "[" + this.name + "]";
	}

	void setSetModifyFilter( Function<Node, Boolean> filter ) {
		setValue( NODE_SET_MODIFY_FILTER, filter );
	}

	private Function<Node, Boolean> getSetModifyFilter() {
		return getValue( NODE_SET_MODIFY_FILTER );
	}

	@SuppressWarnings( "unchecked" )
	private Collection<E> getSetValues() {
		if( dirtyCache ) {
			Class<E> type = (Class<E>)getClass().getGenericSuperclass();
			itemCache = getValues().parallelStream().filter( type::isInstance ).map( e -> (E)e ).collect( Collectors.toSet() );
			dirtyCache = false;
		}
		return itemCache;
	}

}
