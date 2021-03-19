package com.avereon.util;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class generalizes the concept of delayed actions that are performed
 * after some amount of "idle" time has occurred. This class also supports
 * minimum and maximum action requirements.
 */
public class DelayedAction {

	/**
	 * Action will be triggered at most this often.
	 */
	private static final long DEFAULT_MIN_TRIGGER_LIMIT = 100;

	/**
	 * Action will be triggered at least this often.
	 */
	private static final long DEFAULT_MAX_TRIGGER_LIMIT = 500;

	private static final Timer timer = new Timer( DelayedAction.class.getSimpleName() + "-Timer", true );

	private final AtomicLong lastChangeTime = new AtomicLong();

	private final AtomicLong lastActionTime = new AtomicLong();

	private final AtomicLong lastDirtyTime = new AtomicLong();

	private final Object scheduleLock = new Object();

	private long minTriggerLimit = DEFAULT_MIN_TRIGGER_LIMIT;

	private long maxTriggerLimit = DEFAULT_MAX_TRIGGER_LIMIT;

	private ExecutorService executor;

	private ActionTask task;

	private Runnable action;

	public DelayedAction() {
		this( null, null );
	}

	public DelayedAction( ExecutorService executor ) {
		this( executor, null );
	}

	public DelayedAction( Runnable action ) {
		this( null, action );
	}

	public DelayedAction( ExecutorService executor, Runnable action ) {
		this.executor = executor;
		this.action = action;
	}

	public long getMinTriggerLimit() {
		return minTriggerLimit;
	}

	public void setMinTriggerLimit( long minTriggerLimit ) {
		this.minTriggerLimit = minTriggerLimit;
	}

	public long getMaxTriggerLimit() {
		return maxTriggerLimit;
	}

	public void setMaxTriggerLimit( long maxTriggerLimit ) {
		this.maxTriggerLimit = maxTriggerLimit;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor( ExecutorService executor ) {
		this.executor = executor;
	}

	public Runnable getAction() {
		return action;
	}

	public void setAction( Runnable action ) {
		this.action = action;
	}

	// Called when the dirty time needs to be updated, commonly due to state being changed.
	public void update() {
		lastChangeTime.set( System.currentTimeMillis() );
		if( lastDirtyTime.get() <= lastActionTime.get() ) lastDirtyTime.set( lastChangeTime.get() );
		schedule();
	}

	public void schedule() {
		doSchedule( false );
	}

	public void trigger() {
		doSchedule( true );
	}

	public void reset() {
		lastActionTime.set( System.currentTimeMillis() );
	}

	public void cancel() {
		synchronized( scheduleLock ) {
			if( task != null ) task.cancel();
		}
	}

	private void doSchedule( boolean immediate ) {
		synchronized( scheduleLock ) {
			long now = System.currentTimeMillis();
			long actionTime = lastActionTime.get();
			long changeTime = lastChangeTime.get();
			long dirtyTime = lastDirtyTime.get();
			long minNext = changeTime + minTriggerLimit;
			long maxNext = Math.max( dirtyTime, actionTime ) + maxTriggerLimit;

			boolean changesSinceLastAction = dirtyTime > actionTime;

			//System.out.println( "da=" +(dirtyTime - actionTime) + " ua=" + (updateTime - actionTime));

			// If there are no changes since the last action time just return
			if( !immediate && !changesSinceLastAction ) return;

			long nextTime = Math.min( minNext, maxNext );
			long taskTime = task == null ? 0 : task.scheduledExecutionTime();

			//System.out.println( "task=" + task + " taskTime=" + (taskTime - actionTime) + " nextTime=" + (nextTime - actionTime) );

			// If the existing task time is already set to the next time just return
			if( !immediate && (taskTime == nextTime) ) return;

			// Cancel the existing task and schedule a new one
			if( task != null ) task.cancel();
			timer.schedule( task = new ActionTask(), immediate ? 0 : nextTime - now );
		}
	}

	private void fire() {
		reset();
		if( action != null ) action.run();
	}

	private class ActionTask extends TimerTask {

		@Override
		public void run() {
			// If there is an executor, use it to run the task, otherwise run the task on the timer thread
			if( executor != null && !executor.isShutdown() ) {
				executor.submit( DelayedAction.this::fire );
			} else {
				fire();
			}
		}

	}

}
