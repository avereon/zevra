package com.xeomar.util;

import junit.framework.TestCase;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class HashUtilTest extends TestCase {

	public void testHash() {
		assertNull( HashUtil.hash( (String)null ) );
		assertEquals( "da39a3ee5e6b4b0d3255bfef95601890afd80709", HashUtil.hash( "" ) );
		assertEquals( "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", HashUtil.hash( "test" ) );
	}

	public void testHashWithFile() throws Exception {
		assertNull( HashUtil.hash( (File)null ) );
		Path empty = Files.createTempFile( "HashUtil", "test" );
		try {
			assertEquals( "da39a3ee5e6b4b0d3255bfef95601890afd80709", HashUtil.hash( empty ) );
		} finally {
			FileUtil.delete( empty );
		}

		Path test = Files.createTempFile( "HashUtil", "test" );
		try {
			FileUtil.save( "test", test );
			assertEquals( "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", HashUtil.hash( test ) );
		} finally {
			FileUtil.delete( test );
		}
	}

	public void testHashUsingSha3() {
		assertNull( HashUtil.hash( (String)null ) );
		assertEquals( "a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a", HashUtil.hash( "", HashUtil.SHA3 ) );
		assertEquals( "36f028580bb02cc8272a9a020f4200e346e276ae664e45ee80745574e2f5ab80", HashUtil.hash( "test", HashUtil.SHA3 ) );
	}

}
