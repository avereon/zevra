package com.avereon.index;

import com.avereon.result.Result;
import com.avereon.util.FileUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexerTest {

	private Indexer indexer;

	private FuzzySearch search;

	@BeforeEach
	void setup() throws IOException {
		Path indexPath = FileUtil.createTempFolder( "IndexerTest" );
		indexer = new Indexer( indexPath );
		search = new FuzzySearch( 75 );
	}

	@Test
	void testIsRunning() {
		assertThat( indexer.isRunning() ).isFalse();
		indexer.start();
		assertThat( indexer.isRunning() ).isTrue();
		indexer.stop();
		assertThat( indexer.isRunning() ).isFalse();
	}

	@Test
	void testSubmit() {
		Document document = new Document( URI.create( "" ), "", "", "" );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();

		assertThat( result.isSuccessful() ).isTrue();
	}

	@Test
	void testParse() throws Exception {
		String icon = "document";
		String title = "Document";
		String text = "This is some arbitrary content";
		Document document = new Document( URI.create( "" ), icon, title, text );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		// Check the hits
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "document" ) ).contains( new Hit()
			.setContext( title )
			.coordinates( List.of( 0, 0 ) )
			.setLine( 0 )
			.setIndex( 0 )
			.setWord( "document" )
			.setLength( 8 )
			.setDocument( document )
			.setPriority( Hit.TITLE_PRIORITY ) );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "this" ) ).contains( new Hit()
			.setContext( text )
			.coordinates( List.of( 0, 0 ) )
			.setLine( 0 )
			.setIndex( 0 )
			.setWord( "this" )
			.setLength( 4 )
			.setDocument( document )
			.setPriority( Hit.CONTENT_PRIORITY ) );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "is" ) ).contains( new Hit()
			.setContext( text )
			.coordinates( List.of( 0, 5 ) )
			.setLine( 0 )
			.setIndex( 5 )
			.setWord( "is" )
			.setLength( 2 )
			.setDocument( document )
			.setPriority( Hit.CONTENT_PRIORITY ) );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "some" ) ).contains( new Hit()
			.setContext( text )
			.coordinates( List.of( 0, 8 ) )
			.setLine( 0 )
			.setIndex( 8 )
			.setWord( "some" )
			.setLength( 4 )
			.setDocument( document )
			.setPriority( Hit.CONTENT_PRIORITY ) );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "arbitrary" ) ).contains( new Hit()
			.setContext( text )
			.coordinates( List.of( 0, 13 ) )
			.setLine( 0 )
			.setIndex( 13 )
			.setWord( "arbitrary" )
			.setLength( 9 )
			.setDocument( document )
			.setPriority( Hit.CONTENT_PRIORITY ) );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "content" ) ).contains( new Hit()
			.setContext( text )
			.coordinates( List.of( 0, 23 ) )
			.setLine( 0 )
			.setIndex( 23 )
			.setWord( "content" )
			.setLength( 7 )
			.setDocument( document )
			.setPriority( Hit.CONTENT_PRIORITY ) );

		// Check the dictionary
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getDictionary() ).contains( "document", "this", "is", "some", "arbitrary", "content" );
	}

	@Test
	void testParseWithMultipleLinesPunctuationAndExtraWhitespace() throws Exception {
		String icon = "document";
		String name = "This,  A  Document! ";
		String line0 = " This, is";
		String line1 = "some \"arbitrary content\". ";
		String text = line0 + "\n" + line1;
		Document document = new Document( URI.create( "" ), icon, name, text );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		// Check the hits
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "document" ) ).contains( new Hit()
			.setContext( name.trim() )
			.coordinates( List.of( 0, 10 ) )
			.setLine( 0 )
			.setIndex( 10 )
			.setWord( "document" )
			.setLength( 8 )
			.setDocument( document )
			.setPriority( Hit.TITLE_PRIORITY ) );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "this" ) ).contains(
			new Hit().setContext( name.trim() ).coordinates( List.of( 0, 0 ) ).setLine( 0 ).setIndex( 0 ).setWord( "this" ).setLength( 4 ).setDocument( document ).setPriority( Hit.TITLE_PRIORITY ),
			new Hit().setContext( line0.trim() ).coordinates( List.of( 0, 0 ) ).setLine( 0 ).setIndex( 0 ).setWord( "this" ).setLength( 4 ).setDocument( document ).setPriority( Hit.CONTENT_PRIORITY )
		);
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "is" ) ).contains( new Hit()
			.setContext( line0.trim() )
			.coordinates( List.of( 0, 6 ) )
			.setLine( 0 )
			.setIndex( 6 )
			.setWord( "is" )
			.setLength( 2 )
			.setDocument( document )
			.setPriority( Hit.CONTENT_PRIORITY ) );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "some" ) ).contains( new Hit()
			.setContext( line1.trim() )
			.coordinates( List.of( 1, 0 ) )
			.setLine( 1 )
			.setIndex( 0 )
			.setWord( "some" )
			.setLength( 4 )
			.setDocument( document )
			.setPriority( Hit.CONTENT_PRIORITY ) );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "arbitrary" ) ).contains( new Hit()
			.setContext( line1.trim() )
			.coordinates( List.of( 1, 6 ) )
			.setLine( 1 )
			.setIndex( 6 )
			.setWord( "arbitrary" )
			.setLength( 9 )
			.setDocument( document )
			.setPriority( Hit.CONTENT_PRIORITY ) );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "content" ) ).contains( new Hit()
			.setContext( line1.trim() )
			.coordinates( List.of( 1, 16 ) )
			.setLine( 1 )
			.setIndex( 16 )
			.setWord( "content" )
			.setLength( 7 )
			.setDocument( document )
			.setPriority( Hit.CONTENT_PRIORITY ) );

		// Check the dictionary
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getDictionary() ).contains( "a", "document", "this", "is", "some", "arbitrary", "content" );
	}

	@Test
	void testSearch() throws Exception {
		String text = "This is some \"arbitrary content\".";
		Document document = new Document( URI.create( "" ), "", "", text );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();

		result.get().get();

		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getDictionary() ).contains( "this", "is", "some", "arbitrary", "content" );

		String scope = Index.DEFAULT;
		String word = "content";

		List<Hit> hits = indexer
			.getIndex( scope )
			.map( i -> search.search( i, IndexQuery.builder().term( word ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + scope ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found for: " + word ) );

		assertThat( hits.get( 0 ).getDocument() ).isEqualTo( document );
		assertThat( hits.size() ).isEqualTo( 1 );
	}

	@Test
	void testSearchWithTags() throws Exception {
		URI uri = URI.create( "" );
		String icon = "";
		String title = "";
		String content = "";
		Document document = new Document( uri, icon, title, content );
		document.tags( Set.of( "help", "empty" ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();

		result.get().get();

		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getDictionary() ).contains( "help", "empty" );

		String scope = Index.DEFAULT;
		String word = "help";

		List<Hit> hits = indexer
			.getIndex( scope )
			.map( i -> search.search( i, IndexQuery.builder().term( word ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + scope ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found for: " + word ) );

		assertThat( hits.get( 0 ).getDocument() ).isEqualTo( document );
		assertThat( hits.size() ).isEqualTo( 1 );
	}

	@Test
	void testFuzzySearch() throws Exception {
		Document document0 = new Document( URI.create( "" ), "poem", "The Cat", "The cat and the fiddle" );
		Document document1 = new Document( URI.create( "" ), "poem", "The Dog", "The dog ran away with the spoon" );
		Document document2 = new Document( URI.create( "" ), "poem", "The Cow", "The cow jumped over the moon" );

		indexer.start();
		Result<Set<Future<Result<Set<Hit>>>>> result = indexer.submit( document0, document1, document2 );
		indexer.stop();
		for( Future<?> f : result.get() ) {
			f.get();
		}

		List<Hit> hits = indexer
			.getIndex( Index.DEFAULT )
			.map( i -> search.search( i, IndexQuery.builder().term( "moon" ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + Index.DEFAULT ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found" ) );

		assertThat( hits.size() ).isEqualTo( 1 );
		assertThat( hits.get( 0 ).getDocument() ).isEqualTo( document2 );
		//assertThat( hits.get( 1 ).getDocument() ).isEqualTo( document1 );

		hits = indexer
			.getIndex( Index.DEFAULT )
			.map( i -> search.search( i, IndexQuery.builder().term( "soon" ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + Index.DEFAULT ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found" ) );

		assertThat( hits.size() ).isEqualTo( 2 );
		assertThat( hits.get( 0 ).getDocument() ).isEqualTo( document1 );
		assertThat( hits.get( 1 ).getDocument() ).isEqualTo( document2 );
	}

	@Test
	void testPrioritySort() throws Exception {
		String icon = "document";
		String name = "Document";
		String text = "This is an arbitrary document";
		Document document = new Document( URI.create( "" ), icon, name, text );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		List<Hit> hits = indexer
			.getIndex( Index.DEFAULT )
			.map( i -> search.search( i, IndexQuery.builder().term( "document" ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + Index.DEFAULT ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found" ) );

		assertThat( hits.get( 0 ).getPriority() ).isEqualTo( Hit.TITLE_PRIORITY );
		assertThat( hits.get( 1 ).getPriority() ).isEqualTo( Hit.CONTENT_PRIORITY );
		assertThat( hits.size() ).isEqualTo( 2 );
	}

	@Test
	void testSearchWithDuplicateSearchTerms() throws Exception {
		String icon = "document";
		String nameA = "About";
		String textA = "This is an about document";
		String nameB = "Another";
		String textB = "This is another document";

		indexer.start();
		Result<Future<Result<Set<Hit>>>> resultA = indexer.submit( "a", new Document( URI.create( "" ), icon, nameA, textA ) );
		Result<Future<Result<Set<Hit>>>> resultB = indexer.submit( "b", new Document( URI.create( "" ), icon, nameB, textB ) );
		indexer.stop();
		resultA.get().get();
		resultB.get().get();

		Index indexA = indexer.getIndex( "a" ).orElseGet( StandardIndex::new );
		Index indexB = indexer.getIndex( "b" ).orElseGet( StandardIndex::new );

		assertThat( indexA.getDictionary().size() ).isEqualTo( 5 );
		assertThat( indexB.getDictionary().size() ).isEqualTo( 4 );

		Search search = new FuzzySearch( 80 );
		IndexQuery query = IndexQuery.builder().terms( List.of( "document", "about" ) ).build();
		List<Hit> hits = Indexer.search( search, query, indexer.allIndexes() ).get();

		// FIXME This will fail because the results come out of order on occasion
		//		assertThat( hits.get( 0 ).getDocument() ).isEqualTo( new Document( URI.create( "" ), icon, nameA, new StringReader( textA ) ) );
		//		assertThat( hits.get( 0 ).getPriority() ).isEqualTo( Hit.CONTENT_PRIORITY );
		//		assertThat( hits.get( 1 ).getDocument() ).isEqualTo( new Document( URI.create( "" ), icon, nameB, new StringReader( textB ) ) );
		//		assertThat( hits.get( 1 ).getPriority() ).isEqualTo( Hit.CONTENT_PRIORITY );

		// There should only be one hit that matched on both terms
		assertThat( hits.size() ).isEqualTo( 1 );
	}

}
