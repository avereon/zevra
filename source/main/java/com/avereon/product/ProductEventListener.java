package com.avereon.product;

import java.util.EventListener;

/**
 * @deprecated In favor of {@link com.avereon.event.EventHandler}
 * @param <T>
 */
@Deprecated
public interface ProductEventListener<T extends ProductEvent> extends EventListener {

	void handleEvent( T event );

}
