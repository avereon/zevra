package com.avereon.data;

import com.avereon.event.Event;
import com.avereon.transaction.Txn;
import com.avereon.transaction.TxnEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NodeApiEventTest extends BaseNodeTest {

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

	@SuppressWarnings( "unused" )
	private void listEvents( NodeWatcher watcher ) {
		for( Event event : watcher.getEvents() ) {
			System.out.println( "Event: " + event );
		}
	}

}
