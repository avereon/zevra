package com.avereon.data;

import org.assertj.core.api.AbstractAssert;

public class NodeAssert extends AbstractAssert<NodeAssert, Node> {

	public static NodeAssert assertThat( Node actual ) {
		return new NodeAssert( actual );
	}

	protected NodeAssert( Node actual ) {
		super( actual, NodeAssert.class );
	}

	public NodeAssert hasStates(boolean modified, boolean modifiedBySelf, int modifiedValueCount, int modifiedChildCount) {
		if( actual.isModified() != modified) failWithMessage( "Expected modified to be %s but was %s", modified, actual.isModified() );
		if( actual.isModifiedBySelf() != modifiedBySelf) failWithMessage( "Expected modifiedBySelf to be %s but was %s", modifiedBySelf, actual.isModifiedBySelf() );
		if( actual.getModifiedValueCount() != modifiedValueCount) failWithMessage( "Expected modifiedValueCount modified to be %s but was %s", modifiedValueCount, actual.getModifiedValueCount() );
		if( actual.getModifiedChildCount() != modifiedChildCount) failWithMessage( "Expected modifiedChildCount to be %s but was %s", modifiedChildCount, actual.getModifiedChildCount() );
		return this;
	}
}
