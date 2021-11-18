package com.avereon.data;

import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiNodeSettingsTest {

	private MockNode node1;

	private MockNode node2;

	private MockNode node3;

	private MultiNodeSettings settings;

	@BeforeEach
	public void setup() {
		node1 = new MockNode( "a" );
		node2 = new MockNode( "b" );
		node3 = new MockNode( "c" );

		node1.setValue( "color", "blue" );
		node2.setValue( "color", "blue" );
		node3.setValue( "color", "blue" );

		node1.setValue( "size", "S" );
		node2.setValue( "size", "M" );
		node3.setValue( "size", "L" );

		node2.setValue( "count", 42 );

		settings = new MultiNodeSettings( node1, node2, node3 );
	}

	@Test
	void testCreateWithCollection() {
		MultiNodeSettings settings = new MultiNodeSettings( Set.of( node1, node2, node3 ) );

		// These are MOCK_ID, "color" and "size"
		assertThat( settings.getKeys().size() ).isEqualTo( 3 );
	}

	@Test
	void testGetName() {
		assertThat( settings.getName() ).isNull();
	}

	@Test
	void testGetPath() {
		assertThat( settings.getPath() ).isNull();
	}

	@Test
	void testGetKeys() {
		assertThat( settings.getKeys() ).contains( MockNode.MOCK_ID, "color", "size" );
	}

	@Test
	void testExists() {
		assertTrue( settings.exists( "color" ) );
		assertTrue( settings.exists( "size" ) );
		assertFalse( settings.exists( "count" ) );
	}

	@Test
	void testGet() {
		assertThat( settings.get( "color" ) ).isEqualTo( "blue" );
		assertThat( settings.get( "size", "default" ) ).isEqualTo( "default" );
		assertThat( settings.get( "size" ) ).isNull();
		assertThat( settings.get( "count" ) ).isNull();
	}

	@Test
	void testSet() {
		assertThat( node1.<Integer> getValue( "count" ) ).isNull();
		assertThat( node2.<Integer> getValue( "count" ) ).isEqualTo( 42 );
		assertThat( node3.<Integer> getValue( "count" ) ).isNull();
		settings.set( "count", 37 );
		assertThat( node1.<Integer> getValue( "count" ) ).isEqualTo( 37 );
		assertThat( node2.<Integer> getValue( "count" ) ).isEqualTo( 37 );
		assertThat( node3.<Integer> getValue( "count" ) ).isEqualTo( 37 );
	}

	@Test
	void testSetWithNestedTxn() throws Exception {
		assertThat( node1.<Integer> getValue( "count" ) ).isNull();
		assertThat( node2.<Integer> getValue( "count" ) ).isEqualTo( 42 );
		assertThat( node3.<Integer> getValue( "count" ) ).isNull();
		try( Txn ignored = Txn.create( true ) ) {
			settings.set( "temp", "temp-value" );
			settings.set( "count", 37 );
			Txn.submit( new TxnOperation( e -> { } ) {

				@Override
				protected TxnOperation commit() {
					return this;
				}

				@Override
				protected TxnOperation revert() {
					return this;
				}
			} );
		}
		assertThat( node1.<Integer> getValue( "count" ) ).isEqualTo( 37 );
		assertThat( node2.<Integer> getValue( "count" ) ).isEqualTo( 37 );
		assertThat( node3.<Integer> getValue( "count" ) ).isEqualTo( 37 );
	}

	@Test
	void testRemove() {
		assertTrue( node1.exists( "size" ) );
		assertTrue( node2.exists( "size" ) );
		assertTrue( node3.exists( "size" ) );
		settings.remove( "size" );
		assertFalse( node1.exists( "size" ) );
		assertFalse( node2.exists( "size" ) );
		assertFalse( node3.exists( "size" ) );
	}

}
