package com.avereon.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NodeComparatorTest {

	@Test
	void testCompareTo() {
		NamedNode a = new NamedNode();
		NamedNode b = new NamedNode();
		NodeComparator<MockNode> comparator = new NodeComparator<>( "name" );

		a.setName( "a" );
		b.setName( "b" );
		assertThat( comparator.compare( a, b ) ).isEqualTo( -1 );
		assertThat( comparator.compare( b, a ) ).isEqualTo( 1 );
		a.setName( "c" );
		b.setName( "c" );
		assertThat( comparator.compare( a, b ) ).isEqualTo( 0 );
		assertThat( comparator.compare( b, a ) ).isEqualTo( 0 );
	}

	@Test
	void testCompareToWithMissingValues() {
		NamedNode a = new NamedNode();
		NamedNode b = new NamedNode();
		NodeComparator<MockNode> comparator = new NodeComparator<>( "name" );

		assertThat( comparator.compare( a, b ) ).isEqualTo( 0 );
	}

	@Test
	void testCompareToWithNullValues() {
		NamedNode a = new NamedNode();
		NamedNode b = new NamedNode();
		NodeComparator<MockNode> comparator = new NodeComparator<>( "name" );

		a.setName( "a" );
		b.setName( null );
		assertThat( comparator.compare( a, b ) ).isEqualTo( -1 );

		a.setName( null );
		b.setName( "b" );
		assertThat( comparator.compare( a, b ) ).isEqualTo( 1 );
	}

}
