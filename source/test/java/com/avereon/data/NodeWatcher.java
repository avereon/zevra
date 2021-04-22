package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodeWatcher implements EventHandler<Event> {

	private final List<Event> events = new CopyOnWriteArrayList<>();

	@Override
	public void handle( Event event ) {
		events.add( event );
	}

	public List<Event> getEvents() {
		return new ArrayList<>( events );
	}

	void reset() {
		events.clear();
	}

}
