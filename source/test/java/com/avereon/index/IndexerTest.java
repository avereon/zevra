package com.avereon.index;

import com.avereon.result.Result;
import com.avereon.util.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexerTest {

	private Indexer indexer;

	@BeforeEach
	void setup() throws IOException {
		Path indexPath = FileUtil.createTempFolder( "IndexerTest" );
		indexer = new Indexer( indexPath );
	}

	@Test
	void testIsRunning() {
		assertFalse( indexer.isRunning() );
		indexer.start();
		assertTrue( indexer.isRunning() );
		indexer.stop();
		assertFalse( indexer.isRunning() );
	}

	@Test
	void testSubmit() {
		Document document = new Document();

		indexer.start();
		Result<Future<?>> result = indexer.submit( document );
		indexer.stop();

		assertTrue( result.isSuccessful() );
	}

}
