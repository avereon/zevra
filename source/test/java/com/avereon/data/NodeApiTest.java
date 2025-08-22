package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnEvent;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class NodeApiTest extends BaseNodeTest {

	@Test
	void testAsMap() {
		Object o = new Object();
		data.setValue( "0", 0 );
		data.setValue( "a", "A" );
		data.setValue( "n", "not included" );
		data.setValue( "o", o );

		// Intentionally don't include 'n' to make sure it is not included
		assertThat( data.asMap( "0", "a", "o" ) ).isEqualTo( Map.of( "0", 0, "a", "A", "o", o ) );

		// Intentionally try to include 'b' to make sure null values are not included
		assertThat( data.asMap( "0", "a", "b", "o" ) ).isEqualTo( Map.of( "0", 0, "a", "A", "o", o ) );
	}

	@Test
	void testValueChangeEventsNotBubbledToParentValueListeners() {
		MockNode a = new MockNode( "a" );
		MockNode b = new MockNode( "b" );
		a.setValue( "child", b );

		AtomicInteger count = new AtomicInteger();
		a.register( "key", e -> count.incrementAndGet() );
		b.register( "key", e -> count.incrementAndGet() );

		b.setValue( "key", "b" );
		assertThat( count.get() ).isEqualTo( 1 );
	}

	@Test
	void testMultipleEventHandlers() {
		AtomicInteger counterA = new AtomicInteger();
		AtomicInteger counterB = new AtomicInteger();

		MockNode node = new MockNode( "node" );
		node.register( "key", e -> counterA.incrementAndGet() );
		node.register( "key", e -> counterB.incrementAndGet() );

		assertThat( counterA.get() ).isEqualTo( 0 );
		assertThat( counterB.get() ).isEqualTo( 0 );

		// when
		node.setValue( "key", "value" );

		// then
		assertThat( counterA.get() ).isEqualTo( 1 );
		assertThat( counterB.get() ).isEqualTo( 1 );
	}

	@Test
	void testEventHandlerWithOwner() {
		// given
		Object owner = new Object();
		MockNode node = new MockNode( "node" );
		AtomicInteger counter = new AtomicInteger();

		node.register(owner, "key", e -> counter.incrementAndGet() );
		assertThat( counter.get() ).isEqualTo( 0 );

		node.setValue( "key", "value1" );
		assertThat( counter.get() ).isEqualTo( 1 );

		owner = null;
		assertThat(owner).isNull();

		// when
		System.gc();

		// then
		node.setValue( "key", "value2" );
		assertThat( counter.get() ).isEqualTo( 1 );
	}

	@Test
	void testDistanceTo() {
		MockNode grandParent = new MockNode( "grandParent" );
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );

		assertThat( child.distanceTo( grandParent ) ).isEqualTo( -1 );
		assertThat( child.distanceTo( parent ) ).isEqualTo( -1 );

		grandParent.setValue( "child", parent );
		parent.setValue( "child", child );

		assertThat( child.distanceTo( grandParent ) ).isEqualTo( 2 );
		assertThat( child.distanceTo( parent ) ).isEqualTo( 1 );
		assertThat( child.distanceTo( child ) ).isEqualTo( 0 );
	}

	@Test
	void testNewNodeModifiedState() {
		assertThat( data.isModified() ).isEqualTo( false );
	}

	@Test
	void testModifyByFlagAndUnmodifyByFlag() {
		assertThat( data.isModifiedByValue() ).isEqualTo( false );
		assertThat( data.isModifiedByChild() ).isEqualTo( false );
		assertThat( data.isModifiedBySelf() ).isEqualTo( false );
		assertThat( data.isModified() ).isEqualTo( false );
		assertThat( data.getEventCount() ).isEqualTo( 0 );

		data.setModified( true );
		Thread.yield();
		assertThat( data.isModifiedByValue() ).isEqualTo( false );
		assertThat( data.isModifiedByChild() ).isEqualTo( false );
		assertThat( data.isModifiedBySelf() ).isEqualTo( true );
		assertThat( data.isModified() ).isEqualTo( true );

		int index = 0;
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setModified( false );
		assertThat( data.isModifiedByValue() ).isEqualTo( false );
		assertThat( data.isModifiedByChild() ).isEqualTo( false );
		assertThat( data.isModifiedBySelf() ).isEqualTo( false );
		assertThat( data.isModified() ).isEqualTo( false );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testModifyByValueAndUnmodifyByFlag() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "key", 423984 );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", null, 423984 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testModifyByValueAndUnmodifyByValue() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "key", 423984 );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", null, 423984 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "key", null );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", 423984, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testGetValueKeys() {
		data.setValue( "key", "value" );
		assertThat( data.getValueKeys() ).contains( "key" );
	}

	@Test
	void testGetAndSetValue() {
		String key = "key";
		Object value = "value";
		assertThat( data.<Object> getValue( key ) ).isNull();

		data.setValue( key, value );
		assertThat( data.<Object> getValue( key ) ).isEqualTo( value );

		data.setValue( key, null );
		assertThat( data.<Object> getValue( key ) ).isNull();
	}

	@Test
	void testObjectValue() {
		String key = "key";
		Object value = new Object();
		assertThat( data.<Object> getValue( key ) ).isNull();

		data.setValue( key, value );
		assertThat( data.<Object> getValue( key ) ).isEqualTo( value );
	}

	@Test
	void testStringValue() {
		String key = "key";
		String value = "value";
		assertThat( data.<String> getValue( key ) ).isNull();

		data.setValue( key, value );
		assertThat( data.<String> getValue( key ) ).isEqualTo( value );
	}

	@Test
	void testBooleanValue() {
		String key = "key";
		assertThat( data.<Boolean> getValue( key ) ).isNull();
		data.setValue( key, true );
		assertThat( data.<Boolean> getValue( key ) ).isEqualTo( true );
	}

	@Test
	void testIntegerValue() {
		String key = "key";
		int value = 0;
		assertThat( data.<Integer> getValue( key ) ).isNull();

		data.setValue( key, value );
		assertThat( data.<Integer> getValue( key ) ).isEqualTo( value );
	}

	@SuppressWarnings( "StringOperationCanBeSimplified" )
	@Test
	void testPrimitiveDuplicate() {
		String key = "key";
		String value = "value";

		// It is important that these are the same value but not the same object
		String A = new String( value );
		String B = new String( value );

		// This checks that the two objects are not the same object
		assertThat( A ).isNotSameAs( B );

		// This checks that the two objects are the same value
		assertThat( A ).isEqualTo( B );

		assertThat( data.<Object> getValue( key ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.setValue( key, A );
		assertThat( data.<Object> getValue( key ) ).isEqualTo( A );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );

		data.setModified( false );
		assertThat( data.<Object> getValue( key ) ).isEqualTo( A );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.setValue( key, B );
		assertThat( data.<Object> getValue( key ) ).isEqualTo( B );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
	}

	@Test
	void testSetNullValueToNull() {
		int index = 0;
		String key = "key";
		assertThat( data.<Object> getValue( key ) ).isNull();
		data.setValue( key, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testSetValueWithNullName() {
		try {
			data.setValue( null, "value" );
			fail( "Null value keys are not allowed" );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage() ).isEqualTo( "Value key cannot be null" );
		}
		assertThat( data.getEventCount() ).isEqualTo( 0 );
	}

	@Test
	void testGetValueWithNullKey() {
		try {
			data.getValue( null );
			fail( "Null value keys are not allowed." );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage() ).isEqualTo( "Value key cannot be null" );
		}
	}

	@Test
	void testNullValues() {
		int index = 0;

		// Assert initial values
		assertThat( data.<Object> getValue( "key" ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Set the value to null and make sure nothing happens
		data.setValue( "key", null );
		assertThat( data.<Object> getValue( "key" ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Set the value
		data.setValue( "key", "value" );
		assertThat( data.<Object> getValue( "key" ) ).isEqualTo( "value" );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", null, "value" );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Set the value to the same value and make sure nothing happens
		data.setValue( "key", "value" );
		assertThat( data.<Object> getValue( "key" ) ).isEqualTo( "value" );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Set the value back to null
		data.setValue( "key", null );
		assertThat( data.<Object> getValue( "key" ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", "value", null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Set the value to null again and make sure nothing happens
		data.setValue( "key", null );
		assertThat( data.<Object> getValue( "key" ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testGetWithNullSupplier() {
		assertThat( data.<Object> getValue( "x" ) ).isNull();
	}

	@Test
	void testGetValueWithDefault() {
		assertThat( data.<Object> getValue( "key" ) ).isNull();
		assertThat( data.getValue( "key", "default" ) ).isEqualTo( "default" );
	}

	@Test
	void testModifiedBySetAttribute() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		NodeAssert.assertThat( data ).hasStates( true, false, 3, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "x", null, 1 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "y", null, 2 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "z", null, 3 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testUnmodifiedByUnsetValue() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "x", null, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "y", null, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "z", null, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		NodeAssert.assertThat( data ).hasStates( true, false, 3, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "x", 0, 1 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "y", 0, 2 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "z", 0, 3 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "x", 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "y", 2, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "z", 3, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testModifiedAttributeCountResetByClearingModifiedFlag() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		NodeAssert.assertThat( data ).hasStates( true, false, 3, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "x", null, 1 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "y", null, 2 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "z", null, 3 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testSetClearSetValueInTransaction() throws Exception {
		data.setValue( "x", 1 );
		try (Txn t = Txn.create() ) {
			data.setValue( "x", null );
			data.setValue( "x", 1 );
		}
		assertThat( data.<Integer> getValue( "x" ) ).isEqualTo( 1 );
	}

	@Test
	void testRefresh() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.refresh();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testValueKeys() {
		data.setValue( "key", "value" );
		assertThat( data.getValueKeys() ).contains( "key" );
	}

	@Test
	void testIsSet() {
		assertThat( data.isSet( "key" ) ).isFalse();
		data.setValue( "key", "value" );
		assertThat( data.isSet( "key" ) ).isTrue();
		data.setValue( "key", null );
		assertThat( data.isSet( "key" ) ).isFalse();
	}

	@Test
	void testIsNotSet() {
		assertThat( data.isNotSet( "key" ) ).isTrue();
		data.setValue( "key", "value" );
		assertThat( data.isNotSet( "key" ) ).isFalse();
		data.setValue( "key", null );
		assertThat( data.isNotSet( "key" ) ).isTrue();
	}

	@Test
	void testValue() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "name", "mock" );
		assertThat( data.<Object> getValue( "name" ) ).isEqualTo( "mock" );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "name", null, "mock" );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "name", null );
		assertThat( data.<Object> getValue( "name" ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "name", "mock", null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testComputeIfAbsent() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		assertThat( data.<Object> getValue( "name" ) ).isNull();
		assertThat( data.<String> computeIfAbsent( "name", k -> "mock" ) ).isEqualTo( "mock" );
		assertThat( data.<String> getValue( "name" ) ).isEqualTo( "mock" );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "name", null, "mock" );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "name", null );
		assertThat( data.<Object> getValue( "name" ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "name", "mock", null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testValues() {
		assertThat( data.getValues() ).isEqualTo( Set.of() );
		data.setValue( "0", 0 );
		data.setValue( "a", "A" );
		data.setValue( "b", "B" );
		assertThat( data.getValues() ).contains( 0, "A", "B" );
		assertThat( data.isModified() ).isTrue();
	}

	@Test
	void testClearWithNonModifyingValues() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "u", 1 );
		data.setValue( "v", 2 );
		data.setValue( "w", 3 );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		// There should not be a MODIFIED event
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "u", null, 1 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "v", null, 2 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "w", null, 3 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.clear();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		// There should not be an UNMODIFIED event
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "u", 1, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "v", 2, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "w", 3, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testClearWithModifyingValues() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		NodeAssert.assertThat( data ).hasStates( true, false, 3, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "x", null, 1 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "y", null, 2 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "z", null, 3 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.clear();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "x", 1, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "y", 2, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "z", 3, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testValuesOfType() {
		assertThat( data.getValues( String.class ) ).isEqualTo( Set.of() );
		data.setValue( "0", 0 );
		data.setValue( "a", "A" );
		data.setValue( "b", "B" );
		assertThat( data.getValues( Integer.class ) ).contains( 0 );
		assertThat( data.getValues( String.class ) ).contains( "A", "B" );
		assertThat( data.isModified() ).isTrue();
	}

	@Test
	void testNodeSetAddWithNull() {
		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.addItem( null );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
	}

	@Test
	void testNodeSetAddRemove() {
		MockNode item = new MockNode();
		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.addItem( item );
		assertThat( data.getValues( MockNode.ITEMS ) ).contains( item );
		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 );

		data.removeItem( item );
		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.addItem( item );
		assertThat( data.getValues( MockNode.ITEMS ) ).contains( item );
		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.removeItem( item );
		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() );
		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 );
	}

	@Test
	void testNodeSetAddRemoveDataStructure() {
		MockNode item = new MockNode();
		assertThat( item.<Node> getParent() ).isNull();

		data.addItem( item );
		assertThat( item.<Node> getParent() ).isNotNull();
		assertThat( item.<Node> getTrueParent() ).isInstanceOf( NodeSet.class );
		assertThat( item.<Node> getParent() ).isEqualTo( data );

		data.removeItem( item );
		assertThat( item.<Node> getParent() ).isNull();
	}

	@Test
	void testAddMultipleSetItems() {
		MockNode item1 = new MockNode();
		MockNode item2 = new MockNode();
		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.addItem( item1 );
		assertThat( data.getValues( MockNode.ITEMS ) ).contains( item1 );
		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 );
		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.addItem( item2 );
		assertThat( data.getValues( MockNode.ITEMS ) ).contains( item1, item2 );
		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 );
		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
	}

	@Test
	void testRemoveFromMultipleSetItems() {
		MockNode item1 = new MockNode();
		MockNode item2 = new MockNode();
		data.addItem( item1 ).addItem( item2 );
		assertThat( data.getValues( MockNode.ITEMS ) ).contains( item1, item2 );
		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 );
		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.removeItem( item1 );
		assertThat( data.getValues( MockNode.ITEMS ) ).contains( item2 );
		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 );
		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );

		data.removeItem( item2 );
		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() );
		assertThat( data.<Object> getValue( MockNode.ITEMS ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 );
		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
	}

	@Test
	void testNodeSetIterator() {
		MockNode item0 = new MockNode( "0" );
		data.addItem( item0 );
		MockNode item1 = new MockNode( "1" );
		data.addItem( item1 );
		MockNode item2 = new MockNode( "2" );
		data.addItem( item2 );
		MockNode item3 = new MockNode( "3" );
		data.addItem( item3 );

		assertThat( data.getItems() ).contains( item0, item1, item2, item3 );
		assertThat( data.getItems().size() ).isEqualTo( 4 );
	}

	@Test
	void testNodeSetIteratorWithModifyFilter() {
		MockNode item0 = new MockNode( "0" );
		data.addItem( item0 );
		MockNode item1 = new MockNode( "1" );
		data.addItem( item1 );
		MockNode item2 = new MockNode( "2" );
		data.addItem( item2 );
		MockNode item3 = new MockNode( "3" );
		data.addItem( item3 );

		data.setSetModifyFilter( MockNode.ITEMS, n -> true );

		assertThat( data.getItems() ).contains( item0, item1, item2, item3 );
		assertThat( data.getItems().size() ).isEqualTo( 4 );
	}

	@Test
	void testAddDataListener() {
		// Remove the default watcher
		data.unregister( Event.ANY, data.getWatcher() );

		EventHandler<NodeEvent> listener = e -> {};
		data.register( NodeEvent.ANY, listener );

		Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> handlers = data.getEventHandlers();

		assertThat( handlers ).isNotNull();
		assertThat( handlers.size() ).isEqualTo( 1 );
		assertThat( handlers.get( NodeEvent.ANY ).contains( listener ) ).isTrue();
	}

	@Test
	void testRemoveDataListener() {
		// Remove the default watcher
		data.unregister( Event.ANY, data.getWatcher() );

		Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> handlers;
		EventHandler<NodeEvent> listener = e -> {};

		data.register( NodeEvent.ANY, listener );
		handlers = data.getEventHandlers();
		assertThat( handlers ).isNotNull();
		assertThat( handlers.size() ).isEqualTo( 1 );
		assertThat( handlers.get( NodeEvent.ANY ).contains( listener ) ).isTrue();

		data.unregister( NodeEvent.ANY, listener );
		handlers = data.getEventHandlers();
		assertThat( handlers ).isNotNull();
		assertThat( handlers.size() ).isEqualTo( 0 );
		assertThat( handlers.get( NodeEvent.ANY ) ).isNull();

		data.register( NodeEvent.ANY, listener );
		handlers = data.getEventHandlers();
		assertThat( handlers ).isNotNull();
		assertThat( handlers.size() ).isEqualTo( 1 );
		assertThat( handlers.get( NodeEvent.ANY ).contains( listener ) ).isTrue();

		data.unregister( NodeEvent.ANY, listener );
		handlers = data.getEventHandlers();
		assertThat( handlers ).isNotNull();
		assertThat( handlers.size() ).isEqualTo( 0 );
		assertThat( handlers.get( NodeEvent.ANY ) ).isNull();
	}

	@Test
	void testCircularReferenceCheck() {
		MockNode node = new MockNode();
		try {
			node.setValue( "node", node );
			fail( "CircularReferenceException should be thrown" );
		} catch( CircularReferenceException exception ) {
			// Intentionally ignore exception.
			assertThat( exception.getMessage() ).startsWith( "Circular reference detected" );
		}
	}

	@Test
	void testCopyFrom() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1 );
		assertThat( node1.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node1.<String> getValue( "key2" ) ).isEqualTo( "value2" );
		assertThat( node2.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node2.<String> getValue( "key2" ) ).isEqualTo( "valueB" );
	}

	@Test
	void testCopyFromWithOverwrite() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1, true );
		assertThat( node1.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node1.<String> getValue( "key2" ) ).isEqualTo( "value2" );
		assertThat( node2.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node2.<String> getValue( "key2" ) ).isEqualTo( "value2" );
	}

	@Test
	void testCopyFromWithOverwriteAndPrimaryKey() {
		Node node1 = new MockNode();
		node1.setValue( MockNode.MOCK_ID, "a" );
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new MockNode();
		node2.setValue( MockNode.MOCK_ID, "b" );
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1, true );
		assertThat( node1.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node1.<String> getValue( "key2" ) ).isEqualTo( "value2" );
		assertThat( node1.<String> getValue( MockNode.MOCK_ID ) ).isEqualTo( "a" );
		assertThat( node2.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node2.<String> getValue( "key2" ) ).isEqualTo( "value2" );
		assertThat( node2.<String> getValue( MockNode.MOCK_ID ) ).isEqualTo( "b" );
	}

	@Test
	void testCopyFromUsingResources() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1 );
		assertThat( node1.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node1.<String> getValue( "key2" ) ).isEqualTo( "value2" );
		assertThat( node2.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node2.<String> getValue( "key2" ) ).isEqualTo( "valueB" );
	}

	@Test
	void testCopyFromWithOverwriteUsingResources() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1, true );
		assertThat( node1.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node1.<String> getValue( "key2" ) ).isEqualTo( "value2" );
		assertThat( node2.<String> getValue( "key1" ) ).isEqualTo( "value1" );
		assertThat( node2.<String> getValue( "key2" ) ).isEqualTo( "value2" );
	}

	@Test
	void testToString() {
		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data.toString() ).isEqualTo( "MockNode{}" );

		Date birthDate = new Date( 0 );
		data.setValue( "firstName", "Jane" );
		data.setValue( "birthDate", birthDate );
		assertThat( data.toString() ).isEqualTo( "MockNode{birthDate=" + birthDate + ",firstName=Jane}" );

		data.setValue( "lastName", "Doe" );
		assertThat( data.toString() ).isEqualTo( "MockNode{birthDate=" + birthDate + ",firstName=Jane,lastName=Doe}" );
	}

	@Test
	void testToStringWithSomeValues() {
		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data.toString() ).isEqualTo( "MockNode{}" );

		data.setValue( "firstName", "Jane" );
		data.setValue( "lastName", "Doe" );
		assertThat( data.toString( "firstName" ) ).isEqualTo( "MockNode{firstName=Jane}" );
		assertThat( data.toString( "lastName" ) ).isEqualTo( "MockNode{lastName=Doe}" );
	}

	@Test
	void testToStringWithAllValues() {
		assertThat( data.toString( true ) ).isEqualTo( "MockNode{}" );

		data.setValue( "firstName", "Jane" );
		data.setValue( "lastName", "Doe" );
		assertThat( data.toString( true ) ).isEqualTo( "MockNode{firstName=Jane,lastName=Doe}" );
	}

	@Test
	void testReadOnly() {
		data.setValue( "id", "123456789" );
		data.defineReadOnly( "id" );
		assertThat( data.isReadOnly( "id" ) ).isEqualTo( true );
		assertThat( data.<String> getValue( "id" ) ).isEqualTo( "123456789" );

		try {
			data.setValue( "id", "987654321" );
			fail( "Should throw an IllegalStateException" );
		} catch( IllegalStateException exception ) {
			// Intentionally ignore exception
		}
		assertThat( data.<String> getValue( "id" ) ).isEqualTo( "123456789" );
	}

	@Test
	void testHashCode() {
		String key = UUID.randomUUID().toString();
		String lastName = "Doe";

		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data.hashCode() ).isEqualTo( System.identityHashCode( data )  );

		// Test the primary key
		data.setValue( MockNode.MOCK_ID, key );
		assertThat( data.hashCode() ).isEqualTo( key.hashCode()  );

		// Test the natural key
		data.setValue( "lastName", lastName );
		assertThat( data.hashCode() ).isEqualTo( key.hashCode() ^ lastName.hashCode()  );
	}

	@Test
	void testEquals() {
		String key = UUID.randomUUID().toString();
		String lastName = "Doe";

		MockNode data1 = new MockNode();
		data1.defineNaturalKey( "firstName", "lastName", "birthDate" );
		MockNode data2 = new MockNode();
		data2.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data1.equals( data2 ) ).isEqualTo( true  );

		// Test the primary key
		data1.setValue( MockNode.MOCK_ID, key );
		assertThat( data1.equals( data2 ) ).isEqualTo( false  );

		data2.setValue( MockNode.MOCK_ID, key );
		assertThat( data1.equals( data2 ) ).isEqualTo( true  );

		// Test the natural key
		data1.setValue( "lastName", lastName );
		assertThat( data1.equals( data2 ) ).isEqualTo( false  );

		data2.setValue( "lastName", lastName );
		assertThat( data1.equals( data2 ) ).isEqualTo( true  );
	}

}
