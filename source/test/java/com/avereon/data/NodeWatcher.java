package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodeWatcher implements EventHandler<Event> {

	private final List<Event> events = new CopyOnWriteArrayList<>();

	private final Map<Event, Throwable> traces = new ConcurrentHashMap<>();

	@Override
	public void handle( Event event ) {
		events.add( event );
		traces.put( event, new Throwable( String.valueOf( event ) ) );
	}

	public List<Event> getEvents() {
		return new ArrayList<>( events );
	}

	public Throwable getTrace( Event event ) {
		return traces.get( event );
	}

	void reset() {
		events.clear();
	}

}
