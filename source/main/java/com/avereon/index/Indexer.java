package com.avereon.index;

import com.avereon.result.Result;
import com.avereon.skill.Controllable;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Indexer implements Controllable<Indexer> {

	// A thread pool to run indexing tasks
	private ExecutorService executor;

	// A path to place index files
	private Path indexPath;

	public Indexer( Path indexPath ) {
		this.indexPath = indexPath;
	}

	@Override
	public boolean isRunning() {
		return executor != null && !executor.isShutdown();
	}

	@Override
	public Indexer start() {
		executor = Executors.newCachedThreadPool();
		// Start should read exising indexes
		return this;
	}

	@Override
	public Indexer stop() {
		if( executor != null ) executor.shutdown();
		return this;
	}

	public Result<Future<?>> submit( Document document ) {
		if( !isRunning() ) return Result.of( new IllegalStateException( "Indexer not running" ) );
		Future<?> future = executor.submit( () -> doIndex( document ) );
		return Result.of( future );
	}

	private void doIndex( Document document ) {

	}

}
