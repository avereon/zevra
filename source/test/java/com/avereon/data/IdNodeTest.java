package com.avereon.data;

import com.avereon.transaction.Txn;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IdNodeTest {

	@Test
	void testId() {
		assertThat( new MockIdNode().getId() ).isNotNull();
	}

	@Test
	void testConstructor() {
		assertThat( new MockIdNode() ).isNotNull();
	}

	@Test
	void testConstructorWithTransaction() throws Exception {
		IdNode node;
		try( Txn ignore = Txn.create() ) {
			node = new MockIdNode();
			// The id will be null until the txn is complete
			assertThat( node.getId() ).isNull();
		}
		assertThat( node.getId() ).isNotNull();
	}

}
