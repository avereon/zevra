package com.xeomar.product;

import java.util.EventListener;

public interface ProductEventListener<T extends ProductEvent> extends EventListener {

	void handleEvent( T event );

}
