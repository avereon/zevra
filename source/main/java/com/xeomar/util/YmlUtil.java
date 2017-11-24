package com.xeomar.util;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;

public class YmlUtil {

	/**
	 * Load data from a Yaml input stream to a Java bean class. This method will
	 * close the input stream when the load is complete.
	 *
	 * @param input The Yaml input stream
	 * @param clazz The class of the Java bean
	 * @return An instance of the Java bean
	 * @throws IOException If an IO exception occurs
	 */
	@SuppressWarnings( "unchecked" )
	public static <T> T load( InputStream input, Class<T> clazz ) throws IOException {
		try( InputStream stream = input ) {
			return (T)new Yaml( new Constructor( clazz ) ).load( stream );
		}
	}

}
