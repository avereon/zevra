package com.avereon.index;

import com.avereon.result.Result;
import com.avereon.util.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
		Document document = Document.of( new StringReader( "" ) );

		indexer.start();
		Result<Future<?>> result = indexer.submit( document );
		indexer.stop();

		assertTrue( result.isSuccessful() );
	}

	@Test
	void testParse() throws Exception {
		String text = "This is some arbitrary content";
		Document document = Document.of( new StringReader( text ) );

		indexer.start();
		Result<Future<?>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		// Check the hits
		assertThat( indexer.getIndex().orElseThrow().getHits( "this" ), contains( Hit.builder().context( text ).line( 0 ).index( 0 ).word( "this" ).length( 4 ).document( document ).build() ) );
		assertThat( indexer.getIndex().orElseThrow().getHits( "is" ), contains( Hit.builder().context( text ).line( 0 ).index( 5 ).word( "is" ).length( 2 ).document( document ).build() ) );
		assertThat( indexer.getIndex().orElseThrow().getHits( "some" ), contains( Hit.builder().context( text ).line( 0 ).index( 8 ).word( "some" ).length( 4 ).document( document ).build() ) );
		assertThat( indexer.getIndex().orElseThrow().getHits( "arbitrary" ), contains( Hit.builder().context( text ).line( 0 ).index( 13 ).word( "arbitrary" ).length( 9 ).document( document ).build() ) );
		assertThat( indexer.getIndex().orElseThrow().getHits( "content" ), contains( Hit.builder().context( text ).line( 0 ).index( 23 ).word( "content" ).length( 7 ).document( document ).build() ) );

		// Check the dictionary
		assertThat( indexer.getIndex().orElseThrow().getDictionary(), containsInAnyOrder( "this", "is", "some", "arbitrary", "content" ) );
	}

	@Test
	void testParseWithMultipleLinesPunctuationAndExtraWhitespace() throws Exception {
		String line0 = "This is";
		String line1 = "some \"arbitrary content\"!";
		String text = " " + line0 + "\n" + line1 + " ";
		Document document = Document.of( new StringReader( text ) );

		indexer.start();
		Result<Future<?>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		// Check the hits
		assertThat( indexer.getIndex().orElseThrow().getHits( "this" ), contains( Hit.builder().context( line0 ).line( 0 ).index( 0 ).word( "this" ).length( 4 ).document( document ).build() ) );
		assertThat( indexer.getIndex().orElseThrow().getHits( "is" ), contains( Hit.builder().context( line0 ).line( 0 ).index( 5 ).word( "is" ).length( 2 ).document( document ).build() ) );
		assertThat( indexer.getIndex().orElseThrow().getHits( "some" ), contains( Hit.builder().context( line1 ).line( 1 ).index( 0 ).word( "some" ).length( 4 ).document( document ).build() ) );
		assertThat( indexer.getIndex().orElseThrow().getHits( "arbitrary" ), contains( Hit.builder().context( line1 ).line( 1 ).index( 6 ).word( "arbitrary" ).length( 9 ).document( document ).build() ) );
		assertThat( indexer.getIndex().orElseThrow().getHits( "content" ), contains( Hit.builder().context( line1 ).line( 1 ).index( 16 ).word( "content" ).length( 7 ).document( document ).build() ) );

		// Check the dictionary
		assertThat( indexer.getIndex().orElseThrow().getDictionary(), containsInAnyOrder( "this", "is", "some", "arbitrary", "content" ) );
	}

	@Test
	void testSearch() throws Exception {
		String text = "This is some \"arbitrary content\".";
		Document document = Document.of( new StringReader( text ) );

		indexer.start();
		Result<Future<?>> result = indexer.submit( document );
		indexer.stop();

		result.get().get();

		assertThat( indexer.getIndex().orElseThrow().getDictionary(), containsInAnyOrder( "this", "is", "some", "arbitrary", "content" ) );

		String scope = Index.DEFAULT;
		String word = "content";
		List<Hit> hits = indexer
			.getIndex( scope )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + scope ) )
			.search( IndexQuery.builder().text( word ).build() )
			.orElseThrow( () -> new NoSuchElementException( "No documents found for: " + word ) );

		assertThat( hits.get( 0 ).document(), is( document ) );
		assertThat( hits.size(), is( 1 ) );
	}

}
