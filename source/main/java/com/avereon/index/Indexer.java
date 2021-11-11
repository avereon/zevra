package com.avereon.index;

import com.avereon.result.Result;
import com.avereon.skill.Controllable;
import lombok.CustomLog;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@CustomLog
public class Indexer implements Controllable<Indexer> {

	// A thread pool to run indexing tasks
	private ExecutorService executor;

	// A path to place index files
	private final Path indexPath;

	private final Map<String, Index> indexes;

	public Indexer( Path indexPath ) {
		this.indexPath = indexPath;
		this.indexes = new ConcurrentHashMap<>();
	}

	@Override
	public boolean isRunning() {
		return executor != null && !executor.isShutdown();
	}

	@Override
	public Indexer start() {
		executor = Executors.newCachedThreadPool();
		// Start should load exising indexes
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

	public Optional<Index> getIndex() {
		return getIndex( Index.DEFAULT );
	}

	public Optional<Index> getIndex( String scope ) {
		return Optional.ofNullable( indexes.get( scope ) );
	}

	private Result<?> doIndex( Document document ) {
		Index index = indexes.computeIfAbsent( Index.DEFAULT, k -> new Index() );

		// TODO Also add tag words to the index
		// NOTE Should there be different data scopes in an index?
		// For example, should tags be be treated with higher priority than words?

		return new DefaultDocumentParser().parse( document )
			.ifSuccess( index::push )
			.ifFailure( e -> log.atWarn( e ).log( "Unable to parse document: %s", document ) );
	}

}
