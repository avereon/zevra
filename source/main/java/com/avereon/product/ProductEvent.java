package com.avereon.product;

import com.avereon.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

public class ProductEvent extends EventObject {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public ProductEvent( Object source ) {
		super( source );
	}

	@SuppressWarnings( "unchecked" )
	public ProductEvent fire( Set<? extends ProductEventListener> listeners ) {
		for( ProductEventListener listener : new HashSet<>( listeners ) ) {
			try {
				listener.handleEvent( this );
			} catch( Throwable throwable ) {
				log.error( "Error dispatching event: " + toString(), throwable );
			}
		}
		return this;
	}

	public String toString() {
		String sourceClass = getSource().getClass().getSimpleName();
		String eventClass = getClass().getSimpleName();
		return sourceClass + ":" + eventClass;
	}

}
