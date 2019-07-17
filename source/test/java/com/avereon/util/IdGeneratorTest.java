package com.avereon.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IdGeneratorTest {

	@Test
	public void testGetId() {
		String id = IdGenerator.getId();
		assertThat( id.length(), is( 16 ) );
	}

	@Test
	public void testGetIdWithInt() {
		assertThat( IdGenerator.slug( 0x00000000 ), is( "mmmmmmmm" ) );
		assertThat( IdGenerator.slug( 0x55555555 ), is( "cccccccc" ) );
		assertThat( IdGenerator.slug( 0xaaaaaaaa ), is( "xxxxxxxx" ) );
		assertThat( IdGenerator.slug( 0xffffffff ), is( "ssssssss" ) );
	}

	@Test
	public void testGetIdWithLong() {
		assertThat( IdGenerator.getId( 0x0000000000000000L ), is( "mmmmmmmmmmmmmmmm" ) );
		assertThat( IdGenerator.getId( 0x5555555555555555L ), is( "cccccccccccccccc" ) );
		assertThat( IdGenerator.getId( 0xaaaaaaaaaaaaaaaaL ), is( "xxxxxxxxxxxxxxxx" ) );
		assertThat( IdGenerator.getId( 0xffffffffffffffffL ), is( "ssssssssssssssss" ) );
	}

	@Test
	public void testGetIdWithString() {
		assertThat( IdGenerator.getId( "test"), is( "ttzlkhsndwnhcnlc" ) );
		assertThat( IdGenerator.getId( "tent"), is( "zzwvwkwxwwkkzrrl" ) );
		assertThat( IdGenerator.getId( "bent"), is( "btkclsbfbtnhnsvx" ) );
		assertThat( IdGenerator.getId( "bunt"), is( "lrzcnrdcbkbzxkfc" ) );
	}

}
