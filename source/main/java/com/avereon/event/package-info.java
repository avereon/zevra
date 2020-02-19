/**
 * A simple, yet powerful, general event system. Events for any kind of activity
 * can be defined and distributed with the classes in this package. Events are
 * defined by subclassing the {@link com.avereon.event.Event Event} class,
 * defining {@link com.avereon.event.EventType EventTypes} for that class
 * and distributing them with the {@link com.avereon.event.EventHub EventHub}
 * class. {@link com.avereon.event.EventHandler Handlers} can be registered to
 * handle only the event types it can handle and use of lambdas as the handler
 * implementation is encouraged.
 */
package com.avereon.event;