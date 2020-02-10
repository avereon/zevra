package com.avereon.data;

import java.util.EventListener;

@Deprecated
public interface NodeListener extends EventListener {

	void nodeEvent( NodeEvent event );

}
