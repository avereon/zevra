package com.avereon.data;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
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
 * If a {@link List} is desired then a {@link Comparator< E >} must be supplied
 * to sort the elements by some attribute in the {@link Node}s. The
 * {@link NodeComparator} class is helpful to easily create a comparator based
 * on know value keys.
 *
 * @param <E> The type of {@link Node}s in the {@link NodeSet}
 */
@SuppressWarnings( { "NullableProblems", "SuspiciousToArrayCall" } )
class NodeSet<E extends Node> extends Node implements Set<E> {

	static final String PREFIX = "node-set-key-";

	NodeSet() {
		setAllKeysModify();
	}

	@Override
	public int size() {
		return super.size();
	}

	@Override
	public boolean isEmpty() {
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
		return super.addNodes( Set.of( node ) );
	}

	@Override
	public boolean remove( Object object ) {
		return super.removeNodes( Set.of( object ) );
	}

	@Override
	public boolean containsAll( Collection<?> collection ) {
		return getValues().containsAll( collection );
	}

	@Override
	public boolean addAll( Collection<? extends E> collection ) {
		return super.addNodes( collection );
	}

	@Override
	public boolean retainAll( Collection<?> c ) {
		return super.retainNodes( c );
	}

	@Override
	public boolean removeAll( Collection<?> collection ) {
		return super.removeNodes( collection );
	}

	@Override
	public void clear() {
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

	@SuppressWarnings( "unchecked" )
	private Collection<E> getSetValues() {
		return (Collection<E>)getValues();
	}

	public static <T> Set<T> of() {
		return Set.of();
	}

}
