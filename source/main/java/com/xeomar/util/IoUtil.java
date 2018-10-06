package com.xeomar.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class IoUtil {

	public static String toString( InputStream input ) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		input.transferTo( output );
		return output.toString( StandardCharsets.UTF_8 );
	}

}
