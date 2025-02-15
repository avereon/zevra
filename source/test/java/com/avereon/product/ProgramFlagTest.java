package com.avereon.product;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProgramFlagTest {

	@Test
	void exists() {
		assertThat( ProgramFlag.HELP ).isEqualTo( "--help" );
	}

}
