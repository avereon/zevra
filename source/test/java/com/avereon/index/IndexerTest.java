package com.avereon.index;

import com.avereon.result.Result;
import com.avereon.util.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexerTest {

	private static final String sample = "The quick brown fox jumps over the lazy dog";

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
		Document document = new Document( URI.create( "" ), "", new StringReader( "" ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();

		assertTrue( result.isSuccessful() );
	}

	@Test
	void testParse() throws Exception {
		String name = "Document";
		String text = "This is some arbitrary content";
		Document document = new Document( URI.create( "" ), name, new StringReader( text ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		// Check the hits
		assertThat( indexer.getIndex().orElseThrow().getHits( "document" ),
			contains( Hit.builder().context( name ).line( 0 ).index( 0 ).word( "document" ).length( 8 ).document( document ).priority( 1 ).build() )
		);
		assertThat( indexer.getIndex().orElseThrow().getHits( "this" ),
			contains( Hit.builder().context( text ).line( 0 ).index( 0 ).word( "this" ).length( 4 ).document( document ).priority( 2 ).build() )
		);
		assertThat( indexer.getIndex().orElseThrow().getHits( "is" ), contains( Hit.builder().context( text ).line( 0 ).index( 5 ).word( "is" ).length( 2 ).document( document ).priority( 2 ).build() ) );
		assertThat( indexer.getIndex().orElseThrow().getHits( "some" ),
			contains( Hit.builder().context( text ).line( 0 ).index( 8 ).word( "some" ).length( 4 ).document( document ).priority( 2 ).build() )
		);
		assertThat( indexer.getIndex().orElseThrow().getHits( "arbitrary" ),
			contains( Hit.builder().context( text ).line( 0 ).index( 13 ).word( "arbitrary" ).length( 9 ).document( document ).priority( 2 ).build() )
		);
		assertThat( indexer.getIndex().orElseThrow().getHits( "content" ),
			contains( Hit.builder().context( text ).line( 0 ).index( 23 ).word( "content" ).length( 7 ).document( document ).priority( 2 ).build() )
		);

		// Check the dictionary
		assertThat( indexer.getIndex().orElseThrow().getDictionary(), containsInAnyOrder( "document", "this", "is", "some", "arbitrary", "content" ) );
	}

	@Test
	void testParseWithMultipleLinesPunctuationAndExtraWhitespace() throws Exception {
		String name = "This,  A  Document! ";
		String line0 = " This, is";
		String line1 = "some \"arbitrary content\". ";
		String text = line0 + "\n" + line1;
		Document document = new Document( URI.create( "" ), name, new StringReader( text ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		// Check the hits
		assertThat( indexer.getIndex().orElseThrow().getHits( "document" ),
			contains( Hit.builder().context( name.trim() ).line( 0 ).index( 10 ).word( "document" ).length( 8 ).document( document ).priority( 1 ).build() )
		);
		assertThat( indexer.getIndex().orElseThrow().getHits( "this" ),
			containsInAnyOrder( Hit.builder().context( name.trim() ).line( 0 ).index( 0 ).word( "this" ).length( 4 ).document( document ).priority( 1 ).build(),
				Hit.builder().context( line0.trim() ).line( 0 ).index( 0 ).word( "this" ).length( 4 ).document( document ).priority( 2 ).build()
			)
		);
		assertThat(
			indexer.getIndex().orElseThrow().getHits( "is" ),
			contains( Hit.builder().context( line0.trim() ).line( 0 ).index( 6 ).word( "is" ).length( 2 ).document( document ).priority( 2 ).build() )
		);
		assertThat( indexer.getIndex().orElseThrow().getHits( "some" ),
			contains( Hit.builder().context( line1.trim() ).line( 1 ).index( 0 ).word( "some" ).length( 4 ).document( document ).priority( 2 ).build() )
		);
		assertThat( indexer.getIndex().orElseThrow().getHits( "arbitrary" ),
			contains( Hit.builder().context( line1.trim() ).line( 1 ).index( 6 ).word( "arbitrary" ).length( 9 ).document( document ).priority( 2 ).build() )
		);
		assertThat( indexer.getIndex().orElseThrow().getHits( "content" ),
			contains( Hit.builder().context( line1.trim() ).line( 1 ).index( 16 ).word( "content" ).length( 7 ).document( document ).priority( 2 ).build() )
		);

		// Check the dictionary
		assertThat( indexer.getIndex().orElseThrow().getDictionary(), containsInAnyOrder( "a", "document", "this", "is", "some", "arbitrary", "content" ) );
	}

	@Test
	void testSearch() throws Exception {
		String text = "This is some \"arbitrary content\".";
		Document document = new Document( URI.create( "" ), "", new StringReader( text ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();

		result.get().get();

		assertThat( indexer.getIndex().orElseThrow().getDictionary(), containsInAnyOrder( "this", "is", "some", "arbitrary", "content" ) );

		String scope = Index.DEFAULT;
		String word = "content";

		List<Hit> hits = indexer
			.getIndex( scope )
			.map( i -> new FuzzySearch().search( i, IndexQuery.builder().text( word ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + scope ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found for: " + word ) );

		assertThat( hits.get( 0 ).document(), is( document ) );
		assertThat( hits.size(), is( 1 ) );
	}

	@Test
	void testSearchWithTags() throws Exception {
		Document document = new Document( URI.create( "" ), "", new StringReader( "" ) );
		document.tags( Set.of( "help", "empty" ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();

		result.get().get();

		assertThat( indexer.getIndex().orElseThrow().getDictionary(), containsInAnyOrder( "help", "empty" ) );

		String scope = Index.DEFAULT;
		String word = "help";

		List<Hit> hits = indexer
			.getIndex( scope )
			.map( i -> new FuzzySearch().search( i, IndexQuery.builder().text( word ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + scope ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found for: " + word ) );

		assertThat( hits.get( 0 ).document(), is( document ) );
		assertThat( hits.size(), is( 1 ) );
	}

	@Test
	void testFuzzySearch() throws Exception {
		Document document0 = new Document( URI.create( "" ), "The Cat", new StringReader( "The cat and the fiddle" ) );
		Document document1 = new Document( URI.create( "" ), "The Dog", new StringReader( "The dog ran away with the spoon" ) );
		Document document2 = new Document( URI.create( "" ), "The Cow", new StringReader( "The cow jumped over the moon" ) );

		indexer.start();
		Result<Set<Future<Result<Set<Hit>>>>> result = indexer.submit( document0, document1, document2 );
		indexer.stop();
		for( Future<?> f : result.get() ) {
			f.get();
		}

		List<Hit> hits = indexer
			.getIndex( Index.DEFAULT )
			.map( i -> new FuzzySearch().search( i, IndexQuery.builder().text( "moon" ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + Index.DEFAULT ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found" ) );

		assertThat( hits.get( 0 ).document(), is( document2 ) );
		assertThat( hits.get( 1 ).document(), is( document1 ) );
		assertThat( hits.size(), is( 2 ) );

		hits = indexer
			.getIndex( Index.DEFAULT )
			.map( i -> new FuzzySearch().search( i, IndexQuery.builder().text( "soon" ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + Index.DEFAULT ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found" ) );

		assertThat( hits.get( 0 ).document(), is( document1 ) );
		assertThat( hits.get( 1 ).document(), is( document2 ) );
		assertThat( hits.size(), is( 2 ) );
	}
}
