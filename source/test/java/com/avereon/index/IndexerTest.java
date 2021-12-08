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

import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat( indexer.isRunning() ).isFalse();
		indexer.start();
		assertThat( indexer.isRunning() ).isTrue();
		indexer.stop();
		assertThat( indexer.isRunning() ).isFalse();
	}

	@Test
	void testSubmit() {
		Document document = new Document( URI.create( "" ), "", "", new StringReader( "" ) );

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
		Document document = new Document( URI.create( "" ), icon, title, new StringReader( text ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		// Check the hits
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "document" ) ).contains( Hit
			.builder()
			.context( title )
			.line( 0 )
			.index( 0 )
			.word( "document" )
			.length( 8 )
			.document( document )
			.priority( Hit.TITLE_PRIORITY )
			.build() );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "this" ) ).contains( Hit
			.builder()
			.context( text )
			.line( 0 )
			.index( 0 )
			.word( "this" )
			.length( 4 )
			.document( document )
			.priority( Hit.CONTENT_PRIORITY )
			.build() );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "is" ) ).contains( Hit
			.builder()
			.context( text )
			.line( 0 )
			.index( 5 )
			.word( "is" )
			.length( 2 )
			.document( document )
			.priority( Hit.CONTENT_PRIORITY )
			.build() );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "some" ) ).contains( Hit
			.builder()
			.context( text )
			.line( 0 )
			.index( 8 )
			.word( "some" )
			.length( 4 )
			.document( document )
			.priority( Hit.CONTENT_PRIORITY )
			.build() );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "arbitrary" ) ).contains( Hit
			.builder()
			.context( text )
			.line( 0 )
			.index( 13 )
			.word( "arbitrary" )
			.length( 9 )
			.document( document )
			.priority( Hit.CONTENT_PRIORITY )
			.build() );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "content" ) ).contains( Hit
			.builder()
			.context( text )
			.line( 0 )
			.index( 23 )
			.word( "content" )
			.length( 7 )
			.document( document )
			.priority( Hit.CONTENT_PRIORITY )
			.build() );

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
		Document document = new Document( URI.create( "" ), icon, name, new StringReader( text ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		// Check the hits
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "document" ) ).contains( Hit
			.builder()
			.context( name.trim() )
			.line( 0 )
			.index( 10 )
			.word( "document" )
			.length( 8 )
			.document( document )
			.priority( Hit.TITLE_PRIORITY )
			.build() );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "this" ) ).contains(
			Hit.builder().context( name.trim() ).line( 0 ).index( 0 ).word( "this" ).length( 4 ).document( document ).priority( Hit.TITLE_PRIORITY ).build(),
			Hit.builder().context( line0.trim() ).line( 0 ).index( 0 ).word( "this" ).length( 4 ).document( document ).priority( Hit.CONTENT_PRIORITY ).build()
		);
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "is" ) ).contains( Hit
			.builder()
			.context( line0.trim() )
			.line( 0 )
			.index( 6 )
			.word( "is" )
			.length( 2 )
			.document( document )
			.priority( Hit.CONTENT_PRIORITY )
			.build() );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "some" ) ).contains( Hit
			.builder()
			.context( line1.trim() )
			.line( 1 )
			.index( 0 )
			.word( "some" )
			.length( 4 )
			.document( document )
			.priority( Hit.CONTENT_PRIORITY )
			.build() );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "arbitrary" ) ).contains( Hit
			.builder()
			.context( line1.trim() )
			.line( 1 )
			.index( 6 )
			.word( "arbitrary" )
			.length( 9 )
			.document( document )
			.priority( Hit.CONTENT_PRIORITY )
			.build() );
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getHits( "content" ) ).contains( Hit
			.builder()
			.context( line1.trim() )
			.line( 1 )
			.index( 16 )
			.word( "content" )
			.length( 7 )
			.document( document )
			.priority( Hit.CONTENT_PRIORITY )
			.build() );

		// Check the dictionary
		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getDictionary() ).contains( "a", "document", "this", "is", "some", "arbitrary", "content" );
	}

	@Test
	void testSearch() throws Exception {
		String text = "This is some \"arbitrary content\".";
		Document document = new Document( URI.create( "" ), "", "", new StringReader( text ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();

		result.get().get();

		assertThat( indexer.getIndex( Index.DEFAULT ).orElseThrow().getDictionary() ).contains( "this", "is", "some", "arbitrary", "content" );

		String scope = Index.DEFAULT;
		String word = "content";

		List<Hit> hits = indexer
			.getIndex( scope )
			.map( i -> new FuzzySearch().search( i, IndexQuery.builder().term( word ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + scope ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found for: " + word ) );

		assertThat( hits.get( 0 ).document() ).isEqualTo( document );
		assertThat( hits.size() ).isEqualTo( 1 );
	}

	@Test
	void testSearchWithTags() throws Exception {
		Document document = new Document( URI.create( "" ), "", "", new StringReader( "" ) );
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
			.map( i -> new FuzzySearch().search( i, IndexQuery.builder().term( word ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + scope ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found for: " + word ) );

		assertThat( hits.get( 0 ).document() ).isEqualTo( document );
		assertThat( hits.size() ).isEqualTo( 1 );
	}

	@Test
	void testFuzzySearch() throws Exception {
		Document document0 = new Document( URI.create( "" ), "poem", "The Cat", new StringReader( "The cat and the fiddle" ) );
		Document document1 = new Document( URI.create( "" ), "poem", "The Dog", new StringReader( "The dog ran away with the spoon" ) );
		Document document2 = new Document( URI.create( "" ), "poem", "The Cow", new StringReader( "The cow jumped over the moon" ) );

		indexer.start();
		Result<Set<Future<Result<Set<Hit>>>>> result = indexer.submit( document0, document1, document2 );
		indexer.stop();
		for( Future<?> f : result.get() ) {
			f.get();
		}

		List<Hit> hits = indexer
			.getIndex( Index.DEFAULT )
			.map( i -> new FuzzySearch().search( i, IndexQuery.builder().term( "moon" ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + Index.DEFAULT ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found" ) );

		assertThat( hits.get( 0 ).document() ).isEqualTo( document2 );
		assertThat( hits.get( 1 ).document() ).isEqualTo( document1 );
		assertThat( hits.size() ).isEqualTo( 2 );

		hits = indexer
			.getIndex( Index.DEFAULT )
			.map( i -> new FuzzySearch().search( i, IndexQuery.builder().term( "soon" ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + Index.DEFAULT ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found" ) );

		assertThat( hits.get( 0 ).document() ).isEqualTo( document1 );
		assertThat( hits.get( 1 ).document() ).isEqualTo( document2 );
		assertThat( hits.size() ).isEqualTo( 2 );
	}

	@Test
	void testPrioritySort() throws Exception {
		String icon = "document";
		String name = "Document";
		String text = "This is an arbitrary document";
		Document document = new Document( URI.create( "" ), icon, name, new StringReader( text ) );

		indexer.start();
		Result<Future<Result<Set<Hit>>>> result = indexer.submit( document );
		indexer.stop();
		result.get().get();

		List<Hit> hits = indexer
			.getIndex( Index.DEFAULT )
			.map( i -> new FuzzySearch().search( i, IndexQuery.builder().term( "document" ).build() ) )
			.orElseThrow( () -> new NoSuchElementException( "Index not found: " + Index.DEFAULT ) )
			.orElseThrow( () -> new NoSuchElementException( "No documents found" ) );

		assertThat( hits.get( 0 ).priority() ).isEqualTo( Hit.TITLE_PRIORITY );
		assertThat( hits.get( 1 ).priority() ).isEqualTo( Hit.CONTENT_PRIORITY );
		assertThat( hits.size() ).isEqualTo( 2 );
	}

	@Test
	void testSearchWithMultipleIndexes() throws Exception {
		String icon = "document";
		String name = "Document";
		String text = "This is an arbitrary document";

		indexer.start();
		Result<Future<Result<Set<Hit>>>> resultA = indexer.submit( "a", new Document( URI.create( "" ), icon, name, new StringReader( text ) ) );
		Result<Future<Result<Set<Hit>>>> resultB = indexer.submit( "b", new Document( URI.create( "" ), icon, name, new StringReader( text ) ) );
		indexer.stop();
		resultA.get().get();
		resultB.get().get();

		Index indexA = indexer.getIndex( "a" ).orElseGet( StandardIndex::new );
		Index indexB = indexer.getIndex( "b" ).orElseGet( StandardIndex::new );

		assertThat( indexA.getDictionary().size() ).isEqualTo( 5 );
		assertThat( indexB.getDictionary().size() ).isEqualTo( 5 );
		assertThat( indexA ).isEqualTo( indexB );
		assertThat( indexB ).isEqualTo( indexA );

		Search search = new FuzzySearch( 80 );
		IndexQuery query = IndexQuery.builder().terms( Set.of( "document" ) ).build();
		List<Hit> hits = Indexer.search( search, query, indexer.getIndexes().values() ).get();

		assertThat( hits.get( 0 ).document() ).isEqualTo( new Document( URI.create( "" ), icon, name, new StringReader( text ) ) );
		assertThat( hits.get( 0 ).priority() ).isEqualTo( Hit.TITLE_PRIORITY );
		assertThat( hits.get( 1 ).document() ).isEqualTo( new Document( URI.create( "" ), icon, name, new StringReader( text ) ) );
		assertThat( hits.get( 1 ).priority() ).isEqualTo( Hit.CONTENT_PRIORITY );
		assertThat( hits.size() ).isEqualTo( 2 );
	}

}
