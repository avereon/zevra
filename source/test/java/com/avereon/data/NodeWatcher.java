package com.avereon.data;

import com.avereon.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodeWatcher implements EventHandler<NodeEvent> {

	private final List<NodeEvent> events = new CopyOnWriteArrayList<>();

	@Override
	public void handle( NodeEvent event ) {
		events.add( event );
	}

	public List<NodeEvent> getEvents() {
		return new ArrayList<>( events );
	}

	void reset() {
		events.clear();
	}

}
