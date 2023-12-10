package com.avereon.index;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class IndexTest {

	@Test
	void testMerge() {
		Index a = new StandardIndex();
		Index b = new StandardIndex();

		a.push( Set.of( new Hit().word( "a" ) ) );
		a.push( Set.of( new Hit().word( "b" ) ) );

		b.push( Set.of( new Hit().word( "b" ) ) );
		b.push( Set.of( new Hit().word( "c" ) ) );

		// When merged, the word "b" will still only have one hit since the hits are equal

		Index merged = Index.merge( a, b );
		assertThat( merged.getDictionary() ).contains( "a", "b", "c" );
		assertThat( merged.getHits( "a" ).size() ).isEqualTo( 1 );
		assertThat( merged.getHits( "b" ).size() ).isEqualTo( 1 );
		assertThat( merged.getHits( "c" ).size() ).isEqualTo( 1 );
	}

	@Test
	void testMergeWithMultipleHits() {
		Index a = new StandardIndex();
		Index b = new StandardIndex();

		a.push( Set.of( new Hit().word( "a" ) ) );
		a.push( Set.of( new Hit().word( "b" ).coordinates( List.of( 0 ) ).line( 0 ) ) );

		b.push( Set.of( new Hit().word( "b" ).coordinates( List.of( 1 ) ).line( 1 ) ) );
		b.push( Set.of( new Hit().word( "c" ) ) );

		// When merged, the word "b" will have two hits since they are on separate lines

		Index merged = Index.merge( a, b );
		assertThat( merged.getDictionary() ).contains( "a", "b", "c" );
		assertThat( merged.getHits( "a" ).size() ).isEqualTo( 1 );
		assertThat( merged.getHits( "b" ).size() ).isEqualTo( 2 );
		assertThat( merged.getHits( "c" ).size() ).isEqualTo( 1 );
	}

}
