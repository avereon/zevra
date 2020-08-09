package com.avereon.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IdNodeTest {

	@Test
	void testId() {
		assertNotNull( new MockIdNode().getId() );
	}

}
