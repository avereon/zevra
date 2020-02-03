package com.avereon.data;

import java.util.EventListener;

public interface NodeListener extends EventListener {

	void nodeEvent( NodeEvent event );

}
