package com.avereon.util;

import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * This class is used to create type reference objects that can be used
 * to reference a parameterized type in special circumstances. One example
 * is for use when unmarshalling complex data types:
 * <p>
 * Settings example:
 * <pre>
 * Map&lt;String,PojoBean&gt; beans = settings.get( "beans", new TypeReference&lt;Map&lt;String,PojoBean&gt;&gt;(){} );
 * </pre>
 *
 * @param <T> The parameterized type being defined for use
 */
@Getter
public abstract class TypeReference<T> implements Comparable<TypeReference<T>> {

	private final Type type;

	private final Class<T> typeClass;

	/**
	 * This constructor is for use in circumstances where a new TypeReference
	 * subclass is defined in order to reference the parameterized type at
	 * runtime.
	 */
	@SuppressWarnings( "unchecked" )
	protected TypeReference() {
		Type superClass = this.getClass().getGenericSuperclass();
		if( superClass instanceof Class ) throw new IllegalArgumentException( getClass().getSimpleName() + " created without type information" );

		this.type = ((ParameterizedType)superClass).getActualTypeArguments()[ 0 ];

		Class<T> reference = null;
		try {
			String name = this.type.getTypeName();
			int index = name.indexOf( ("<") );
			if( index < 0 ) {
				reference = (Class<T>)Class.forName( name );
			} else {
				reference = (Class<T>)Class.forName( name.substring( 0, index ) );
			}
		} catch( ClassNotFoundException exception ) {
			// Should never happen
		} finally {
			this.typeClass = reference;
		}

	}

	/**
	 * This constructor is for use in circumstances where the type to be
	 * referenced is not parameterized and can be referenced by the class.
	 *
	 * @param type The type to reference
	 */
	protected TypeReference( Class<T> type ) {
		this.type = type;
		this.typeClass = type;
	}

	/**
	 * Compares this TypeReference object with the specified TypeReference object
	 * for order. The comparison is based on the string representation of the
	 * reference class.
	 *
	 * @param that The TypeReference object to be compared.
	 * @return A negative integer, zero, or a positive integer as this TypeReference object
	 * is less than, equal to, or greater than the specified TypeReference object.
	 */
	public int compareTo( TypeReference<T> that ) {
		return this.getType().getTypeName().compareTo( that.getType().getTypeName() );
	}

	/**
	 * Returns the string representation of the reference class.
	 *
	 * @return The string representation of the reference class.
	 */
	@Override
	public String toString() {
		return typeClass.getName();
	}

}
