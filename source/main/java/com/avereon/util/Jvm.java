package com.avereon.util;

import java.security.SecureRandom;

public class Jvm {

	public static final long ID = Math.abs( new SecureRandom().nextLong() );

}
