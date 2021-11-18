package com.avereon.data;

import org.junit.jupiter.api.BeforeEach;

public abstract class BaseNodeTest {

	protected MockNode data;

	@BeforeEach
	void setup() {
		data = new MockNode();
	}

}
