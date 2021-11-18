package com.avereon.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorTest {

	@Test
	void testGetId() {
		String id = IdGenerator.getId();
		assertThat( id.length() ).isEqualTo( 16 );
	}

	@Test
	void testGetIdWithInt() {
		assertThat( IdGenerator.slug( 0x00000000 ) ).isEqualTo( "mmmmmmmm" );
		assertThat( IdGenerator.slug( 0x55555555 ) ).isEqualTo( "cccccccc" );
		assertThat( IdGenerator.slug( 0xaaaaaaaa ) ).isEqualTo( "xxxxxxxx" );
		assertThat( IdGenerator.slug( 0xffffffff ) ).isEqualTo( "ssssssss" );
	}

	@Test
	void testGetIdWithLong() {
		assertThat( IdGenerator.getId( 0x0000000000000000L ) ).isEqualTo( "mmmmmmmmmmmmmmmm" );
		assertThat( IdGenerator.getId( 0x5555555555555555L ) ).isEqualTo( "cccccccccccccccc" );
		assertThat( IdGenerator.getId( 0xaaaaaaaaaaaaaaaaL ) ).isEqualTo( "xxxxxxxxxxxxxxxx" );
		assertThat( IdGenerator.getId( 0xffffffffffffffffL ) ).isEqualTo( "ssssssssssssssss" );
	}

	@Test
	void testGetIdWithString() {
		assertThat( IdGenerator.getId( "test" ) ).isEqualTo( "ttzlkhsndwnhcnlc" );
		assertThat( IdGenerator.getId( "tent" ) ).isEqualTo( "zzwvwkwxwwkkzrrl" );
		assertThat( IdGenerator.getId( "bent" ) ).isEqualTo( "btkclsbfbtnhnsvx" );
		assertThat( IdGenerator.getId( "bunt" ) ).isEqualTo( "lrzcnrdcbkbzxkfc" );
	}

}
