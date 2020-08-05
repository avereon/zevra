package com.avereon.data;

import com.avereon.util.Log;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;

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
 * If a {@link List} is desired then a {@link Comparator<T>} must be supplied
 * to sort the elements by some attribute in the {@link Node}s. The
 * {@link NodeComparator} class is helpful to easily create a comparator based
 * on know value keys.
 *
 * @param <T> The type of {@link Node}s in the {@link NodeSet}
 */
class NodeSet<T extends Node> extends Node implements Set<T> {

	static final String PREFIX = "node-set-key-";

	private static final System.Logger log = Log.get();

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
	public Iterator<T> iterator() {
		return getSetValues().iterator();
	}

	@Override
	public void forEach( Consumer<? super T> action ) {
		getSetValues().forEach( action );
	}

	@Override
	public Object[] toArray() {
		return getSetValues().toArray();
	}

	@Override
	public <T1> T1[] toArray( T1[] array ) {
		return getSetValues().toArray( array );
	}

	@Override
	public <T1> T1[] toArray( IntFunction<T1[]> generator ) {
		return getSetValues().toArray( generator );
	}

	@Override
	public boolean add( T node ) {
		String key = node.getCollectionId();
		Objects.requireNonNull( key );
		if( super.hasKey( key ) ) return false;
		setValue( key, node );
		return true;
	}

	@Override
	public boolean remove( Object object ) {
		if( !(object instanceof Node) ) return false;
		Node node = (Node)object;
		String key = node.getCollectionId();
		if( !super.hasKey( key ) ) return false;
		setValue( key, null );
		return true;
	}

	@Override
	public boolean containsAll( Collection<?> collection ) {
		return getValues().containsAll( collection );
	}

	@Override
	public boolean addAll( Collection<? extends T> collection ) {
		return super.addNodes( collection );
	}

	@Override
	public boolean retainAll( Collection<?> c ) {
		// TODO Implement retainAll()
		return false;
	}

	@Override
	public boolean removeAll( Collection<?> collection ) {
		return super.removeNodes( collection );
	}

	@Override
	public void clear() {
		super.clear();
	}

	//	@Override
	//	public Spliterator<T> spliterator() {
	//		return getSetValues().spliterator();
	//	}
	//
	//	@Override
	//	public Stream<T> stream() {
	//		return getSetValues().stream();
	//	}
	//
	//	@Override
	//	public Stream<T> parallelStream() {
	//		return getSetValues().parallelStream();
	//	}

	@SuppressWarnings( "unchecked" )
	private Collection<T> getSetValues() {
		return (Collection<T>)getValues();
	}

	@SuppressWarnings("unchecked")
	public static <T> Set<T> of() {
		return Set.of();
	}

}
