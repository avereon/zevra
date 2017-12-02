package com.xeomar.product;

import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

public class ProductEvent extends EventObject {

	private static Logger log = LogUtil.get( ProductEvent.class );

	private Object source;

	public ProductEvent( Object source ) {
		super( source );
	}

	@SuppressWarnings( "unchecked" )
	public void fire( Set<? extends ProductEventListener> listeners ) {
		for( ProductEventListener listener : new HashSet<>( listeners ) ) {
			try {
				listener.handleEvent( this );
			} catch( Throwable throwable ) {
				log.error( "Error dispatching event: " + toString(), throwable );
			}
		}
	}

	public String toString() {
		String sourceClass = getSource().getClass().getSimpleName();
		String eventClass = getClass().getSimpleName();
		return sourceClass + ":" + eventClass;
	}

}
