package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class HashUtilTest {

	@Test
	void testHash() {
		assertThat( HashUtil.hash( (String)null ) ).isNull();
		assertThat( HashUtil.hash( "" ) ).isEqualTo( "da39a3ee5e6b4b0d3255bfef95601890afd80709" );
		assertThat( HashUtil.hash( "test" ) ).isEqualTo( "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3" );
	}

	@Test
	void testHashWithFile() throws Exception {
		assertThat( HashUtil.hash( (File)null ) ).isNull();
		Path empty = Files.createTempFile( "HashUtil", "test" );
		try {
			assertThat( HashUtil.hash( empty ) ).isEqualTo( "da39a3ee5e6b4b0d3255bfef95601890afd80709" );
		} finally {
			FileUtil.delete( empty );
		}

		Path test = Files.createTempFile( "HashUtil", "test" );
		try {
			FileUtil.save( "test", test );
			assertThat( HashUtil.hash( test ) ).isEqualTo( "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3" );
		} finally {
			FileUtil.delete( test );
		}
	}

	@Test
	void testHashUsingSha3() {
		assertThat( HashUtil.hash( (String)null ) ).isNull();
		assertThat( HashUtil.hash( "", HashUtil.SHA3 ) ).isEqualTo( "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a" );
		assertThat( HashUtil.hash( "test", HashUtil.SHA3 ) ).isEqualTo( "36f028580bb02cc8272a9a020f4200e346e276ae664e45ee80745574e2f5ab80" );
	}

}
