package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DelayedActionTest {

	private final AtomicLong actionTimestamp = new AtomicLong();

	private final Object actionLock = new Object();

	@Test
	void testAction() throws Exception {
		DelayedAction action = new DelayedAction();
		action.setAction( this::doAction );
		assertThat( actionTimestamp.get(), is( 0L ) );

		long before = System.currentTimeMillis();
		readyAction();
		action.trigger();
		waitForAction();
		long after = System.currentTimeMillis();

		assertThat( actionTimestamp.get(), both( greaterThanOrEqualTo( before ) ).and( lessThanOrEqualTo( after ) ) );
		assertThat( after - before, both( greaterThanOrEqualTo( 0L ) ).and( lessThanOrEqualTo( 50L ) ) );
	}

	@Test
	void testMinTriggerLimit() throws Exception {
		// To test the min trigger limit, trigger() needs to be called and then
		// update() needs to be called before the minTriggerLimit is reached

		long minTriggerLimit = 50;
		long maxTriggerLimit = minTriggerLimit * 2;

		DelayedAction action = new DelayedAction();
		action.setMinTriggerLimit( minTriggerLimit );
		action.setMaxTriggerLimit( maxTriggerLimit );
		action.setAction( this::doAction );
		assertThat( actionTimestamp.get(), is( 0L ) );

		long before = System.currentTimeMillis();
		action.trigger();

		// Wait just a moment so the internal timestamps will not be the same
		ThreadUtil.pause( 1 );

		// Update() can be called as many times before the minTriggerLimit but the
		// action should not occur unit the minTriggerLimit time has been reached
		readyAction();
		action.update();
		action.update();
		action.update();
		action.update();
		action.update();
		waitForAction();
		long after = System.currentTimeMillis();

		assertThat( actionTimestamp.get(), both( greaterThanOrEqualTo( before + minTriggerLimit ) ).and( lessThanOrEqualTo( after ) ) );
		assertThat( after - before, both( greaterThanOrEqualTo( minTriggerLimit ) ).and( lessThanOrEqualTo( 2 * minTriggerLimit ) ) );
	}

	@Test
	void testMaxTriggerLimit() throws Exception {
		// To test the max trigger limit, update() needs to be called after the
		// minTriggerLimit but before the maxTriggerLimit is reached

		long minTriggerLimit = 50;
		long maxTriggerLimit = minTriggerLimit * 2;

		DelayedAction action = new DelayedAction();
		action.setMinTriggerLimit( minTriggerLimit );
		action.setMaxTriggerLimit( maxTriggerLimit );
		action.setAction( this::doAction );
		assertThat( actionTimestamp.get(), is( 0L ) );

		action.trigger();
		long before = System.currentTimeMillis();

		// Wait at least minTriggerLimit before calling update
		ThreadUtil.pause( minTriggerLimit + 1 );

		readyAction();
		action.update();
		action.update();
		action.update();
		action.update();
		action.update();
		waitForAction();
		long after = System.currentTimeMillis();

		assertThat( actionTimestamp.get(), both( greaterThanOrEqualTo( before + maxTriggerLimit ) ).and( lessThanOrEqualTo( after ) ) );
		assertThat( after - before, both( greaterThanOrEqualTo( maxTriggerLimit ) ).and( lessThanOrEqualTo( 2 * maxTriggerLimit ) ) );
	}

	private void doAction() {
		synchronized( actionLock ) {
			this.actionTimestamp.set( System.currentTimeMillis() );
			actionLock.notifyAll();
		}
	}

	private void readyAction() {
		actionTimestamp.set( 0 );
	}

	private void waitForAction() throws InterruptedException, TimeoutException {
		long timeout = 1000;
		synchronized( actionLock ) {
			while( actionTimestamp.get() == 0 ) {
				long start = System.currentTimeMillis();
				actionLock.wait( timeout );
				long end = System.currentTimeMillis();
				if( end - start >= timeout ) throw new TimeoutException( "Timeout waiting for action" );
			}
		}
	}

}
