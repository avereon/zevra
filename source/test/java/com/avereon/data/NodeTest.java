package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnEvent;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

	private MockNode data;

	@BeforeEach
	void setup() {
		data = new MockNode();
	}

	@Test
	void testAsMap() {
		Object o = new Object();
		data.setValue( "0", 0 );
		data.setValue( "a", "A" );
		data.setValue( "n", "not included" );
		data.setValue( "o", o );

		// Intentionally don't include 'n' to make sure it is not included
		assertThat( data.asMap( "0", "a", "o" ), is( Map.of( "0", 0, "a", "A", "o", o ) ) );

		// Intentionally try to include 'b' to make sure null values are not included
		assertThat( data.asMap( "0", "a", "b", "o" ), is( Map.of( "0", 0, "a", "A", "o", o ) ) );
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
		assertThat( count.get(), is( 1 ) );
	}

	@Test
	void testDistanceTo() {
		MockNode grandParent = new MockNode( "grandParent" );
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );

		assertThat( child.distanceTo( grandParent ), is( -1 ) );
		assertThat( child.distanceTo( parent ), is( -1 ) );

		grandParent.setValue( "child", parent );
		parent.setValue( "child", child );

		assertThat( child.distanceTo( grandParent ), is( 2 ) );
		assertThat( child.distanceTo( parent ), is( 1 ) );
		assertThat( child.distanceTo( child ), is( 0 ) );
	}

	@Test
	void testNewNodeModifiedState() {
		assertThat( data.isModified(), is( false ) );
	}

	@Test
	void testModifyByFlagAndUnmodifyByFlag() {
		int index = 0;
		assertThat( data.isModifiedByValue(), is( false ) );
		assertThat( data.isModifiedByChild(), is( false ) );
		assertThat( data.isModifiedBySelf(), is( false ) );
		assertThat( data.isModified(), is( false ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setModified( true );
		assertThat( data.isModifiedByValue(), is( false ) );
		assertThat( data.isModifiedByChild(), is( false ) );
		assertThat( data.isModifiedBySelf(), is( true ) );
		assertThat( data.isModified(), is( true ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.setModified( false );
		assertThat( data.isModifiedByValue(), is( false ) );
		assertThat( data.isModifiedByChild(), is( false ) );
		assertThat( data.isModifiedBySelf(), is( false ) );
		assertThat( data.isModified(), is( false ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testModifyByValueAndUnmodifyByFlag() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "key", 423984 );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "key", null, 423984 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testModifyByValueAndUnmodifyByValue() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "key", 423984 );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "key", null, 423984 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "key", null );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "key", 423984, null );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testGetValueKeys() {
		data.setValue( "key", "value" );
		assertThat( data.getValueKeys(), containsInAnyOrder( "key" ) );
	}

	@Test
	void testGetAndSetValue() {
		String key = "key";
		Object value = "value";
		assertThat( data.getValue( key ), is( nullValue() ) );

		data.setValue( key, value );
		assertThat( data.getValue( key ), is( value ) );

		data.setValue( key, null );
		assertThat( data.getValue( key ), is( nullValue() ) );
	}

	@Test
	void testObjectValue() {
		String key = "key";
		Object value = new Object();
		assertThat( data.getValue( key ), is( nullValue() ) );

		data.setValue( key, value );
		assertThat( data.getValue( key ), is( value ) );
	}

	@Test
	void testStringValue() {
		String key = "key";
		String value = "value";
		assertThat( data.getValue( key ), is( nullValue() ) );

		data.setValue( key, value );
		assertThat( data.getValue( key ), is( value ) );
	}

	@Test
	void testBooleanValue() {
		String key = "key";
		assertThat( data.getValue( key ), is( nullValue() ) );
		data.setValue( key, true );
		assertThat( data.getValue( key ), is( true ) );
	}

	@Test
	void testIntegerValue() {
		String key = "key";
		int value = 0;
		assertThat( data.getValue( key ), is( nullValue() ) );

		data.setValue( key, value );
		assertThat( data.getValue( key ), is( value ) );
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
		assertNotSame( A, B );

		// This checks that the two objects are the same value
		assertEquals( A, B );

		assertThat( data.getValue( key ), is( nullValue() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.setValue( key, A );
		assertThat( data.getValue( key ), is( A ) );
		assertThat( data, hasStates( true, false, 1, 0 ) );

		data.setModified( false );
		assertThat( data.getValue( key ), is( A ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.setValue( key, B );
		assertThat( data.getValue( key ), is( B ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );
	}

	@Test
	void testSetNullValueToNull() {
		int index = 0;
		String key = "key";
		assertThat( data.getValue( key ), is( nullValue() ) );
		data.setValue( key, null );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testSetValueWithNullName() {
		try {
			data.setValue( null, "value" );
			fail( "Null value keys are not allowed" );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage(), is( "Value key cannot be null" ) );
		}
		assertThat( data.getEventCount(), is( 0 ) );
	}

	@Test
	void testGetValueWithNullKey() {
		try {
			data.getValue( null );
			fail( "Null value keys are not allowed." );
		} catch( NullPointerException exception ) {
			assertThat( exception.getMessage(), is( "Value key cannot be null" ) );
		}
	}

	@Test
	void testNullValues() {
		int index = 0;

		// Assert initial values
		assertThat( data.getValue( "key" ), is( nullValue() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value to null and make sure nothing happens
		data.setValue( "key", null );
		assertThat( data.getValue( "key" ), is( nullValue() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value
		data.setValue( "key", "value" );
		assertThat( data.getValue( "key" ), is( "value" ) );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "key", null, "value" );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value to the same value and make sure nothing happens
		data.setValue( "key", "value" );
		assertThat( data.getValue( "key" ), is( "value" ) );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value back to null
		data.setValue( "key", null );
		assertThat( data.getValue( "key" ), is( nullValue() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "key", "value", null );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value to null again and make sure nothing happens
		data.setValue( "key", null );
		assertThat( data.getValue( "key" ), is( nullValue() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testGetWithNullSupplier() {
		assertNull( data.getValue( "x", (Supplier<String>)null ) );
	}

	@Test
	void testGetAndSetValueEvents() {
		List<NodeEvent> events = new ArrayList<>();
		data.register( "x", events::add );

		int index = 0;
		assertThat( data.getValue( "x" ), is( nullValue() ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1.0 );
		assertThat( data.getValue( "x" ), is( 1.0 ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		//assertThat( events.get( 0 ), hasEventState( data, NodeEvent.VALUE_CHANGED, "x", null, 1.0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 1.0 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 0.0 );
		assertThat( data.getValue( "x" ), is( 0.0 ) );
		assertThat( data.getValue( "y" ), is( nullValue() ) );
		assertThat( data.getValue( "z" ), is( nullValue() ) );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertThat( events.get( 1 ), hasEventState( data, NodeEvent.VALUE_CHANGED, "x", 1.0, 0.0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", 1.0, 0.0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testGetValueWithDefault() {
		assertThat( data.getValue( "key" ), is( nullValue() ) );
		assertThat( data.getValue( "key", "default" ), is( "default" ) );
	}

	@Test
	void testModifiedBySetAttribute() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, false, 3, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 1 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", null, 2 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", null, 3 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testUnmodifiedByUnsetValue() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 0 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", null, 0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", null, 0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, false, 3, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", 0, 1 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", 0, 2 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", 0, 3 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 0 );
		data.setValue( "y", 0 );
		data.setValue( "z", 0 );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", 1, 0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", 2, 0 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", 3, 0 );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testModifiedAttributeCountResetByClearingModifiedFlag() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, false, 3, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 1 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", null, 2 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", null, 3 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testRefresh() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.refresh();
		assertThat( data, hasStates( false, false, 0, 0 ) );
		//assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		//assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		//assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testRefreshOnChildCausesParentNodeChangedEvent() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, false, 1, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( child, childIndex++, NodeEvent.ADDED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.CHILD_ADDED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		child.refresh();
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		//assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		//assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		//assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );
		//assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		//assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		//assertEventState( child, childIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testClearWithNonModifyingValues() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "u", 1 );
		data.setValue( "v", 2 );
		data.setValue( "w", 3 );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		// There should not be a MODIFIED event
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "u", null, 1 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "v", null, 2 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "w", null, 3 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.clear();
		assertThat( data, hasStates( false, false, 0, 0 ) );
		// There should not be an UNMODIFIED event
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "u", 1, null );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "v", 2, null );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "w", 3, null );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testClearWithModifyingValues() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "x", 1 );
		data.setValue( "y", 2 );
		data.setValue( "z", 3 );
		assertThat( data, hasStates( true, false, 3, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", null, 1 );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", null, 2 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );

		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", null, 3 );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.clear();
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "x", 1, null );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "y", 2, null );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "z", 3, null );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testValueKeys() {
		data.setValue( "key", "value" );
		assertThat( data.getValueKeys(), containsInAnyOrder( "key" ) );
	}

	@Test
	void testIsSet() {
		assertFalse( data.isSet( "key" ) );
		data.setValue( "key", "value" );
		assertTrue( data.isSet( "key" ) );
		data.setValue( "key", null );
		assertFalse( data.isSet( "key" ) );
	}

	@Test
	void testIsNotSet() {
		assertTrue( data.isNotSet( "key" ) );
		data.setValue( "key", "value" );
		assertFalse( data.isNotSet( "key" ) );
		data.setValue( "key", null );
		assertTrue( data.isNotSet( "key" ) );
	}

	@Test
	void testValue() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "name", "mock" );
		assertThat( data.getValue( "name" ), is( "mock" ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "name", null, "mock" );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "name", null );
		assertThat( data.getValue( "name" ), is( nullValue() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "name", "mock", null );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testComputeIfAbsent() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		assertNull( data.getValue( "name" ) );
		assertThat( data.computeIfAbsent( "name", k -> "mock" ), is( "mock" ) );
		assertThat( data.getValue( "name" ), is( "mock" ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "name", null, "mock" );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		data.setValue( "name", null );
		assertThat( data.getValue( "name" ), is( nullValue() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "name", "mock", null );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testValues() {
		assertThat( data.getValues(), is( Set.of() ) );
		data.setValue( "0", 0 );
		data.setValue( "a", "A" );
		data.setValue( "b", "B" );
		assertThat( data.getValues(), containsInAnyOrder( 0, "A", "B" ) );
		assertTrue( data.isModified() );
	}

	@Test
	void testValuesOfType() {
		assertThat( data.getValues( String.class ), is( Set.of() ) );
		data.setValue( "0", 0 );
		data.setValue( "a", "A" );
		data.setValue( "b", "B" );
		assertThat( data.getValues( Integer.class ), containsInAnyOrder( 0 ) );
		assertThat( data.getValues( String.class ), containsInAnyOrder( "A", "B" ) );
		assertTrue( data.isModified() );
	}

	@Test
	void testNodeSetAddWithNull() {
		assertThat( data.getValues( MockNode.ITEMS ), is( Set.of() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.addItem( null );
		assertThat( data, hasStates( false, false, 0, 0 ) );
	}

	@Test
	void testNodeSetAddRemove() {
		MockNode item = new MockNode();
		assertThat( data.getValues( MockNode.ITEMS ), is( Set.of() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.addItem( item );
		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item ) );
		assertThat( data, hasStates( true, false, 0, 1 ) );

		data.removeItem( item );
		assertThat( data.getValues( MockNode.ITEMS ), is( Set.of() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.addItem( item );
		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item ) );
		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.removeItem( item );
		assertThat( data.getValues( MockNode.ITEMS ), is( Set.of() ) );
		assertThat( data, hasStates( true, false, 0, 1 ) );
	}

	@Test
	void testNodeSetAddRemoveDataStructure() {
		MockNode item = new MockNode();
		assertNull( item.getParent() );

		data.addItem( item );
		assertNotNull( item.getParent() );
		assertThat( item.getTrueParent(), isA( NodeSet.class ) );
		assertThat( item.getParent(), is( data ) );

		data.removeItem( item );
		assertNull( item.getParent() );
	}

	@Test
	void testNodeSetAddRemoveEvents() {
		int index = 0;
		int itemIndex = 0;
		MockNode item = new MockNode();

		data.addItem( item );
		NodeSet<?> items = data.getValue( "items" );
		assertEventState( item, itemIndex++, NodeEvent.ADDED );
		assertThat( item.getEventCount(), is( itemIndex ) );
		assertEventState( data, index++, items, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.CHILD_ADDED, item.getTrueParent(), "items", item.getCollectionId(), null, item );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, item.getTrueParent(), "items", item.getCollectionId(), null, item );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, items, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, items, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		Node set = item.getTrueParent();
		data.removeItem( item );
		assertEventState( item, itemIndex++, NodeEvent.REMOVED );
		assertThat( item.getEventCount(), is( itemIndex ) );
		assertEventState( data, index++, items, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.CHILD_REMOVED, set, "items", item.getCollectionId(), item, null );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, set, "items", item.getCollectionId(), item, null );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, items, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, items, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testAddMultipleSetItems() {
		MockNode item1 = new MockNode();
		MockNode item2 = new MockNode();
		assertThat( data.getValues( MockNode.ITEMS ), is( Set.of() ) );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.addItem( item1 );
		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item1 ) );
		assertThat( data, hasStates( true, false, 0, 1 ) );
		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.addItem( item2 );
		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item1, item2 ) );
		assertThat( data, hasStates( true, false, 0, 1 ) );
		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );
	}

	@Test
	void testRemoveFromMultipleSetItems() {
		MockNode item1 = new MockNode();
		MockNode item2 = new MockNode();
		data.addItem( item1 ).addItem( item2 );
		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item1, item2 ) );
		assertThat( data, hasStates( true, false, 0, 1 ) );
		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.removeItem( item1 );
		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item2 ) );
		assertThat( data, hasStates( true, false, 0, 1 ) );
		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );

		data.removeItem( item2 );
		assertThat( data.getValues( MockNode.ITEMS ), is( Set.of() ) );
		assertNull( data.getValue( MockNode.ITEMS ) );
		assertThat( data, hasStates( true, false, 0, 1 ) );
		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );
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

		assertThat( data.getItems(), containsInAnyOrder( item0, item1, item2, item3 ) );
		assertThat( data.getItems().size(), is( 4 ) );
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

		assertThat( data.getItems(), containsInAnyOrder( item0, item1, item2, item3 ) );
		assertThat( data.getItems().size(), is( 4 ) );
	}

	@Test
	void testResourceChangesOnChildCausesParentNodeChangedEvent() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, false, 1, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( child, childIndex++, NodeEvent.ADDED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.CHILD_ADDED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		child.setValue( "name", "mock" );
		assertThat( child.getValue( "name" ), is( "mock" ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "name", null, "mock" );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "name", null, "mock" );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		child.setValue( "name", null );
		assertThat( child.getValue( "name" ), is( nullValue() ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "name", "mock", null );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "name", "mock", null );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testDataEventNotification() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		// Set a value
		data.setValue( "key", "value0" );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "key", null, "value0" );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		// Set the value to the same value
		data.setValue( "key", "value0" );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		// Modify the value
		data.setValue( "key", "value1" );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "key", "value0", "value1" );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		// Remove the attribute.
		data.setValue( "key", null );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "key", "value1", null );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testEventsWithModifiedFlagFalse() {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		// Change a value
		data.setValue( "key", "value0" );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "key", null, "value0" );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		// Set the modified flag to false
		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.UNMODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );

		// Set the modified flag to false again
		data.setModified( false );
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testSetClearSetValueInTransaction() throws Exception {
		data.setValue( "x", 1 );
		Txn.create();
		data.setValue( "x", null );
		data.setValue( "x", 1 );
		Txn.commit();
		assertThat( data.getValue( "x" ), is( 1 ) );
	}

	@Test
	void testCollapsingEventsWithTransaction() throws Exception {
		int index = 0;
		assertThat( data, hasStates( false, false, 0, 0 ) );
		assertThat( data.getEventCount(), is( index ) );

		Txn.create();
		data.setValue( "a", "1" );
		data.setValue( "a", "2" );
		data.setValue( "a", "3" );
		data.setValue( "a", "4" );
		data.setValue( "a", "5" );
		Txn.commit();

		assertThat( data.getValue( "a" ), is( "5" ) );
		assertThat( data, hasStates( true, false, 1, 0 ) );
		assertEventState( data, index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( data, index++, NodeEvent.VALUE_CHANGED, "a", null, "5" );
		assertEventState( data, index++, NodeEvent.MODIFIED );
		assertEventState( data, index++, NodeEvent.NODE_CHANGED );
		assertEventState( data, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( data, index++, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount(), is( index ) );
	}

	@Test
	void testGetParent() {
		MockNode parent = new MockNode();
		MockNode child = new MockNode();
		assertThat( child.getParent(), is( nullValue() ) );

		String key = "key";

		parent.setValue( key, child );
		assertThat( child.getParent(), is( parent ) );

		parent.setValue( key, null );
		assertThat( child.getParent(), is( nullValue() ) );
	}

	@Test
	void testGetNodePath() {
		MockNode parent = new MockNode();
		MockNode child = new MockNode();

		parent.setValue( "child", child );

		List<Node> path = parent.getNodePath();
		assertThat( path.size(), is( 1 ) );
		assertThat( path.get( 0 ), is( parent ) );

		path = child.getNodePath();
		assertThat( path.size(), is( 2 ) );
		assertThat( path.get( 0 ), is( parent ) );
		assertThat( path.get( 1 ), is( child ) );
	}

	@Test
	void testParentGetsModifiedAndUnmodifiedWithChildModifyFlag() {
		MockNode grandparent = new MockNode( "grandparent" );
		MockNode parent = new MockNode( "parent" );
		grandparent.setValue( "child", parent );
		grandparent.setModified( false );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );

		assertFalse( grandparent.isModified() );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertThat( grandparent, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );

		child.setModified( true );
		assertTrue( grandparent.isModified() );
		assertTrue( parent.isModified() );
		assertTrue( child.isModified() );
		assertThat( grandparent, hasStates( true, false, 0, 1 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertThat( child, hasStates( true, true, 0, 0 ) );

		parent.setModified( false );
		assertFalse( grandparent.isModified() );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertThat( grandparent, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
	}

	@Test
	void testParentGetsModifiedEventsWhenChildModifiedAndUnmodified() {
		// Start with a standard parent/child model
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		int index = 0;

		// Set an attribute on the child to modify the child and parent
		child.setValue( "key", "value0" );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", null, "value0" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );

		// Change the attribute value on the child
		child.setValue( "key", "value1" );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value0", "value1" );
		// The parent is already modified so there should not be a modified event here
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );

		// Set the child attribute back to null to unmodify the child and parent
		child.setValue( "key", null );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value1", null );
		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );
	}

	@Test
	void testChildModifiedClearedWhenParentModifiedCleared() {
		// Start with a standard parent/child model
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		int index = 0;

		// Set an attribute on the child to modify the child and parent
		child.setValue( "key", "value0" );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", null, "value0" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );

		// Clear the parent modified flag
		parent.setModified( false );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, index++, TxnEvent.COMMIT_BEGIN );

		// FIXME Should the child events be wrapped in a Txn
		//assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		//assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		//assertEventState( parent, index++, child, TxnEvent.COMMIT_END );

		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );
	}

	@Test
	void testParentModifiedAndUnmodifiedByChildNodeAttributeChangeWithNullStartValue() {
		// Start with a standard parent/child model
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		int index = 0;

		// Test setting a value on the child node modifies the parent
		child.setValue( "key", "value" );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", null, "value" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );

		// Test clear the value on the child node unmodifies the parent
		child.setValue( "key", null );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value", null );
		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );
	}

	@Test
	void testParentModifiedAndUnmodifiedByChildNodeAttributeChangeWithNonNullStartValue() {
		// Start with a standard parent/child model
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.getWatcher().reset();
		parent.getWatcher().reset();
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		int index = 0;

		// Set an attribute on the child to a non-null value
		child.setValue( "key", "value0" );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", null, "value0" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );

		// Clear the modified flags
		parent.setModified( false );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, index++, TxnEvent.COMMIT_BEGIN );
		// FIXME Should the child events be wrapped in a Txn
		//assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		//assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		//assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, TxnEvent.COMMIT_END );

		// Change the attribute value on the child
		child.setValue( "key", "value1" );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value0", "value1" );
		assertEventState( parent, index++, NodeEvent.MODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );

		// Set the child attribute to the same value, should do nothing
		child.setValue( "key", "value1" );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );

		// Set the child attribute back to value0
		child.setValue( "key", "value0" );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value1", "value0" );
		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( index ) );
	}

	@Test
	void testGrandparentModifiedByChildNodeAttributeChange() {
		int parentIndex = 0;
		int childIndex = 0;
		int grandChildIndex = 0;
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		MockNode grandChild = new MockNode( "grandChild" );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, false, 1, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, false, 0, 0 ) );

		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.CHILD_ADDED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, NodeEvent.ADDED );
		//assertEventState( child, childIndex++, parent, NodeEvent.PARENT_CHANGED );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		child.setValue( "child", grandChild );
		assertThat( grandChild.getParent(), is( child ) );
		assertThat( parent, hasStates( true, false, 1, 1 ) );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( grandChild, hasStates( false, false, 0, 0 ) );

		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.CHILD_ADDED, child, "child", null, grandChild );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "child", null, grandChild );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.CHILD_ADDED, "child", null, grandChild );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "child", null, grandChild );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );

		assertEventState( grandChild, grandChildIndex++, NodeEvent.ADDED );
		//assertEventState( grandChild, grandChildIndex++, parent, NodeEvent.PARENT_CHANGED );

		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		parent.setModified( false );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, false, 0, 0 ) );

		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		// FIXME Should the child events be wrapped in a Txn
		//assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		//assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		//assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );

		// FIXME Should the child events be wrapped in a Txn
		//assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		//assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		//assertEventState( child, childIndex++, TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		// Test setting a value on the child node modifies the parents
		grandChild.setValue( "key", "value" );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertThat( child, hasStates( true, false, 0, 1 ) );
		assertThat( grandChild, hasStates( true, false, 1, 0 ) );

		assertEventState( parent, parentIndex++, grandChild, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, grandChild, "key", null, "value" );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED, parent, null, null, null );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, grandChild, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, grandChild, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, grandChild, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, grandChild, "key", null, "value" );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED, child, null, null, null );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, grandChild, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, grandChild, TxnEvent.COMMIT_END );

		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.VALUE_CHANGED, "key", null, "value" );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.MODIFIED );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		// Test unsetting the value on the child node unmodifies the parents
		grandChild.setValue( "key", null );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, parentIndex++, grandChild, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, grandChild, "key", "value", null );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, grandChild, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, grandChild, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, grandChild, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, grandChild, "key", "value", null );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, grandChild, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, grandChild, TxnEvent.COMMIT_END );

		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.VALUE_CHANGED, "key", "value", null );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.UNMODIFIED );
		assertEventState( grandChild, grandChildIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );
	}

	@Test
	void testParentModifiedByChildNodeClearedByFlag() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, false, 1, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( child, childIndex++, NodeEvent.ADDED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.CHILD_ADDED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "1" );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertThat( child, hasStates( true, false, 1, 0 ) );

		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "a", null, "1" );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "a", null, "1" );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "1" );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertThat( child, hasStates( true, false, 2, 0 ) );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "b", null, "1" );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "b", null, "1" );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Set this state as the new unmodified state
		child.setModified( false );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );

		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testParentModifiedByChildNodeClearedByValue() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, false, 1, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );

		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.CHILD_ADDED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, NodeEvent.ADDED );
		//assertEventState( child, childIndex++, parent, NodeEvent.PARENT_CHANGED );

		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "2" );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );

		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "a", null, "2" );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "a", null, "2" );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "2" );
		assertThat( child, hasStates( true, false, 2, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "b", null, "2" );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "b", null, "2" );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test unsetting the 'a' value on the child leaves the parent modified
		child.setValue( "a", null );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "a", "2", null );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "a", "2", null );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test unsetting the value 'b' on the child returns the parent to unmodified
		child.setValue( "b", null );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );

		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "b", "2", null );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "b", "2", null );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testChildModifiedClearedByParentSetModifiedFalse() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, false, 1, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( child, childIndex++, NodeEvent.ADDED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.CHILD_ADDED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "x", "2" );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( parent, hasStates( true, false, 0, 1 ) );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.VALUE_CHANGED, child, "x", null, "2" );
		assertEventState( parent, parentIndex++, NodeEvent.MODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );

		assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.VALUE_CHANGED, "x", null, "2" );
		assertEventState( child, childIndex++, NodeEvent.MODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setModified( false );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_BEGIN );
		// FIXME Should the child events be wrapped in a Txn
		//assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		//assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_SUCCESS );
		//assertEventState( parent, parentIndex++, child, TxnEvent.COMMIT_END );
		assertEventState( parent, parentIndex++, NodeEvent.UNMODIFIED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, TxnEvent.COMMIT_END );

		// FIXME Should the child events be wrapped in a Txn
		//assertEventState( child, childIndex++, TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, NodeEvent.UNMODIFIED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		//assertEventState( child, childIndex++, TxnEvent.COMMIT_SUCCESS );
		//assertEventState( child, childIndex++, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testAddNodeAttributeToDifferentParent() {
		MockNode parent0 = new MockNode( "parent0" );
		MockNode parent1 = new MockNode( "parent1" );
		MockNode child = new MockNode( "child" );
		int parent0Index = 0;
		int parent1Index = 0;
		int childIndex = 0;
		assertThat( parent0, hasStates( false, false, 0, 0 ) );
		assertThat( parent1, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent0.getEventCount(), is( parent0Index ) );
		assertThat( parent1.getEventCount(), is( parent1Index ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Add the child attribute to parent 0
		parent0.setValue( "child", child );
		assertThat( parent0, hasStates( true, false, 1, 0 ) );
		assertThat( parent1, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( child, childIndex++, NodeEvent.ADDED );
		//assertEventState( child, childIndex++, parent0, NodeEvent.PARENT_CHANGED );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent0, parent0Index++, NodeEvent.CHILD_ADDED, "child", null, child );
		assertEventState( parent0, parent0Index++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent0, parent0Index++, NodeEvent.MODIFIED );
		assertEventState( parent0, parent0Index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_END );
		assertThat( parent0.getEventCount(), is( parent0Index ) );
		assertThat( parent1.getEventCount(), is( parent1Index ) );

		// Clear the modified flag of parent 0.
		parent0.setModified( false );
		assertThat( child.getParent(), is( parent0 ) );
		assertThat( parent0, hasStates( false, false, 0, 0 ) );
		assertThat( parent1, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent0, parent0Index++, NodeEvent.UNMODIFIED );
		assertEventState( parent0, parent0Index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_END );
		assertThat( parent0.getEventCount(), is( parent0Index ) );
		assertThat( parent1.getEventCount(), is( parent1Index ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		// Add the child attribute to parent 1.
		parent1.setValue( "child", child );
		assertThat( child.getParent(), is( parent1 ) );
		assertThat( parent1.getValue( "child" ), is( child ) );
		assertThat( parent0.getValue( "child" ), is( nullValue() ) );
		assertThat( parent0, hasStates( true, false, 1, 0 ) );
		assertThat( parent1, hasStates( true, false, 1, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertEventState( child, childIndex++, NodeEvent.REMOVED );
		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent0, parent0Index++, NodeEvent.CHILD_REMOVED, "child", child, null );
		assertEventState( parent0, parent0Index++, NodeEvent.VALUE_CHANGED, "child", child, null );
		assertEventState( parent0, parent0Index++, NodeEvent.MODIFIED );
		assertEventState( parent0, parent0Index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_END );
		assertEventState( child, childIndex++, NodeEvent.ADDED );
		assertEventState( parent1, parent1Index++, TxnEvent.COMMIT_BEGIN );
		assertEventState( parent1, parent1Index++, NodeEvent.CHILD_ADDED, "child", null, child );
		assertEventState( parent1, parent1Index++, NodeEvent.VALUE_CHANGED, "child", null, child );
		assertEventState( parent1, parent1Index++, NodeEvent.MODIFIED );
		assertEventState( parent1, parent1Index++, NodeEvent.NODE_CHANGED );
		assertEventState( parent1, parent1Index++, TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent1, parent1Index++, TxnEvent.COMMIT_END );
		//assertEventState( child, childIndex++, parent1, NodeEvent.PARENT_CHANGED );
		assertThat( parent0.getEventCount(), is( parent0Index ) );
		assertThat( parent1.getEventCount(), is( parent1Index ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testChildReceivesParentChangedEvent() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, false, 1, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( child.getEventCount(), is( childIndex += 1 ) );

		parent.addModifyingKeys( "x" );
		parent.setValue( "x", 1 );
		assertEventState( child, childIndex++, parent, NodeEvent.PARENT_CHANGED );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testGrandchildReceivesParentChangedEvent() {
		int parentIndex = 0;
		int childIndex = 0;
		int grandChildIndex = 0;
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		MockNode grandChild = new MockNode( "grandChild" );
		assertThat( parent, hasStates( false, false, 0, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		parent.setValue( "child", child );
		assertThat( child.getParent(), is( parent ) );
		assertThat( parent, hasStates( true, false, 1, 0 ) );
		assertThat( child, hasStates( false, false, 0, 0 ) );
		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
		assertThat( child.getEventCount(), is( childIndex += 1 ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );

		child.setValue( "child", grandChild );
		assertThat( grandChild.getParent(), is( child ) );
		assertThat( parent, hasStates( true, false, 1, 1 ) );
		assertThat( child, hasStates( true, false, 1, 0 ) );
		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
		assertThat( child.getEventCount(), is( childIndex += 7 ) );
		assertThat( grandChild.getEventCount(), is( grandChildIndex += 1 ) );

		parent.addModifyingKeys( "x" );
		parent.setValue( "x", 1 );
		assertEventState( child, childIndex++, parent, NodeEvent.PARENT_CHANGED );
		assertThat( child.getEventCount(), is( childIndex ) );
		assertEventState( grandChild, grandChildIndex++, parent, NodeEvent.PARENT_CHANGED );
		assertThat( grandChild.getEventCount(), is( grandChildIndex ) );
	}

	@Test
	void testParentNodeNotModifiedByNodeAddedWithSetWithModifyFilter() {
		int parentIndex = 12;
		int childIndex = 1;

		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertFalse( child.isModified() );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );

		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( child.getEventCount(), is( childIndex ) );

		MockNode item0 = new MockNode( "0" );
		item0.setValue( "dont-modify", true );
		assertFalse( item0.isModified() );
		// No new events should have occurred
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		child.removeItem( item0 );
		assertFalse( child.isModified() );
		assertFalse( parent.isModified() );
		child.removeItem( item0 );
		assertFalse( child.isModified() );
		assertFalse( parent.isModified() );
		// No new events should have occurred
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		child.addItem( item0 );
		assertFalse( child.isModified() );
		assertFalse( parent.isModified() );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_ADDED );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_ADDED );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( child.getEventCount(), is( childIndex ) );
		child.addItem( item0 );
		assertFalse( child.isModified() );
		assertFalse( parent.isModified() );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );

		child.removeItem( item0 );
		assertFalse( child.isModified() );
		assertFalse( parent.isModified() );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_REMOVED );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		assertEventState( parent, parentIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		assertEventState( parent, parentIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_REMOVED );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		assertEventState( child, childIndex++, NodeEvent.NODE_CHANGED );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		assertEventState( child, childIndex++, child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( child.getEventCount(), is( childIndex ) );
		child.removeItem( item0 );
		assertFalse( child.isModified() );
		assertFalse( parent.isModified() );
		assertThat( parent.getEventCount(), is( parentIndex ) );
		assertThat( child.getEventCount(), is( childIndex ) );
	}

	@Test
	void testNodeNotModifiedByChildAddUntilNodeFilterValueChange() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );

		MockNode item0 = new MockNode( "A" );

		// Make sure the modified flag is working as expected before using the dont-modify value
		child.addItem( item0 );
		assertTrue( parent.isModified() );
		assertTrue( child.isModified() );
		assertFalse( item0.isModified() );
		child.removeItem( item0 );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertFalse( item0.isModified() );

		// The dont-modify is not a modifying key
		item0.setValue( "dont-modify", true );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertFalse( item0.isModified() );

		// Adding the child should not cause any node to be modified
		child.addItem( item0 );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertFalse( item0.isModified() );

		// Some extra checking
		child.removeItem( item0 );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertFalse( item0.isModified() );
		child.addItem( item0 );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertFalse( item0.isModified() );
	}

	@Test
	void testNodeNotModifiedByChildRemoveUntilNodeFilterValueChange() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );

		MockNode item0 = new MockNode( "A" );

		// Make sure the modified flag is working as expected before using the dont-modify value
		child.addItem( item0 );
		assertTrue( parent.isModified() );
		assertTrue( child.isModified() );
		assertFalse( item0.isModified() );
		child.removeItem( item0 );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertFalse( item0.isModified() );

		item0.setValue( "dont-modify", true );
		child.addItem( item0 );
		assertFalse( parent.isModified() );
		assertFalse( child.isModified() );
		assertFalse( item0.isModified() );

		// This does not modify the node because dont-modify is not a modifying key
		item0.setValue( "dont-modify", null );
		child.removeItem( item0 );
		assertTrue( parent.isModified() );
		assertTrue( child.isModified() );
		assertFalse( item0.isModified() );
	}

	@Test
	void testParentNodeNotModifiedByNodeSetWithModifyFilter() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );

		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertFalse( child.isModified() );

		MockNode item0 = new MockNode( "0" );
		child.addItem( item0 );
		assertFalse( item0.isModified() );
		assertTrue( child.isModified() );
		assertTrue( parent.isModified() );
		child.setModified( false );

		assertFalse( item0.isModified() );
		assertFalse( child.isModified() );
		assertFalse( parent.isModifiedByValue() );
		assertFalse( parent.isModifiedByChild() );
		assertFalse( parent.isModified() );

		item0.setValue( "dont-modify", true );
		assertFalse( item0.isModified() );
		assertFalse( child.isModified() );
		assertFalse( parent.isModified() );

		item0.setValue( "a", "A" );
		assertTrue( item0.isModified() );
		assertFalse( child.isModified() );
		assertFalse( parent.isModified() );

		item0.setValue( "dont-modify", null );
		assertFalse( child.isModified() );
		assertFalse( parent.isModified() );
		child.removeItem( item0 );
		assertTrue( child.isModified() );
		assertTrue( parent.isModified() );
		child.setModified( false );
	}

	@Test
	void testParentModifiedByAddingModifiedChild() {
		MockNode child = new MockNode( "child" );
		child.setModified( true );
		assertFalse( data.isModified() );
		assertTrue( child.isModified() );

		data.addItem( new MockNode().addItem( new MockNode().addItem( child ) ) );
		assertTrue( data.isModified() );
		assertTrue( child.isModified() );

		data.setModified( false );
		assertFalse( data.isModified() );
		assertFalse( child.isModified() );
	}

	@Test
	void testAddDataListener() {
		// Remove the default watcher
		data.unregister( Event.ANY, data.getWatcher() );

		EventHandler<NodeEvent> listener = e -> {};
		data.register( NodeEvent.ANY, listener );

		Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> handlers = data.getEventHandlers();

		assertNotNull( handlers );
		assertThat( handlers.size(), is( 1 ) );
		assertThat( handlers.get( NodeEvent.ANY ), contains( listener ) );
	}

	@Test
	void testRemoveDataListener() {
		// Remove the default watcher
		data.unregister( Event.ANY, data.getWatcher() );

		Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> handlers;
		EventHandler<NodeEvent> listener = e -> {};

		data.register( NodeEvent.ANY, listener );
		handlers = data.getEventHandlers();
		assertNotNull( handlers );
		assertThat( handlers.size(), is( 1 ) );
		assertThat( handlers.get( NodeEvent.ANY ), contains( listener ) );

		data.unregister( NodeEvent.ANY, listener );
		handlers = data.getEventHandlers();
		assertNotNull( handlers );
		assertThat( handlers.size(), is( 0 ) );
		assertThat( handlers.get( NodeEvent.ANY ), not( contains( listener ) ) );

		data.register( NodeEvent.ANY, listener );
		handlers = data.getEventHandlers();
		assertNotNull( handlers );
		assertThat( handlers.size(), is( 1 ) );
		assertThat( handlers.get( NodeEvent.ANY ), contains( listener ) );

		data.unregister( NodeEvent.ANY, listener );
		handlers = data.getEventHandlers();
		assertNotNull( handlers );
		assertThat( handlers.size(), is( 0 ) );
		assertThat( handlers.get( NodeEvent.ANY ), not( contains( listener ) ) );
	}

	@Test
	void testCircularReferenceCheck() {
		MockNode node = new MockNode();
		try {
			node.setValue( "node", node );
			fail( "CircularReferenceException should be thrown" );
		} catch( CircularReferenceException exception ) {
			// Intentionally ignore exception.
			assertThat( exception.getMessage(), startsWith( "Circular reference detected" ) );
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
		assertThat( node1.getValue( "key1" ), is( "value1" ) );
		assertThat( node1.getValue( "key2" ), is( "value2" ) );
		assertThat( node2.getValue( "key1" ), is( "value1" ) );
		assertThat( node2.getValue( "key2" ), is( "valueB" ) );
	}

	@Test
	void testCopyFromWithOverwrite() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1, true );
		assertThat( node1.getValue( "key1" ), is( "value1" ) );
		assertThat( node1.getValue( "key2" ), is( "value2" ) );
		assertThat( node2.getValue( "key1" ), is( "value1" ) );
		assertThat( node2.getValue( "key2" ), is( "value2" ) );
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
		assertThat( node1.getValue( "key1" ), is( "value1" ) );
		assertThat( node1.getValue( "key2" ), is( "value2" ) );
		assertThat( node1.getValue( MockNode.MOCK_ID ), is( "a" ) );
		assertThat( node2.getValue( "key1" ), is( "value1" ) );
		assertThat( node2.getValue( "key2" ), is( "value2" ) );
		assertThat( node2.getValue( MockNode.MOCK_ID ), is( "b" ) );
	}

	@Test
	void testCopyFromUsingResources() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1 );
		assertThat( node1.getValue( "key1" ), is( "value1" ) );
		assertThat( node1.getValue( "key2" ), is( "value2" ) );
		assertThat( node2.getValue( "key1" ), is( "value1" ) );
		assertThat( node2.getValue( "key2" ), is( "valueB" ) );
	}

	@Test
	void testCopyFromWithOverwriteUsingResources() {
		Node node1 = new Node();
		node1.setValue( "key1", "value1" );
		node1.setValue( "key2", "value2" );

		Node node2 = new Node();
		node2.setValue( "key2", "valueB" );

		node2.copyFrom( node1, true );
		assertThat( node1.getValue( "key1" ), is( "value1" ) );
		assertThat( node1.getValue( "key2" ), is( "value2" ) );
		assertThat( node2.getValue( "key1" ), is( "value1" ) );
		assertThat( node2.getValue( "key2" ), is( "value2" ) );
	}

	@Test
	void testToString() {
		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data.toString(), is( "MockNode{}" ) );

		Date birthDate = new Date( 0 );
		data.setValue( "firstName", "Jane" );
		data.setValue( "birthDate", birthDate );
		assertThat( data.toString(), is( "MockNode{birthDate=" + birthDate + ",firstName=Jane}" ) );

		data.setValue( "lastName", "Doe" );
		assertThat( data.toString(), is( "MockNode{birthDate=" + birthDate + ",firstName=Jane,lastName=Doe}" ) );
	}

	@Test
	void testToStringWithSomeValues() {
		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data.toString(), is( "MockNode{}" ) );

		data.setValue( "firstName", "Jane" );
		data.setValue( "lastName", "Doe" );
		assertThat( data.toString( "firstName" ), is( "MockNode{firstName=Jane}" ) );
		assertThat( data.toString( "lastName" ), is( "MockNode{lastName=Doe}" ) );
	}

	@Test
	void testToStringWithAllValues() {
		assertThat( data.toString( true ), is( "MockNode{}" ) );

		data.setValue( "firstName", "Jane" );
		data.setValue( "lastName", "Doe" );
		assertThat( data.toString( true ), is( "MockNode{firstName=Jane,lastName=Doe}" ) );
	}

	@Test
	void testReadOnly() {
		data.setValue( "id", "123456789" );
		data.defineReadOnly( "id" );
		assertThat( data.isReadOnly( "id" ), is( true ) );
		assertThat( data.getValue( "id" ), is( "123456789" ) );

		try {
			data.setValue( "id", "987654321" );
			fail( "Should throw an IllegalStateException" );
		} catch( IllegalStateException exception ) {
			// Intentionally ignore exception
		}
		assertThat( data.getValue( "id" ), is( "123456789" ) );
	}

	@Test
	void testHashCode() {
		String key = UUID.randomUUID().toString();
		String lastName = "Doe";

		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data.hashCode(), is( System.identityHashCode( data ) ) );

		// Test the primary key
		data.setValue( MockNode.MOCK_ID, key );
		assertThat( data.hashCode(), is( key.hashCode() ) );

		// Test the natural key
		data.setValue( "lastName", lastName );
		assertThat( data.hashCode(), is( key.hashCode() ^ lastName.hashCode() ) );
	}

	@Test
	void testEquals() {
		String key = UUID.randomUUID().toString();
		String lastName = "Doe";

		MockNode data1 = new MockNode();
		data1.defineNaturalKey( "firstName", "lastName", "birthDate" );
		MockNode data2 = new MockNode();
		data2.defineNaturalKey( "firstName", "lastName", "birthDate" );
		assertThat( data1.equals( data2 ), is( true ) );

		// Test the primary key
		data1.setValue( MockNode.MOCK_ID, key );
		assertThat( data1.equals( data2 ), is( false ) );

		data2.setValue( MockNode.MOCK_ID, key );
		assertThat( data1.equals( data2 ), is( true ) );

		// Test the natural key
		data1.setValue( "lastName", lastName );
		assertThat( data1.equals( data2 ), is( false ) );

		data2.setValue( "lastName", lastName );
		assertThat( data1.equals( data2 ), is( true ) );
	}

	@SuppressWarnings( "unused" )
	private void listEvents( NodeWatcher watcher ) {
		for( Event event : watcher.getEvents() ) {
			System.out.println( "Event: " + event );
		}
	}

	private static Matcher<Node> hasStates( boolean modified, boolean modifiedBySelf, int modifiedValueCount, int modifiedChildCount ) {
		Matcher<Node> modifiedMatcher = modifiedFlag( is( modified ) );
		Matcher<Node> modifiedBySelfMatcher = modifiedBySelf( is( modifiedBySelf ) );
		Matcher<Node> modifiedValueCountMatcher = modifiedValueCount( is( modifiedValueCount ) );
		Matcher<Node> modifiedChildCountMatcher = modifiedChildCount( is( modifiedChildCount ) );
		return allOf( modifiedMatcher, modifiedBySelfMatcher, modifiedValueCountMatcher, modifiedChildCountMatcher );
	}

	private static Matcher<Node> modifiedFlag( Matcher<? super Boolean> matcher ) {
		return new FeatureMatcher<Node, Boolean>( matcher, "the modified flag", "modified" ) {

			@Override
			protected Boolean featureValueOf( Node node ) {
				return node.isModified();
			}

		};
	}

	private static Matcher<Node> modifiedBySelf( Matcher<? super Boolean> matcher ) {
		return new FeatureMatcher<Node, Boolean>( matcher, "the modified flag", "modified" ) {

			@Override
			protected Boolean featureValueOf( Node node ) {
				return node.isModifiedBySelf();
			}

		};
	}

	private static Matcher<Node> modifiedValueCount( Matcher<? super Integer> matcher ) {
		return new FeatureMatcher<Node, Integer>( matcher, "modified value count", "count" ) {

			@Override
			protected Integer featureValueOf( Node node ) {
				return node.getModifiedValueCount();
			}

		};
	}

	private static Matcher<Node> modifiedChildCount( Matcher<? super Integer> matcher ) {
		return new FeatureMatcher<Node, Integer>( matcher, "modified child count", "count" ) {

			@Override
			protected Integer featureValueOf( Node node ) {
				return node.getModifiedChildCount();
			}

		};
	}

	private static void assertEventState( MockNode target, int index, EventType<? extends Event> type ) {
		assertThat( target.getWatcher().getEvents().get( index ), hasEventState( target, type ) );
	}

	private static void assertEventState( MockNode target, int index, Node item, EventType<? extends Event> type ) {
		assertThat( target.getWatcher().getEvents().get( index ), hasEventState( item, type ) );
	}

	private static void assertEventState(
		MockNode target, int index, EventType<? extends Event> type, String key, Object oldValue, Object newValue
	) {
		assertThat( target.getWatcher().getEvents().get( index ), hasEventState( target, type, null, key, oldValue, newValue ) );
	}

	private static void assertEventState(
		MockNode parent, int index, EventType<? extends NodeEvent> type, Node node, String key, Object oldValue, Object newValue
	) {
		assertThat( parent.getWatcher().getEvents().get( index ), hasEventState( node, type, null, key, oldValue, newValue ) );
	}

	private static void assertEventState(
		MockNode parent, int index, EventType<? extends NodeEvent> type, Node node, String setKey, String key, Object oldValue, Object newValue
	) {
		assertThat( parent.getWatcher().getEvents().get( index ), hasEventState( node, type, setKey, key, oldValue, newValue ) );
	}

	private static Matcher<Event> hasEventState( Node node, EventType<? extends Event> type ) {
		Matcher<Event> eventNode = eventNode( is( node ) );
		Matcher<Event> eventType = eventType( is( type ) );
		return allOf( eventNode, eventType );
	}

	private static Matcher<Event> hasEventState( Node node, EventType<? extends Event> type, String key, Object oldValue, Object newValue ) {
		return hasEventState( node, type, null, key, oldValue, newValue );
	}

	private static Matcher<Event> hasEventState( Node node, EventType<? extends Event> type, String setKey, String key, Object oldValue, Object newValue ) {
		Matcher<Event> eventNode = eventNode( is( node ) );
		Matcher<Event> eventType = eventType( is( type ) );
		Matcher<Event> eventSetKey = eventSetKey( is( setKey ) );
		Matcher<Event> eventKey = eventKey( is( key ) );
		Matcher<Event> eventOldValue = eventOldValue( is( oldValue ) );
		Matcher<Event> eventNewValue = eventNewValue( is( newValue ) );
		return allOf( eventNode, eventType, eventSetKey, eventKey, eventOldValue, eventNewValue );
	}

	private static <T extends Event> Matcher<T> eventNode( Matcher<? super Node> matcher ) {
		return new FeatureMatcher<T, Node>( matcher, "node", "node" ) {

			@Override
			protected Node featureValueOf( T event ) {
				return (Node)event.getSource();
			}

		};
	}

	private static <T extends Event> Matcher<T> eventType( Matcher<? super EventType<T>> matcher ) {
		return new FeatureMatcher<T, EventType<T>>( matcher, "type", "type" ) {

			@Override
			@SuppressWarnings( "unchecked" )
			protected EventType<T> featureValueOf( T event ) {
				return (EventType<T>)event.getEventType();
			}

		};
	}

	private static <T extends Event> Matcher<T> eventSetKey( Matcher<? super String> matcher ) {
		return new FeatureMatcher<T, String>( matcher, "setKey", "setKey" ) {

			@Override
			protected String featureValueOf( T event ) {
				return event instanceof NodeEvent ? ((NodeEvent)event).getSetKey() : null;
			}

		};
	}

	private static <T extends Event> Matcher<T> eventKey( Matcher<? super String> matcher ) {
		return new FeatureMatcher<T, String>( matcher, "key", "key" ) {

			@Override
			protected String featureValueOf( T event ) {
				return event instanceof NodeEvent ? ((NodeEvent)event).getKey() : null;
			}

		};
	}

	private static <T extends Event> Matcher<T> eventOldValue( Matcher<? super Object> matcher ) {
		return new FeatureMatcher<T, Object>( matcher, "oldValue", "oldValue" ) {

			@Override
			protected Object featureValueOf( T event ) {
				return event instanceof NodeEvent ? ((NodeEvent)event).getOldValue() : null;
			}

		};
	}

	private static <T extends Event> Matcher<T> eventNewValue( Matcher<? super Object> matcher ) {
		return new FeatureMatcher<T, Object>( matcher, "newValue", "newValue" ) {

			@Override
			protected Object featureValueOf( T event ) {
				return event instanceof NodeEvent ? ((NodeEvent)event).getNewValue() : null;
			}

		};
	}

}
