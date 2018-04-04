package com.xeomar.util;

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
public abstract class TypeReference<T> implements Comparable<TypeReference<T>> {

	private final Type type;

	private final Class<T> referenceClass;

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
			this.referenceClass = reference;
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
		this.referenceClass = type;
	}

	public Type getType() {
		return this.type;
	}

	public Class<T> getTypeClass() {
		return referenceClass;
	}

	public int compareTo( TypeReference<T> that ) {
		return this.getType().getTypeName().compareTo( that.getType().getTypeName() );
	}

}
