package com.avereon.data;

import com.avereon.transaction.TxnEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;
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
	void testEventSpreadOnValueChangeEvents() {
		MockNode a = new MockNode( "a" );
		MockNode b = new MockNode( "b" );
		a.setValue( "child", b );

		// This shows the problem with listeners on the same key and the nodes
		// related to each other. The event from b is also propagated to a and
		// now a receives the value change event from b...unknowingly.
		AtomicInteger count = new AtomicInteger();
		a.register( "key", e -> count.incrementAndGet() );
		b.register( "key", e -> count.incrementAndGet() );

		b.setValue( "key", "b" );
		assertThat( count.get() ).isEqualTo( 1 );
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
	void testRefresh() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0  );
		assertThat( data.getEventCount() ).isEqualTo( index  );

		data.refresh();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0  );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		assertThat( data.getEventCount() ).isEqualTo( index  );
	}

}
