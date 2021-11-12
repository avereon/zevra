package com.avereon.index;

import com.avereon.result.Result;
import com.avereon.skill.Controllable;
import lombok.CustomLog;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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

	public Result<Future<Result<Set<Hit>>>> submit( Document document ) {
		if( !isRunning() ) return Result.of( new IllegalStateException( "Indexer not running" ) );
		return Result.of( executor.submit( () -> doIndex( document ) ) );
	}

	public Result<Set<Future<Result<Set<Hit>>>>> submit( Document... documents ) {
		if( !isRunning() ) return Result.of( new IllegalStateException( "Indexer not running" ) );
		return Result.of( Arrays.stream( documents ).map( d -> executor.submit( () -> doIndex( d ) ) ).collect( Collectors.toSet() ) );
	}

	public Optional<Index> getIndex() {
		return getIndex( Index.DEFAULT );
	}

	public Optional<Index> getIndex( String scope ) {
		return Optional.ofNullable( indexes.get( scope ) );
	}

	private Result<Set<Hit>> doIndex( Document document ) {
		Index index = indexes.computeIfAbsent( Index.DEFAULT, k -> new Index() );

		// Add document words to the index
		return new DefaultDocumentParser().index( document ).ifSuccess( index::push ).ifFailure( e -> log.atWarn( e ).log( "Unable to parse document: %s", document ) );
	}

}
