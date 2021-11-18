package com.avereon.data;

import com.avereon.transaction.TxnEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NodeTest extends BaseNodeTest {

	@Test
	void testGetAndSetValueEvents() {
		List<NodeEvent> events = new ArrayList<>();
		data.register( "x", events::add );

		int index = 0;
		assertThat( data.<Object> getValue( "x" ) ).isNull();
		assertThat( data.<Object> getValue( "y" ) ).isNull();
		assertThat( data.<Object> getValue( "z" ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "x", 1.0 );
		assertThat( data.<Object> getValue( "x" ) ).isEqualTo( 1.0 );
		assertThat( data.<Object> getValue( "y" ) ).isNull();
		assertThat( data.<Object> getValue( "z" ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( events.get( 0 ) ).hasEventState( data, NodeEvent.VALUE_CHANGED, "x", null, 1.0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "x", null, 1.0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		data.setValue( "x", 0.0 );
		assertThat( data.<Object> getValue( "x" ) ).isEqualTo( 0.0 );
		assertThat( data.<Object> getValue( "y" ) ).isNull();
		assertThat( data.<Object> getValue( "z" ) ).isNull();
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( events.get( 1 ) ).hasEventState( data, NodeEvent.VALUE_CHANGED, "x", 1.0, 0.0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "x", 1.0, 0.0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testRefreshOnChildCausesParentNodeChangedEvent() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setValue( "child", child );
		assertThat( child.<Node> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setModified( false );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.refresh();
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
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

	//	@Test
	//	void testValues() {
	//		assertThat( data.getValues() ).isEqualTo( Set.of() ) );
	//		data.setValue( "0", 0 );
	//		data.setValue( "a", "A" );
	//		data.setValue( "b", "B" );
	//		assertThat( data.getValues(), containsInAnyOrder( 0, "A", "B" ) );
	//		assertTrue( data.isModified() );
	//	}
	//
	//	@Test
	//	void testValuesOfType() {
	//		assertThat( data.getValues( String.class ) ).isEqualTo( Set.of() ) );
	//		data.setValue( "0", 0 );
	//		data.setValue( "a", "A" );
	//		data.setValue( "b", "B" );
	//		assertThat( data.getValues( Integer.class ), containsInAnyOrder( 0 ) );
	//		assertThat( data.getValues( String.class ), containsInAnyOrder( "A", "B" ) );
	//		assertTrue( data.isModified() );
	//	}
	//
	//	@Test
	//	void testNodeSetAddWithNull() {
	//		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() ) );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//
	//		data.addItem( null );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//	}
	//
	//	@Test
	//	void testNodeSetAddRemove() {
	//		MockNode item = new MockNode();
	//		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() ) );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//
	//		data.addItem( item );
	//		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item ) );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 ) );
	//
	//		data.removeItem( item );
	//		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() ) );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//
	//		data.addItem( item );
	//		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item ) );
	//		data.setModified( false );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//
	//		data.removeItem( item );
	//		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() ) );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 ) );
	//	}
	//
	//	@Test
	//	void testNodeSetAddRemoveDataStructure() {
	//		MockNode item = new MockNode();
	//		assertNull( item.getParent() );
	//
	//		data.addItem( item );
	//		assertNotNull( item.getParent() );
	//		assertThat( item.getTrueParent() ).isEqualToA( NodeSet.class ) );
	//		assertThat( item.getParent() ).isEqualTo( data ) );
	//
	//		data.removeItem( item );
	//		assertNull( item.getParent() );
	//	}
	//
	//	@Test
	//	void testNodeSetAddRemoveEvents() {
	//		int index = 0;
	//		int itemIndex = 0;
	//		MockNode item = new MockNode();
	//
	//		data.addItem( item );
	//		NodeSet<?> items = data.getValue( "items" );
	//		assertEventState( item, itemIndex++, NodeEvent.ADDED );
	//		assertThat( item.getEventCount() ).isEqualTo( itemIndex ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.CHILD_ADDED, item.getTrueParent(), "items", item.getCollectionId(), null, item );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, item.getTrueParent(), "items", item.getCollectionId(), null, item );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//
	//		Node set = item.getTrueParent();
	//		data.removeItem( item );
	//		assertEventState( item, itemIndex++, NodeEvent.REMOVED );
	//		assertThat( item.getEventCount() ).isEqualTo( itemIndex ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.CHILD_REMOVED, set, "items", item.getCollectionId(), item, null );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, set, "items", item.getCollectionId(), item, null );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//	}
	//
	//	@Test
	//	void testAddMultipleSetItems() {
	//		MockNode item1 = new MockNode();
	//		MockNode item2 = new MockNode();
	//		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() ) );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//
	//		data.addItem( item1 );
	//		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item1 ) );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 ) );
	//		data.setModified( false );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//
	//		data.addItem( item2 );
	//		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item1, item2 ) );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 ) );
	//		data.setModified( false );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//	}
	//
	//	@Test
	//	void testRemoveFromMultipleSetItems() {
	//		MockNode item1 = new MockNode();
	//		MockNode item2 = new MockNode();
	//		data.addItem( item1 ).addItem( item2 );
	//		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item1, item2 ) );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 ) );
	//		data.setModified( false );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//
	//		data.removeItem( item1 );
	//		assertThat( data.getValues( MockNode.ITEMS ), containsInAnyOrder( item2 ) );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 ) );
	//		data.setModified( false );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//
	//		data.removeItem( item2 );
	//		assertThat( data.getValues( MockNode.ITEMS ) ).isEqualTo( Set.of() ) );
	//		assertNull( data.getValue( MockNode.ITEMS ) );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 0, 1 ) );
	//		data.setModified( false );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//	}
	//
	//	@Test
	//	void testNodeSetIterator() {
	//		MockNode item0 = new MockNode( "0" );
	//		data.addItem( item0 );
	//		MockNode item1 = new MockNode( "1" );
	//		data.addItem( item1 );
	//		MockNode item2 = new MockNode( "2" );
	//		data.addItem( item2 );
	//		MockNode item3 = new MockNode( "3" );
	//		data.addItem( item3 );
	//
	//		assertThat( data.getItems(), containsInAnyOrder( item0, item1, item2, item3 ) );
	//		assertThat( data.getItems().size() ).isEqualTo( 4 ) );
	//	}
	//
	//	@Test
	//	void testNodeSetIteratorWithModifyFilter() {
	//		MockNode item0 = new MockNode( "0" );
	//		data.addItem( item0 );
	//		MockNode item1 = new MockNode( "1" );
	//		data.addItem( item1 );
	//		MockNode item2 = new MockNode( "2" );
	//		data.addItem( item2 );
	//		MockNode item3 = new MockNode( "3" );
	//		data.addItem( item3 );
	//
	//		data.setSetModifyFilter( MockNode.ITEMS, n -> true );
	//
	//		assertThat( data.getItems(), containsInAnyOrder( item0, item1, item2, item3 ) );
	//		assertThat( data.getItems().size() ).isEqualTo( 4 ) );
	//	}
	//
	//	@Test
	//	void testResourceChangesOnChildCausesParentNodeChangedEvent() {
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		int parentIndex = 0;
	//		int childIndex = 0;
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setValue( "child", child );
	//		assertThat( child.getParent() ).isEqualTo( parent ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setModified( false );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		child.setValue( "name", "mock" );
	//		assertThat( child.getValue( "name" ) ).isEqualTo( "mock" ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "name", null, "mock" );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "name", null, "mock" );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		child.setValue( "name", null );
	//		assertThat( child.getValue( "name" ) ).isNull();
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "name", "mock", null );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "name", "mock", null );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//	}
	//
	//	@Test
	//	void testDataEventNotification() {
	//		int index = 0;
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//
	//		// Set a value
	//		data.setValue( "key", "value0" );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", null, "value0" );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//
	//		// Set the value to the same value
	//		data.setValue( "key", "value0" );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//
	//		// Modify the value
	//		data.setValue( "key", "value1" );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", "value0", "value1" );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//
	//		// Remove the attribute.
	//		data.setValue( "key", null );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", "value1", null );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//	}
	//
	//	@Test
	//	void testEventsWithModifiedFlagFalse() {
	//		int index = 0;
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//
	//		// Change a value
	//		data.setValue( "key", "value0" );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", null, "value0" );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//
	//		// Set the modified flag to false
	//		data.setModified( false );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//
	//		// Set the modified flag to false again
	//		data.setModified( false );
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//	}
	//
	//	@Test
	//	void testSetClearSetValueInTransaction() throws Exception {
	//		data.setValue( "x", 1 );
	//		Txn.create();
	//		data.setValue( "x", null );
	//		data.setValue( "x", 1 );
	//		Txn.commit();
	//		assertThat( data.getValue( "x" ) ).isEqualTo( 1 ) );
	//	}
	//
	//	@Test
	//	void testCollapsingEventsWithTransaction() throws Exception {
	//		int index = 0;
	//		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 ) );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//
	//		Txn.create();
	//		data.setValue( "a", "1" );
	//		data.setValue( "a", "2" );
	//		data.setValue( "a", "3" );
	//		data.setValue( "a", "4" );
	//		data.setValue( "a", "5" );
	//		Txn.commit();
	//
	//		assertThat( data.getValue( "a" ) ).isEqualTo( "5" ) );
	//		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 ) );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "a", null, "5" );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( data.getEventCount() ).isEqualTo( index ) );
	//	}
	//
	//	@Test
	//	void testGetParent() {
	//		MockNode parent = new MockNode();
	//		MockNode child = new MockNode();
	//		assertThat( child.getParent() ).isNull();
	//
	//		String key = "key";
	//
	//		parent.setValue( key, child );
	//		assertThat( child.getParent() ).isEqualTo( parent ) );
	//
	//		parent.setValue( key, null );
	//		assertThat( child.getParent() ).isNull();
	//	}
	//
	//	@Test
	//	void testGetNodePath() {
	//		MockNode parent = new MockNode();
	//		MockNode child = new MockNode();
	//
	//		parent.setValue( "child", child );
	//
	//		List<Node> path = parent.getNodePath();
	//		assertThat( path.size() ).isEqualTo( 1 ) );
	//		assertThat( path.get( 0 ) ).isEqualTo( parent ) );
	//
	//		path = child.getNodePath();
	//		assertThat( path.size() ).isEqualTo( 2 ) );
	//		assertThat( path.get( 0 ) ).isEqualTo( parent ) );
	//		assertThat( path.get( 1 ) ).isEqualTo( child ) );
	//	}
	//
	//	@Test
	//	void testParentGetsModifiedAndUnmodifiedWithChildModifyFlag() {
	//		MockNode grandparent = new MockNode( "grandparent" );
	//		MockNode parent = new MockNode( "parent" );
	//		grandparent.setValue( "child", parent );
	//		grandparent.setModified( false );
	//		MockNode child = new MockNode( "child" );
	//		parent.setValue( "child", child );
	//		parent.setModified( false );
	//
	//		assertFalse( grandparent.isModified() );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		assertThat( grandparent, hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//
	//		child.setModified( true );
	//		assertTrue( grandparent.isModified() );
	//		assertTrue( parent.isModified() );
	//		assertTrue( child.isModified() );
	//		assertThat( grandparent, hasStates( true, false, 0, 1 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		NodeAssert.assertThat( child).hasStates( true, true, 0, 0 ) );
	//
	//		parent.setModified( false );
	//		assertFalse( grandparent.isModified() );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		assertThat( grandparent, hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//	}
	//
	//	@Test
	//	void testParentGetsModifiedEventsWhenChildModifiedAndUnmodified() {
	//		// Start with a standard parent/child model
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		parent.setValue( "child", child );
	//		parent.setModified( false );
	//		child.getWatcher().reset();
	//		parent.getWatcher().reset();
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		int index = 0;
	//
	//		// Set an attribute on the child to modify the child and parent
	//		child.setValue( "key", "value0" );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", null, "value0" );
	//		assertEventState( parent, index++, NodeEvent.MODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//
	//		// Change the attribute value on the child
	//		child.setValue( "key", "value1" );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value0", "value1" );
	//		// The parent is already modified so there should not be a modified event here
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//
	//		// Set the child attribute back to null to unmodify the child and parent
	//		child.setValue( "key", null );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value1", null );
	//		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//	}
	//
	//	@Test
	//	void testChildModifiedClearedWhenParentModifiedCleared() {
	//		// Start with a standard parent/child model
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		parent.setValue( "child", child );
	//		parent.setModified( false );
	//		child.getWatcher().reset();
	//		parent.getWatcher().reset();
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		int index = 0;
	//
	//		// Set an attribute on the child to modify the child and parent
	//		child.setValue( "key", "value0" );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", null, "value0" );
	//		assertEventState( parent, index++, NodeEvent.MODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//
	//		// Clear the parent modified flag
	//		parent.setModified( false );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		assertEventState( parent, index++, TxnEvent.COMMIT_BEGIN );
	//
	//		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, TxnEvent.COMMIT_END );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//	}
	//
	//	@Test
	//	void testParentModifiedAndUnmodifiedByChildNodeAttributeChangeWithNullStartValue() {
	//		// Start with a standard parent/child model
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		parent.setValue( "child", child );
	//		parent.setModified( false );
	//		child.getWatcher().reset();
	//		parent.getWatcher().reset();
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		int index = 0;
	//
	//		// Test setting a value on the child node modifies the parent
	//		child.setValue( "key", "value" );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", null, "value" );
	//		assertEventState( parent, index++, NodeEvent.MODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//
	//		// Test clear the value on the child node unmodifies the parent
	//		child.setValue( "key", null );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value", null );
	//		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//	}
	//
	//	@Test
	//	void testParentModifiedAndUnmodifiedByChildNodeAttributeChangeWithNonNullStartValue() {
	//		// Start with a standard parent/child model
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		parent.setValue( "child", child );
	//		parent.setModified( false );
	//		child.getWatcher().reset();
	//		parent.getWatcher().reset();
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		int index = 0;
	//
	//		// Set an attribute on the child to a non-null value
	//		child.setValue( "key", "value0" );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", null, "value0" );
	//		assertEventState( parent, index++, NodeEvent.MODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//
	//		// Clear the modified flags
	//		parent.setModified( false );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		assertEventState( parent, index++, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, TxnEvent.COMMIT_END );
	//
	//		// Change the attribute value on the child
	//		child.setValue( "key", "value1" );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value0", "value1" );
	//		assertEventState( parent, index++, NodeEvent.MODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//
	//		// Set the child attribute to the same value, should do nothing
	//		child.setValue( "key", "value1" );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//
	//		// Set the child attribute back to value0
	//		child.setValue( "key", "value0" );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent, index++, NodeEvent.VALUE_CHANGED, child, "key", "value1", "value0" );
	//		assertEventState( parent, index++, NodeEvent.UNMODIFIED );
	//		assertEventState( parent, index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent, index++, child, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( index ) );
	//	}
	//
	//	@Test
	//	void testGrandparentModifiedByChildNodeAttributeChange() {
	//		int parentIndex = 0;
	//		int childIndex = 0;
	//		int grandChildIndex = 0;
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		MockNode grandChild = new MockNode( "grandChild" );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex ) );
	//
	//		parent.setValue( "child", child );
	//		assertThat( child.getParent() ).isEqualTo( parent ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
	//
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
	//		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex ) );
	//
	//		child.setValue( "child", grandChild );
	//		assertThat( grandChild.getParent() ).isEqualTo( child ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 1, 1 ) );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
	//
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, child, "child", null, grandChild );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "child", null, grandChild );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, grandChild );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, grandChild );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		assertEventState( grandChild, grandChildIndex++, NodeEvent.ADDED );
	//		//assertEventState( grandChild, grandChildIndex++, parent, NodeEvent.PARENT_CHANGED );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex ) );
	//
	//		parent.setModified( false );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
	//
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex ) );
	//
	//		// Test setting a value on the child node modifies the parents
	//		grandChild.setValue( "key", "value" );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		NodeAssert.assertThat( child).hasStates( true, false, 0, 1 ) );
	//		assertThat( grandChild, hasStates( true, false, 1, 0 ) );
	//
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, grandChild, "key", null, "value" );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED, parent, null, null, null );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, grandChild, "key", null, "value" );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.MODIFIED, child, null, null, null );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );
	//
	//		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( grandChild, grandChildIndex++, NodeEvent.VALUE_CHANGED, "key", null, "value" );
	//		assertEventState( grandChild, grandChildIndex++, NodeEvent.MODIFIED );
	//		assertEventState( grandChild, grandChildIndex++, NodeEvent.NODE_CHANGED );
	//		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex ) );
	//
	//		// Test unsetting the value on the child node unmodifies the parents
	//		grandChild.setValue( "key", null );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, grandChild, "key", "value", null );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, grandChild, "key", "value", null );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );
	//
	//		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( grandChild, grandChildIndex++, NodeEvent.VALUE_CHANGED, "key", "value", null );
	//		assertEventState( grandChild, grandChildIndex++, NodeEvent.UNMODIFIED );
	//		assertEventState( grandChild, grandChildIndex++, NodeEvent.NODE_CHANGED );
	//		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( grandChild, grandChildIndex++, TxnEvent.COMMIT_END );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex ) );
	//	}
	//
	//	@Test
	//	void testParentModifiedByChildNodeClearedByFlag() {
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		int parentIndex = 0;
	//		int childIndex = 0;
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setValue( "child", child );
	//		assertThat( child.getParent() ).isEqualTo( parent ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setModified( false );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Test setting the 'a' value on the child modifies the parent
	//		child.setValue( "a", "1" );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "a", null, "1" );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "a", null, "1" );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Test setting the 'b' value on the child leaves the parent modified
	//		child.setValue( "b", "1" );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		NodeAssert.assertThat( child).hasStates( true, false, 2, 0 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "b", null, "1" );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "b", null, "1" );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Set this state as the new unmodified state
	//		child.setModified( false );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//	}
	//
	//	@Test
	//	void testParentModifiedByChildNodeClearedByValue() {
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		int parentIndex = 0;
	//		int childIndex = 0;
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setValue( "child", child );
	//		assertThat( child.getParent() ).isEqualTo( parent ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
	//		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setModified( false );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Test setting the 'a' value on the child modifies the parent
	//		child.setValue( "a", "2" );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "a", null, "2" );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "a", null, "2" );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Test setting the 'b' value on the child leaves the parent modified
	//		child.setValue( "b", "2" );
	//		NodeAssert.assertThat( child).hasStates( true, false, 2, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "b", null, "2" );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "b", null, "2" );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Test unsetting the 'a' value on the child leaves the parent modified
	//		child.setValue( "a", null );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "a", "2", null );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "a", "2", null );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Test unsetting the value 'b' on the child returns the parent to unmodified
	//		child.setValue( "b", null );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "b", "2", null );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "b", "2", null );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//	}
	//
	//	@Test
	//	void testChildModifiedClearedByParentSetModifiedFalse() {
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		int parentIndex = 0;
	//		int childIndex = 0;
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setValue( "child", child );
	//		assertThat( child.getParent() ).isEqualTo( parent ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setModified( false );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Test setting the 'a' value on the child modifies the parent
	//		child.setValue( "x", "2" );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 0, 1 ) );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, child, "x", null, "2" );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "x", null, "2" );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.MODIFIED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setModified( false );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//	}
	//
	//	@Test
	//	void testAddNodeAttributeToDifferentParent() {
	//		MockNode parent0 = new MockNode( "parent0" );
	//		MockNode parent1 = new MockNode( "parent1" );
	//		MockNode child = new MockNode( "child" );
	//		int parent0Index = 0;
	//		int parent1Index = 0;
	//		int childIndex = 0;
	//		assertThat( parent0, hasStates( false, false, 0, 0 ) );
	//		assertThat( parent1, hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index ) );
	//		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Add the child attribute to parent 0
	//		parent0.setValue( "child", child );
	//		assertThat( parent0, hasStates( true, false, 1, 0 ) );
	//		assertThat( parent1, hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
	//		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent0, NodeEvent.PARENT_CHANGED );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent0, parent0Index++, NodeEvent.CHILD_ADDED, "child", null, child );
	//		assertEventState( parent0, parent0Index++, NodeEvent.VALUE_CHANGED, "child", null, child );
	//		assertEventState( parent0, parent0Index++, NodeEvent.MODIFIED );
	//		assertEventState( parent0, parent0Index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_END );
	//		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index ) );
	//		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index ) );
	//
	//		// Clear the modified flag of parent 0.
	//		parent0.setModified( false );
	//		assertThat( child.getParent() ).isEqualTo( parent0 ) );
	//		assertThat( parent0, hasStates( false, false, 0, 0 ) );
	//		assertThat( parent1, hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent0, parent0Index++, NodeEvent.UNMODIFIED );
	//		assertEventState( parent0, parent0Index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_END );
	//		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index ) );
	//		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		// Add the child attribute to parent 1.
	//		parent1.setValue( "child", child );
	//		assertThat( child.getParent() ).isEqualTo( parent1 ) );
	//		assertThat( parent1.getValue( "child" ) ).isEqualTo( child ) );
	//		assertThat( parent0.getValue( "child" ) ).isNull();
	//		assertThat( parent0, hasStates( true, false, 1, 0 ) );
	//		assertThat( parent1, hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.REMOVED );
	//		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent0, parent0Index++, NodeEvent.CHILD_REMOVED, "child", child, null );
	//		assertEventState( parent0, parent0Index++, NodeEvent.VALUE_CHANGED, "child", child, null );
	//		assertEventState( parent0, parent0Index++, NodeEvent.MODIFIED );
	//		assertEventState( parent0, parent0Index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent0, parent0Index++, TxnEvent.COMMIT_END );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
	//		assertEventState( parent1, parent1Index++, TxnEvent.COMMIT_BEGIN );
	//		assertEventState( parent1, parent1Index++, NodeEvent.CHILD_ADDED, "child", null, child );
	//		assertEventState( parent1, parent1Index++, NodeEvent.VALUE_CHANGED, "child", null, child );
	//		assertEventState( parent1, parent1Index++, NodeEvent.MODIFIED );
	//		assertEventState( parent1, parent1Index++, NodeEvent.NODE_CHANGED );
	//		assertEventState( parent1, parent1Index++, TxnEvent.COMMIT_SUCCESS );
	//		assertEventState( parent1, parent1Index++, TxnEvent.COMMIT_END );
	//		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent1, NodeEvent.PARENT_CHANGED );
	//		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index ) );
	//		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//	}
	//
	//	@Test
	//	void testChildReceivesParentChangedEvent() {
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		int parentIndex = 0;
	//		int childIndex = 0;
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		parent.setValue( "child", child );
	//		assertThat( child.getParent() ).isEqualTo( parent ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex += 1 ) );
	//
	//		parent.addModifyingKeys( "x" );
	//		parent.setValue( "x", 1 );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//	}
	//
	//	@Test
	//	void testGrandchildReceivesParentChangedEvent() {
	//		int parentIndex = 0;
	//		int childIndex = 0;
	//		int grandChildIndex = 0;
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		MockNode grandChild = new MockNode( "grandChild" );
	//		NodeAssert.assertThat( parent).hasStates( false, false, 0, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex ) );
	//
	//		parent.setValue( "child", child );
	//		assertThat( child.getParent() ).isEqualTo( parent ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 1, 0 ) );
	//		NodeAssert.assertThat( child).hasStates( false, false, 0, 0 ) );
	//		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex += 1 ) );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex ) );
	//
	//		child.setValue( "child", grandChild );
	//		assertThat( grandChild.getParent() ).isEqualTo( child ) );
	//		NodeAssert.assertThat( parent).hasStates( true, false, 1, 1 ) );
	//		NodeAssert.assertThat( child).hasStates( true, false, 1, 0 ) );
	//		assertThat( grandChild, hasStates( false, false, 0, 0 ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex += 7 ) );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex += 1 ) );
	//
	//		parent.addModifyingKeys( "x" );
	//		parent.setValue( "x", 1 );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		assertEventState( grandChild, grandChildIndex++, parent, NodeEvent.PARENT_CHANGED );
	//		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex ) );
	//	}
	//
	//	@Test
	//	void testParentNodeNotModifiedByNodeAddedWithSetWithModifyFilter() {
	//		int parentIndex = 12;
	//		int childIndex = 1;
	//
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		parent.setValue( "child", child );
	//		parent.setModified( false );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
	//		assertFalse( child.isModified() );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		MockNode item0 = new MockNode( "0" );
	//		item0.setValue( "dont-modify", true );
	//		assertFalse( item0.isModified() );
	//		// No new events should have occurred
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		child.removeItem( item0 );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModified() );
	//		child.removeItem( item0 );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModified() );
	//		// No new events should have occurred
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		child.addItem( item0 );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModified() );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_ADDED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_ADDED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		child.addItem( item0 );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModified() );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//
	//		child.removeItem( item0 );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModified() );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_REMOVED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_REMOVED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
	//		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//		child.removeItem( item0 );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModified() );
	//		assertThat( parent.getEventCount() ).isEqualTo( parentIndex ) );
	//		assertThat( child.getEventCount() ).isEqualTo( childIndex ) );
	//	}
	//
	//	@Test
	//	void testNodeNotModifiedByChildAddUntilNodeFilterValueChange() {
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		parent.setValue( "child", child );
	//		parent.setModified( false );
	//		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//
	//		MockNode item0 = new MockNode( "A" );
	//
	//		// Make sure the modified flag is working as expected before using the dont-modify value
	//		child.addItem( item0 );
	//		assertTrue( parent.isModified() );
	//		assertTrue( child.isModified() );
	//		assertFalse( item0.isModified() );
	//		child.removeItem( item0 );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( item0.isModified() );
	//
	//		// The dont-modify is not a modifying key
	//		item0.setValue( "dont-modify", true );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( item0.isModified() );
	//
	//		// Adding the child should not cause any node to be modified
	//		child.addItem( item0 );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( item0.isModified() );
	//
	//		// Some extra checking
	//		child.removeItem( item0 );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( item0.isModified() );
	//		child.addItem( item0 );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( item0.isModified() );
	//	}
	//
	//	@Test
	//	void testNodeNotModifiedByChildRemoveUntilNodeFilterValueChange() {
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		parent.setValue( "child", child );
	//		parent.setModified( false );
	//		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//
	//		MockNode item0 = new MockNode( "A" );
	//
	//		// Make sure the modified flag is working as expected before using the dont-modify value
	//		child.addItem( item0 );
	//		assertTrue( parent.isModified() );
	//		assertTrue( child.isModified() );
	//		assertFalse( item0.isModified() );
	//		child.removeItem( item0 );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( item0.isModified() );
	//
	//		item0.setValue( "dont-modify", true );
	//		child.addItem( item0 );
	//		assertFalse( parent.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( item0.isModified() );
	//
	//		// This does not modify the node because dont-modify is not a modifying key
	//		item0.setValue( "dont-modify", null );
	//		child.removeItem( item0 );
	//		assertTrue( parent.isModified() );
	//		assertTrue( child.isModified() );
	//		assertFalse( item0.isModified() );
	//	}
	//
	//	@Test
	//	void testParentNodeNotModifiedByNodeSetWithModifyFilter() {
	//		MockNode parent = new MockNode( "parent" );
	//		MockNode child = new MockNode( "child" );
	//		parent.setValue( "child", child );
	//		parent.setModified( false );
	//
	//		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
	//		assertFalse( child.isModified() );
	//
	//		MockNode item0 = new MockNode( "0" );
	//		child.addItem( item0 );
	//		assertFalse( item0.isModified() );
	//		assertTrue( child.isModified() );
	//		assertTrue( parent.isModified() );
	//		child.setModified( false );
	//
	//		assertFalse( item0.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModifiedByValue() );
	//		assertFalse( parent.isModifiedByChild() );
	//		assertFalse( parent.isModified() );
	//
	//		item0.setValue( "dont-modify", true );
	//		assertFalse( item0.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModified() );
	//
	//		item0.setValue( "a", "A" );
	//		assertTrue( item0.isModified() );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModified() );
	//
	//		item0.setValue( "dont-modify", null );
	//		assertFalse( child.isModified() );
	//		assertFalse( parent.isModified() );
	//		child.removeItem( item0 );
	//		assertTrue( child.isModified() );
	//		assertTrue( parent.isModified() );
	//		child.setModified( false );
	//	}
	//
	//	@Test
	//	void testParentModifiedByAddingModifiedChild() {
	//		MockNode child = new MockNode( "child" );
	//		child.setModified( true );
	//		assertFalse( data.isModified() );
	//		assertTrue( child.isModified() );
	//
	//		data.addItem( new MockNode().addItem( new MockNode().addItem( child ) ) );
	//		assertTrue( data.isModified() );
	//		assertTrue( child.isModified() );
	//
	//		data.setModified( false );
	//		assertFalse( data.isModified() );
	//		assertFalse( child.isModified() );
	//	}
	//
	//	@Test
	//	void testAddDataListener() {
	//		// Remove the default watcher
	//		data.unregister( Event.ANY, data.getWatcher() );
	//
	//		EventHandler<NodeEvent> listener = e -> {};
	//		data.register( NodeEvent.ANY, listener );
	//
	//		Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> handlers = data.getEventHandlers();
	//
	//		assertNotNull( handlers );
	//		assertThat( handlers.size() ).isEqualTo( 1 ) );
	//		assertThat( handlers.get( NodeEvent.ANY ), contains( listener ) );
	//	}
	//
	//	@Test
	//	void testRemoveDataListener() {
	//		// Remove the default watcher
	//		data.unregister( Event.ANY, data.getWatcher() );
	//
	//		Map<EventType<? extends Event>, Collection<? extends EventHandler<? extends Event>>> handlers;
	//		EventHandler<NodeEvent> listener = e -> {};
	//
	//		data.register( NodeEvent.ANY, listener );
	//		handlers = data.getEventHandlers();
	//		assertNotNull( handlers );
	//		assertThat( handlers.size() ).isEqualTo( 1 ) );
	//		assertThat( handlers.get( NodeEvent.ANY ), contains( listener ) );
	//
	//		data.unregister( NodeEvent.ANY, listener );
	//		handlers = data.getEventHandlers();
	//		assertNotNull( handlers );
	//		assertThat( handlers.size() ).isEqualTo( 0 ) );
	//		assertThat( handlers.get( NodeEvent.ANY ), not( contains( listener ) ) );
	//
	//		data.register( NodeEvent.ANY, listener );
	//		handlers = data.getEventHandlers();
	//		assertNotNull( handlers );
	//		assertThat( handlers.size() ).isEqualTo( 1 ) );
	//		assertThat( handlers.get( NodeEvent.ANY ), contains( listener ) );
	//
	//		data.unregister( NodeEvent.ANY, listener );
	//		handlers = data.getEventHandlers();
	//		assertNotNull( handlers );
	//		assertThat( handlers.size() ).isEqualTo( 0 ) );
	//		assertThat( handlers.get( NodeEvent.ANY ), not( contains( listener ) ) );
	//	}
	//
	//	@Test
	//	void testCircularReferenceCheck() {
	//		MockNode node = new MockNode();
	//		try {
	//			node.setValue( "node", node );
	//			fail( "CircularReferenceException should be thrown" );
	//		} catch( CircularReferenceException exception ) {
	//			// Intentionally ignore exception.
	//			assertThat( exception.getMessage(), startsWith( "Circular reference detected" ) );
	//		}
	//	}
	//
	//	@Test
	//	void testCopyFrom() {
	//		Node node1 = new Node();
	//		node1.setValue( "key1", "value1" );
	//		node1.setValue( "key2", "value2" );
	//
	//		Node node2 = new Node();
	//		node2.setValue( "key2", "valueB" );
	//
	//		node2.copyFrom( node1 );
	//		assertThat( node1.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node1.getValue( "key2" ) ).isEqualTo( "value2" ) );
	//		assertThat( node2.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node2.getValue( "key2" ) ).isEqualTo( "valueB" ) );
	//	}
	//
	//	@Test
	//	void testCopyFromWithOverwrite() {
	//		Node node1 = new Node();
	//		node1.setValue( "key1", "value1" );
	//		node1.setValue( "key2", "value2" );
	//
	//		Node node2 = new Node();
	//		node2.setValue( "key2", "valueB" );
	//
	//		node2.copyFrom( node1, true );
	//		assertThat( node1.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node1.getValue( "key2" ) ).isEqualTo( "value2" ) );
	//		assertThat( node2.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node2.getValue( "key2" ) ).isEqualTo( "value2" ) );
	//	}
	//
	//	@Test
	//	void testCopyFromWithOverwriteAndPrimaryKey() {
	//		Node node1 = new MockNode();
	//		node1.setValue( MockNode.MOCK_ID, "a" );
	//		node1.setValue( "key1", "value1" );
	//		node1.setValue( "key2", "value2" );
	//
	//		Node node2 = new MockNode();
	//		node2.setValue( MockNode.MOCK_ID, "b" );
	//		node2.setValue( "key2", "valueB" );
	//
	//		node2.copyFrom( node1, true );
	//		assertThat( node1.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node1.getValue( "key2" ) ).isEqualTo( "value2" ) );
	//		assertThat( node1.getValue( MockNode.MOCK_ID ) ).isEqualTo( "a" ) );
	//		assertThat( node2.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node2.getValue( "key2" ) ).isEqualTo( "value2" ) );
	//		assertThat( node2.getValue( MockNode.MOCK_ID ) ).isEqualTo( "b" ) );
	//	}
	//
	//	@Test
	//	void testCopyFromUsingResources() {
	//		Node node1 = new Node();
	//		node1.setValue( "key1", "value1" );
	//		node1.setValue( "key2", "value2" );
	//
	//		Node node2 = new Node();
	//		node2.setValue( "key2", "valueB" );
	//
	//		node2.copyFrom( node1 );
	//		assertThat( node1.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node1.getValue( "key2" ) ).isEqualTo( "value2" ) );
	//		assertThat( node2.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node2.getValue( "key2" ) ).isEqualTo( "valueB" ) );
	//	}
	//
	//	@Test
	//	void testCopyFromWithOverwriteUsingResources() {
	//		Node node1 = new Node();
	//		node1.setValue( "key1", "value1" );
	//		node1.setValue( "key2", "value2" );
	//
	//		Node node2 = new Node();
	//		node2.setValue( "key2", "valueB" );
	//
	//		node2.copyFrom( node1, true );
	//		assertThat( node1.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node1.getValue( "key2" ) ).isEqualTo( "value2" ) );
	//		assertThat( node2.getValue( "key1" ) ).isEqualTo( "value1" ) );
	//		assertThat( node2.getValue( "key2" ) ).isEqualTo( "value2" ) );
	//	}
	//
	//	@Test
	//	void testToString() {
	//		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
	//		assertThat( data.toString() ).isEqualTo( "MockNode{}" ) );
	//
	//		Date birthDate = new Date( 0 );
	//		data.setValue( "firstName", "Jane" );
	//		data.setValue( "birthDate", birthDate );
	//		assertThat( data.toString() ).isEqualTo( "MockNode{birthDate=" + birthDate + ",firstName=Jane}" ) );
	//
	//		data.setValue( "lastName", "Doe" );
	//		assertThat( data.toString() ).isEqualTo( "MockNode{birthDate=" + birthDate + ",firstName=Jane,lastName=Doe}" ) );
	//	}
	//
	//	@Test
	//	void testToStringWithSomeValues() {
	//		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
	//		assertThat( data.toString() ).isEqualTo( "MockNode{}" ) );
	//
	//		data.setValue( "firstName", "Jane" );
	//		data.setValue( "lastName", "Doe" );
	//		assertThat( data.toString( "firstName" ) ).isEqualTo( "MockNode{firstName=Jane}" ) );
	//		assertThat( data.toString( "lastName" ) ).isEqualTo( "MockNode{lastName=Doe}" ) );
	//	}
	//
	//	@Test
	//	void testToStringWithAllValues() {
	//		assertThat( data.toString( true ) ).isEqualTo( "MockNode{}" ) );
	//
	//		data.setValue( "firstName", "Jane" );
	//		data.setValue( "lastName", "Doe" );
	//		assertThat( data.toString( true ) ).isEqualTo( "MockNode{firstName=Jane,lastName=Doe}" ) );
	//	}
	//
	//	@Test
	//	void testReadOnly() {
	//		data.setValue( "id", "123456789" );
	//		data.defineReadOnly( "id" );
	//		assertThat( data.isReadOnly( "id" ) ).isEqualTo( true ) );
	//		assertThat( data.getValue( "id" ) ).isEqualTo( "123456789" ) );
	//
	//		try {
	//			data.setValue( "id", "987654321" );
	//			fail( "Should throw an IllegalStateException" );
	//		} catch( IllegalStateException exception ) {
	//			// Intentionally ignore exception
	//		}
	//		assertThat( data.getValue( "id" ) ).isEqualTo( "123456789" ) );
	//	}
	//
	//	@Test
	//	void testHashCode() {
	//		String key = UUID.randomUUID().toString();
	//		String lastName = "Doe";
	//
	//		data.defineNaturalKey( "firstName", "lastName", "birthDate" );
	//		assertThat( data.hashCode() ).isEqualTo( System.identityHashCode( data ) ) );
	//
	//		// Test the primary key
	//		data.setValue( MockNode.MOCK_ID, key );
	//		assertThat( data.hashCode() ).isEqualTo( key.hashCode() ) );
	//
	//		// Test the natural key
	//		data.setValue( "lastName", lastName );
	//		assertThat( data.hashCode() ).isEqualTo( key.hashCode() ^ lastName.hashCode() ) );
	//	}
	//
	//	@Test
	//	void testEquals() {
	//		String key = UUID.randomUUID().toString();
	//		String lastName = "Doe";
	//
	//		MockNode data1 = new MockNode();
	//		data1.defineNaturalKey( "firstName", "lastName", "birthDate" );
	//		MockNode data2 = new MockNode();
	//		data2.defineNaturalKey( "firstName", "lastName", "birthDate" );
	//		assertThat( data1.equals( data2 ) ).isEqualTo( true ) );
	//
	//		// Test the primary key
	//		data1.setValue( MockNode.MOCK_ID, key );
	//		assertThat( data1.equals( data2 ) ).isEqualTo( false ) );
	//
	//		data2.setValue( MockNode.MOCK_ID, key );
	//		assertThat( data1.equals( data2 ) ).isEqualTo( true ) );
	//
	//		// Test the natural key
	//		data1.setValue( "lastName", lastName );
	//		assertThat( data1.equals( data2 ) ).isEqualTo( false ) );
	//
	//		data2.setValue( "lastName", lastName );
	//		assertThat( data1.equals( data2 ) ).isEqualTo( true ) );
	//	}
	//
	//	@SuppressWarnings( "unused" )
	//	private void listEvents( NodeWatcher watcher ) {
	//		for( Event event : watcher.getEvents() ) {
	//			System.out.println( "Event: " + event );
	//		}
	//	}
	//
	//	@Deprecated
	//	private static Matcher<Node> hasStates( boolean modified, boolean modifiedBySelf, int modifiedValueCount, int modifiedChildCount ) {
	//		Matcher<Node> modifiedMatcher = modifiedFlag( is( modified ) );
	//		Matcher<Node> modifiedBySelfMatcher = modifiedBySelf( is( modifiedBySelf ) );
	//		Matcher<Node> modifiedValueCountMatcher = modifiedValueCount( is( modifiedValueCount ) );
	//		Matcher<Node> modifiedChildCountMatcher = modifiedChildCount( is( modifiedChildCount ) );
	//		return allOf( modifiedMatcher, modifiedBySelfMatcher, modifiedValueCountMatcher, modifiedChildCountMatcher );
	//	}
	//
	//	@Deprecated
	//	private static Matcher<Node> modifiedFlag( Matcher<? super Boolean> matcher ) {
	//		return new FeatureMatcher<Node, Boolean>( matcher, "the modified flag", "modified" ) {
	//
	//			@Override
	//			protected Boolean featureValueOf( Node node ) {
	//				return node.isModified();
	//			}
	//
	//		};
	//	}
	//
	//	@Deprecated
	//	private static Matcher<Node> modifiedBySelf( Matcher<? super Boolean> matcher ) {
	//		return new FeatureMatcher<Node, Boolean>( matcher, "the modified flag", "modified" ) {
	//
	//			@Override
	//			protected Boolean featureValueOf( Node node ) {
	//				return node.isModifiedBySelf();
	//			}
	//
	//		};
	//	}
	//
	//	@Deprecated
	//	private static Matcher<Node> modifiedValueCount( Matcher<? super Integer> matcher ) {
	//		return new FeatureMatcher<Node, Integer>( matcher, "modified value count", "count" ) {
	//
	//			@Override
	//			protected Integer featureValueOf( Node node ) {
	//				return node.getModifiedValueCount();
	//			}
	//
	//		};
	//	}
	//
	//	@Deprecated
	//	private static Matcher<Node> modifiedChildCount( Matcher<? super Integer> matcher ) {
	//		return new FeatureMatcher<Node, Integer>( matcher, "modified child count", "count" ) {
	//
	//			@Override
	//			protected Integer featureValueOf( Node node ) {
	//				return node.getModifiedChildCount();
	//			}
	//
	//		};
	//	}
	//
	//	@Deprecated
	//	private static void assertEventState( MockNode target, int index, EventType<? extends Event> type ) {
	//		assertThat( target.getWatcher().getEvents().get( index ), hasEventState( target, type ) );
	//	}
	//
	//	@Deprecated
	//	private static void assertEventState( MockNode target, int index, Node item, EventType<? extends Event> type ) {
	//		assertThat( target.getWatcher().getEvents().get( index ), hasEventState( item, type ) );
	//	}
	//
	//	@Deprecated
	//	private static void assertEventState(
	//		MockNode target, int index, EventType<? extends Event> type, String key, Object oldValue, Object newValue
	//	) {
	//		assertThat( target.getWatcher().getEvents().get( index ), hasEventState( target, type, null, key, oldValue, newValue ) );
	//	}
	//
	//	@Deprecated
	//	private static void assertEventState(
	//		MockNode parent, int index, EventType<? extends NodeEvent> type, Node node, String key, Object oldValue, Object newValue
	//	) {
	//		assertThat( parent.getWatcher().getEvents().get( index ), hasEventState( node, type, null, key, oldValue, newValue ) );
	//	}
	//
	//	@Deprecated
	//	private static void assertEventState(
	//		MockNode parent, int index, EventType<? extends NodeEvent> type, Node node, String setKey, String key, Object oldValue, Object newValue
	//	) {
	//		assertThat( parent.getWatcher().getEvents().get( index ), hasEventState( node, type, setKey, key, oldValue, newValue ) );
	//	}
	//
	//	@Deprecated
	//	private static Matcher<Event> hasEventState( Node node, EventType<? extends Event> type ) {
	//		Matcher<Event> eventNode = eventNode( is( node ) );
	//		Matcher<Event> eventType = eventType( is( type ) );
	//		return allOf( eventNode, eventType );
	//	}
	//
	//	@Deprecated
	//	private static Matcher<Event> hasEventState( Node node, EventType<? extends Event> type, String key, Object oldValue, Object newValue ) {
	//		return hasEventState( node, type, null, key, oldValue, newValue );
	//	}
	//
	//	@Deprecated
	//	private static Matcher<Event> hasEventState( Node node, EventType<? extends Event> type, String setKey, String key, Object oldValue, Object newValue ) {
	//		Matcher<Event> eventNode = eventNode( is( node ) );
	//		Matcher<Event> eventType = eventType( is( type ) );
	//		Matcher<Event> eventSetKey = eventSetKey( is( setKey ) );
	//		Matcher<Event> eventKey = eventKey( is( key ) );
	//		Matcher<Event> eventOldValue = eventOldValue( is( oldValue ) );
	//		Matcher<Event> eventNewValue = eventNewValue( is( newValue ) );
	//		return allOf( eventNode, eventType, eventSetKey, eventKey, eventOldValue, eventNewValue );
	//	}
	//
	//	@Deprecated
	//	private static <T extends Event> Matcher<T> eventNode( Matcher<? super Node> matcher ) {
	//		return new FeatureMatcher<T, Node>( matcher, "node", "node" ) {
	//
	//			@Override
	//			protected Node featureValueOf( T event ) {
	//				return (Node)event.getSource();
	//			}
	//
	//		};
	//	}
	//
	//	@Deprecated
	//	private static <T extends Event> Matcher<T> eventType( Matcher<? super EventType<T>> matcher ) {
	//		return new FeatureMatcher<T, EventType<T>>( matcher, "type", "type" ) {
	//
	//			@Override
	//			@SuppressWarnings( "unchecked" )
	//			protected EventType<T> featureValueOf( T event ) {
	//				return (EventType<T>)event.getEventType();
	//			}
	//
	//		};
	//	}
	//
	//	@Deprecated
	//	private static <T extends Event> Matcher<T> eventSetKey( Matcher<? super String> matcher ) {
	//		return new FeatureMatcher<T, String>( matcher, "setKey", "setKey" ) {
	//
	//			@Override
	//			protected String featureValueOf( T event ) {
	//				return event instanceof NodeEvent ? ((NodeEvent)event).getSetKey() : null;
	//			}
	//
	//		};
	//	}
	//
	//	@Deprecated
	//	private static <T extends Event> Matcher<T> eventKey( Matcher<? super String> matcher ) {
	//		return new FeatureMatcher<T, String>( matcher, "key", "key" ) {
	//
	//			@Override
	//			protected String featureValueOf( T event ) {
	//				return event instanceof NodeEvent ? ((NodeEvent)event).getKey() : null;
	//			}
	//
	//		};
	//	}
	//
	//	@Deprecated
	//	private static <T extends Event> Matcher<T> eventOldValue( Matcher<? super Object> matcher ) {
	//		return new FeatureMatcher<T, Object>( matcher, "oldValue", "oldValue" ) {
	//
	//			@Override
	//			protected Object featureValueOf( T event ) {
	//				return event instanceof NodeEvent ? ((NodeEvent)event).getOldValue() : null;
	//			}
	//
	//		};
	//	}
	//
	//	@Deprecated
	//	private static <T extends Event> Matcher<T> eventNewValue( Matcher<? super Object> matcher ) {
	//		return new FeatureMatcher<T, Object>( matcher, "newValue", "newValue" ) {
	//
	//			@Override
	//			protected Object featureValueOf( T event ) {
	//				return event instanceof NodeEvent ? ((NodeEvent)event).getNewValue() : null;
	//			}
	//
	//		};
	//	}

}
