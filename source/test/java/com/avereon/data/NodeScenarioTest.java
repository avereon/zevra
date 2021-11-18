package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NodeScenarioTest extends BaseNodeTest {

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
	void testNodeSetAddRemoveEvents() {
		int index = 0;
		int itemIndex = 0;
		MockNode item = new MockNode();

		data.addItem( item );
		NodeSet<?> items = data.getValue( "items" );
		NodeEventAssert.assertThat( item.event( itemIndex++ ) ).hasEventState( NodeEvent.ADDED );
		assertThat( item.getEventCount() ).isEqualTo( itemIndex );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( item.getTrueParent(), NodeEvent.CHILD_ADDED, "items", item.getCollectionId(), null, item );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( item.getTrueParent(), NodeEvent.VALUE_CHANGED, "items", item.getCollectionId(), null, item );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		Node set = item.getTrueParent();
		data.removeItem( item );
		NodeEventAssert.assertThat( item.event( itemIndex++ ) ).hasEventState( NodeEvent.REMOVED );
		assertThat( item.getEventCount() ).isEqualTo( itemIndex );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( set, NodeEvent.CHILD_REMOVED, "items", item.getCollectionId(), item, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( set, NodeEvent.VALUE_CHANGED, "items", item.getCollectionId(), item, null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( items, TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testResourceChangesOnChildCausesParentNodeChangedEvent() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setValue( "child", child );
		assertThat( child.<MockNode> getParent() ).isEqualTo( parent );
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

		child.setValue( "name", "mock" );
		assertThat( child.<String> getValue( "name" ) ).isEqualTo( "mock" );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "name", null, "mock" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "name", null, "mock" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.setValue( "name", null );
		assertThat( child.<Object> getValue( "name" ) ).isNull();
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "name", "mock", null );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "name", "mock", null );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testDataEventNotification() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Set a value
		data.setValue( "key", "value0" );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", null, "value0" );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Set the value to the same value
		data.setValue( "key", "value0" );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Modify the value
		data.setValue( "key", "value1" );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", "value0", "value1" );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Remove the attribute.
		data.setValue( "key", null );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", "value1", null );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testEventsWithModifiedFlagFalse() {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Change a value
		data.setValue( "key", "value0" );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "key", null, "value0" );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Set the modified flag to false
		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );

		// Set the modified flag to false again
		data.setModified( false );
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testSetClearSetValueInTransaction() throws Exception {
		data.setValue( "x", 1 );
		Txn.create();
		data.setValue( "x", null );
		data.setValue( "x", 1 );
		Txn.commit();
		assertThat( data.<Integer> getValue( "x" ) ).isEqualTo( 1 );
	}

	@Test
	void testCollapsingEventsWithTransaction() throws Exception {
		int index = 0;
		NodeAssert.assertThat( data ).hasStates( false, false, 0, 0 );
		assertThat( data.getEventCount() ).isEqualTo( index );

		Txn.create();
		data.setValue( "a", "1" );
		data.setValue( "a", "2" );
		data.setValue( "a", "3" );
		data.setValue( "a", "4" );
		data.setValue( "a", "5" );
		Txn.commit();

		assertThat( data.<String> getValue( "a" ) ).isEqualTo( "5" );
		NodeAssert.assertThat( data ).hasStates( true, false, 1, 0 );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.VALUE_CHANGED, "a", null, "5" );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( data.event( index++ ) ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( data.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testGetParent() {
		MockNode parent = new MockNode();
		MockNode child = new MockNode();
		assertThat( child.<Node> getParent() ).isNull();

		String key = "key";

		parent.setValue( key, child );
		assertThat( child.<MockNode> getParent() ).isEqualTo( parent );

		parent.setValue( key, null );
		assertThat( child.<Node> getParent() ).isNull();
	}

	@Test
	void testGetNodePath() {
		MockNode parent = new MockNode();
		MockNode child = new MockNode();

		parent.setValue( "child", child );

		List<Node> path = parent.getNodePath();
		assertThat( path.size() ).isEqualTo( 1 );
		assertThat( path.get( 0 ) ).isEqualTo( parent );

		path = child.getNodePath();
		assertThat( path.size() ).isEqualTo( 2 );
		assertThat( path.get( 0 ) ).isEqualTo( parent );
		assertThat( path.get( 1 ) ).isEqualTo( child );
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

		assertThat( grandparent.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		NodeAssert.assertThat( grandparent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );

		child.setModified( true );
		assertThat( grandparent.isModified() ).isTrue();
		assertThat( parent.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();
		NodeAssert.assertThat( grandparent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( child ).hasStates( true, true, 0, 0 );

		parent.setModified( false );
		assertThat( grandparent.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		NodeAssert.assertThat( grandparent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
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
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		int index = 0;

		// Set an attribute on the child to modify the child and parent
		child.setValue( "key", "value0" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "key", null, "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Change the attribute value on the child
		child.setValue( "key", "value1" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "key", "value0", "value1" );
		// The parent is already modified so there should not be a modified event here
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Set the child attribute back to null to unmodify the child and parent
		child.setValue( "key", null );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "key", "value1", null );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );
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
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		int index = 0;

		// Set an attribute on the child to modify the child and parent
		child.setValue( "key", "value0" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "key", null, "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Clear the parent modified flag
		parent.setModified( false );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );

		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( index );
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
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		int index = 0;

		// Test setting a value on the child node modifies the parent
		child.setValue( "key", "value" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "key", null, "value" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Test clear the value on the child node unmodifies the parent
		child.setValue( "key", null );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "key", "value", null );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );
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
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		int index = 0;

		// Set an attribute on the child to a non-null value
		child.setValue( "key", "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "key", null, "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );

		// Clear the modified flags
		parent.setModified( false );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( TxnEvent.COMMIT_END );

		// Change the attribute value on the child
		child.setValue( "key", "value1" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "key", "value0", "value1" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Set the child attribute to the same value, should do nothing
		child.setValue( "key", "value1" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );

		// Set the child attribute back to value0
		child.setValue( "key", "value0" );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "key", "value1", "value0" );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, index++ ).hasEventState( child, TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( index );
	}

	@Test
	void testGrandparentModifiedByChildNodeAttributeChange() {
		int parentIndex = 0;
		int childIndex = 0;
		int grandChildIndex = 0;
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		MockNode grandChild = new MockNode( "grandChild" );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		parent.setValue( "child", child );
		assertThat( child.<Node> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		child.setValue( "child", grandChild );
		assertThat( grandChild.<Node> getParent() ).isEqualTo( child );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.CHILD_ADDED, "child", null, grandChild );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "child", null, grandChild );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, grandChild );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, grandChild );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( NodeEvent.ADDED );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		parent.setModified( false );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		// Test setting a value on the child node modifies the parents
		grandChild.setValue( "key", "value" );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( grandChild ).hasStates( true, false, 1, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, NodeEvent.VALUE_CHANGED, "key", null, "value" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( parent, NodeEvent.MODIFIED, null, null, null );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, NodeEvent.VALUE_CHANGED, "key", null, "value" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child, NodeEvent.MODIFIED, null, null, null );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "key", null, "value" );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		// Test unsetting the value on the child node unmodifies the parents
		grandChild.setValue( "key", null );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, NodeEvent.VALUE_CHANGED, "key", "value", null );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, NodeEvent.VALUE_CHANGED, "key", "value", null );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( grandChild, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "key", "value", null );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );
	}

	@Test
	void testParentModifiedByChildNodeClearedByFlag() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
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
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "1" );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "a", null, "1" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "a", null, "1" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "1" );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 2, 0 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "b", null, "1" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "b", null, "1" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Set this state as the new unmodified state
		child.setModified( false );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testParentModifiedByChildNodeClearedByValue() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setValue( "child", child );
		assertThat( child.<Node> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );

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

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "a", "2" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "a", null, "2" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "a", null, "2" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test setting the 'b' value on the child leaves the parent modified
		child.setValue( "b", "2" );
		NodeAssert.assertThat( child ).hasStates( true, false, 2, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "b", null, "2" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "b", null, "2" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test unsetting the 'a' value on the child leaves the parent modified
		child.setValue( "a", null );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "a", "2", null );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "a", "2", null );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Test unsetting the value 'b' on the child returns the parent to unmodified
		child.setValue( "b", null );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );

		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "b", "2", null );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "b", "2", null );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testChildModifiedClearedByParentSetModifiedFalse() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
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

		// Test setting the 'a' value on the child modifies the parent
		child.setValue( "x", "2" );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent ).hasStates( true, false, 0, 1 );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, NodeEvent.VALUE_CHANGED, "x", null, "2" );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child, TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.VALUE_CHANGED, "x", null, "2" );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setModified( false );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( TxnEvent.COMMIT_END );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );

		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testAddNodeAttributeToDifferentParent() {
		MockNode parent0 = new MockNode( "parent0" );
		MockNode parent1 = new MockNode( "parent1" );
		MockNode child = new MockNode( "child" );
		int parent0Index = 0;
		int parent1Index = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent0 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent1 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index );
		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Add the child attribute to parent 0
		parent0.setValue( "child", child );
		NodeAssert.assertThat( parent0 ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent1 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent0, NodeEvent.PARENT_CHANGED );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index );
		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index );

		// Clear the modified flag of parent 0.
		parent0.setModified( false );
		assertThat( child.<Node> getParent() ).isEqualTo( parent0 );
		NodeAssert.assertThat( parent0 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( parent1 ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.UNMODIFIED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_END );
		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index );
		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		// Add the child attribute to parent 1.
		parent1.setValue( "child", child );
		assertThat( child.<Node> getParent() ).isEqualTo( parent1 );
		assertThat( parent1.<Node> getValue( "child" ) ).isEqualTo( child );
		assertThat( parent0.<Node> getValue( "child" ) ).isNull();
		NodeAssert.assertThat( parent0 ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( parent1 ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.REMOVED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.CHILD_REMOVED, "child", child, null );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", child, null );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent0, parent0Index++ ).hasEventState( TxnEvent.COMMIT_END );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.ADDED );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( NodeEvent.CHILD_ADDED, "child", null, child );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( NodeEvent.VALUE_CHANGED, "child", null, child );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( NodeEvent.MODIFIED );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent1, parent1Index++ ).hasEventState( TxnEvent.COMMIT_END );
		//NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent1, NodeEvent.PARENT_CHANGED );
		assertThat( parent0.getEventCount() ).isEqualTo( parent0Index );
		assertThat( parent1.getEventCount() ).isEqualTo( parent1Index );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testChildReceivesParentChangedEvent() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		int parentIndex = 0;
		int childIndex = 0;
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		parent.setValue( "child", child );
		assertThat( child.<Node> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		assertThat( child.getEventCount() ).isEqualTo( childIndex += 1 );

		parent.addModifyingKeys( "x" );
		parent.setValue( "x", 1 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testGrandchildReceivesParentChangedEvent() {
		int parentIndex = 0;
		int childIndex = 0;
		int grandChildIndex = 0;
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		MockNode grandChild = new MockNode( "grandChild" );
		NodeAssert.assertThat( parent ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		parent.setValue( "child", child );
		assertThat( child.<Node> getParent() ).isEqualTo( parent );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( child ).hasStates( false, false, 0, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		assertThat( child.getEventCount() ).isEqualTo( childIndex += 1 );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );

		child.setValue( "child", grandChild );
		assertThat( grandChild.<Node> getParent() ).isEqualTo( child );
		NodeAssert.assertThat( parent ).hasStates( true, false, 1, 1 );
		NodeAssert.assertThat( child ).hasStates( true, false, 1, 0 );
		NodeAssert.assertThat( grandChild ).hasStates( false, false, 0, 0 );
		assertThat( child.getEventCount() ).isEqualTo( childIndex += 7 );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex += 1 );

		parent.addModifyingKeys( "x" );
		parent.setValue( "x", 1 );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		NodeEventAssert.assertThat( grandChild, grandChildIndex++ ).hasEventState( parent, NodeEvent.PARENT_CHANGED );
		assertThat( grandChild.getEventCount() ).isEqualTo( grandChildIndex );
	}

	@Test
	void testParentNodeNotModifiedByNodeAddedWithSetWithModifyFilter() {
		int parentIndex = 12;
		int childIndex = 1;

		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertThat( child.isModified() ).isFalse();
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );

		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		MockNode item0 = new MockNode( "0" );
		item0.setValue( "dont-modify", true );
		assertThat( item0.isModified() ).isFalse();
		// No new events should have occurred
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.removeItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		child.removeItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		// No new events should have occurred
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.addItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_ADDED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_ADDED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		child.addItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );

		child.removeItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_REMOVED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( parent, parentIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_BEGIN );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.CHILD_REMOVED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), NodeEvent.VALUE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( NodeEvent.NODE_CHANGED );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_SUCCESS );
		NodeEventAssert.assertThat( child, childIndex++ ).hasEventState( child.getValue( MockNode.ITEMS ), TxnEvent.COMMIT_END );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
		child.removeItem( item0 );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		assertThat( parent.getEventCount() ).isEqualTo( parentIndex );
		assertThat( child.getEventCount() ).isEqualTo( childIndex );
	}

	@Test
	void testNodeNotModifiedByChildAddUntilNodeFilterValueChange() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();

		MockNode item0 = new MockNode( "A" );

		// Make sure the modified flag is working as expected before using the dont-modify value
		child.addItem( item0 );
		assertThat( parent.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();
		assertThat( item0.isModified() ).isFalse();
		child.removeItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		// The dont-modify is not a modifying key
		item0.setValue( "dont-modify", true );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		// Adding the child should not cause any node to be modified
		child.addItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		// Some extra checking
		child.removeItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();
		child.addItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();
	}

	@Test
	void testNodeNotModifiedByChildRemoveUntilNodeFilterValueChange() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );
		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();

		MockNode item0 = new MockNode( "A" );

		// Make sure the modified flag is working as expected before using the dont-modify value
		child.addItem( item0 );
		assertThat( parent.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();
		assertThat( item0.isModified() ).isFalse();
		child.removeItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		item0.setValue( "dont-modify", true );
		child.addItem( item0 );
		assertThat( parent.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( item0.isModified() ).isFalse();

		// This does not modify the node because dont-modify is not a modifying key
		item0.setValue( "dont-modify", null );
		child.removeItem( item0 );
		assertThat( parent.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();
		assertThat( item0.isModified() ).isFalse();
	}

	@Test
	void testParentNodeNotModifiedByNodeSetWithModifyFilter() {
		MockNode parent = new MockNode( "parent" );
		MockNode child = new MockNode( "child" );
		parent.setValue( "child", child );
		parent.setModified( false );

		child.setSetModifyFilter( MockNode.ITEMS, n -> n.getValue( "dont-modify" ) == null );
		assertThat( child.isModified() ).isFalse();

		MockNode item0 = new MockNode( "0" );
		child.addItem( item0 );
		assertThat( item0.isModified() ).isFalse();
		assertThat( child.isModified() ).isTrue();
		assertThat( parent.isModified() ).isTrue();
		child.setModified( false );

		assertThat( item0.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModifiedByValue() ).isFalse();
		assertThat( parent.isModifiedByChild() ).isFalse();
		assertThat( parent.isModified() ).isFalse();

		item0.setValue( "dont-modify", true );
		assertThat( item0.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();

		item0.setValue( "a", "A" );
		assertThat( item0.isModified() ).isTrue();
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();

		item0.setValue( "dont-modify", null );
		assertThat( child.isModified() ).isFalse();
		assertThat( parent.isModified() ).isFalse();
		child.removeItem( item0 );
		assertThat( child.isModified() ).isTrue();
		assertThat( parent.isModified() ).isTrue();
		child.setModified( false );
	}

	@Test
	void testParentModifiedByAddingModifiedChild() {
		MockNode child = new MockNode( "child" );
		child.setModified( true );
		assertThat( data.isModified() ).isFalse();
		assertThat( child.isModified() ).isTrue();

		data.addItem( new MockNode().addItem( new MockNode().addItem( child ) ) );
		assertThat( data.isModified() ).isTrue();
		assertThat( child.isModified() ).isTrue();

		data.setModified( false );
		assertThat( data.isModified() ).isFalse();
		assertThat( child.isModified() ).isFalse();
	}

	@SuppressWarnings( "unused" )
	private void listEvents( NodeWatcher watcher ) {
		for( Event event : watcher.getEvents() ) {
			System.out.println( "Event: " + event );
		}
	}

}
