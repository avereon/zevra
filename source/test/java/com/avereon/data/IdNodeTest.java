package com.avereon.data;

import com.avereon.transaction.Txn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IdNodeTest {

	@Test
	void testId() {
		assertNotNull( new MockIdNode().getId() );
	}

	@Test
	void testConstructor() {
		IdNode node = new MockIdNode();
		assertNotNull( node.getId() );
	}

	@Test
	void testConstructorWithTransaction() throws Exception {
		IdNode node;
		try( Txn ignore = Txn.create() ) {
			node = new MockIdNode();
			// The id will be null until the txn is complete
			assertNull( node.getId() );
		}
		assertNotNull( node.getId() );
	}

}
