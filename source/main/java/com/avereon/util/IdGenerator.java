package com.avereon.util;

import lombok.extern.flogger.Flogger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

@Flogger
public class IdGenerator {

	private static final Random RANDOM = new Random();

	// The character map must contain 16 characters, no more, no less
	// List of consonant letters with no descenders in no particular order
	private static final char[] map = "mhnflcdtvkxzrwbs".toCharArray();

	public static String getId() {
		return getId( System.currentTimeMillis() ^ RANDOM.nextLong() );
	}

	public static String getId( long value ) {
		StringBuilder builder = new StringBuilder();

		for( int count = 0; count < 16; count++ ) {
			builder.append( map[ (int)(value & 0xF) ] );
			value >>= 4;
		}

		return builder.toString();
	}

	public static String getId( String string ) {
		long id = 0;

		try {
			MessageDigest digest = MessageDigest.getInstance( "SHA-256" );
			byte[] hash = digest.digest( string.getBytes( StandardCharsets.UTF_8 ) );
			for( int index = 0; index < hash.length; index++ ) {
				long value = hash[ index ];
				value = value << (index % 8 * 8);
				id ^= value;
			}
		} catch( Exception exception ) {
			log.atSevere().withCause( exception ).log( "Error computing id for: %s", string );
		}

		return getId( id );
	}

	public static String slug( int value ) {
		StringBuilder builder = new StringBuilder();

		for( int count = 0; count < 8; count++ ) {
			builder.append( map[ value & 0xF ] );
			value >>= 4;
		}

		return builder.toString();
	}

	public static String slug( long value ) {
		return slug( (int)(value ^ (value >>> 32)) );
	}

}
