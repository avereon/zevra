package com.avereon.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A class to read the application configuration file at
 * <code>{appHome}/lib/app/{appName}.cfg</code>.
 */
@Getter
@Setter
public class AppConfig {

	public static final List<String> HEAP_UNITS = List.of( "B", "K", "M", "G" );

	@Setter( AccessLevel.NONE )
	private Path path;

	/**
	 * The configuration file lines.
	 */
	private List<String> lines;

	/**
	 * The minimum heap size. Zero will be interpreted as automatic, and the JVM
	 * option will not be set.
	 */
	private int jvmHeapMin;

	/**
	 * The maximum heap size. Zero will be interpreted as automatic, and the JVM
	 * option will not be set.
	 */
	private int jvmHeapMax;

	/**
	 * The unit for the minimum heap size. Must be one of "g", "m", "k", "b",
	 * or "". The values "b" and "" are equivalent to bytes and are written as "".
	 */
	@NonNull
	private String jvmHeapMinUnit = "";

	/**
	 * The unit for the maximum heap size. Must be one of "g", "m", "k", "b",
	 * or "". The values "b" and "" are equivalent to bytes and are written as "".
	 */
	@NonNull
	private String jvmHeapMaxUnit = "";

	private int jvmMinHeapIndex = -1;

	private int jvmMaxHeapIndex = -1;

	AppConfig( Path path ) {
		this.path = path;
	}

	public static AppConfig of( Path path ) throws IOException {
		if( !Files.exists( path ) ) throw new FileNotFoundException( "Application configuration file does not exist: " + path );
		if( !Files.isRegularFile( path ) ) throw new IOException( "Application configuration file is not a regular file: " + path );
		if( !Files.isReadable( path ) ) throw new IOException( "Application configuration file is not readable: " + path );
		//if( !Files.isWritable( path ) ) throw new IOException( "Application configuration file is not writable: " + path );

		return new AppConfig( path ).load();
	}

	private AppConfig load() throws IOException {
		return load( Files.readAllLines( path, StandardCharsets.UTF_8 ) );
	}

	AppConfig load( List<String> lines ) {
		// Find the min line
		String minLine = lines.stream().filter( line -> line.startsWith( "java-options=-Xms" ) ).findFirst().orElse( null );
		// Find the max line
		String maxLine = lines.stream().filter( line -> line.startsWith( "java-options=-Xmx" ) ).findFirst().orElse( null );

		if( minLine != null ) {
			String[] minOptions = parseHeapOptions( minLine );
			this.setJvmHeapMin( Integer.parseInt( minOptions[ 0 ] ) );
			this.setJvmHeapMinUnit( minOptions[ 1 ] );
			jvmMinHeapIndex = lines.indexOf( minLine );
		}

		if( maxLine != null ) {
			String[] maxOptions = parseHeapOptions( maxLine );
			this.setJvmHeapMax( Integer.parseInt( maxOptions[ 0 ] ) );
			this.setJvmHeapMaxUnit( maxOptions[ 1 ] );
			jvmMaxHeapIndex = lines.indexOf( maxLine );
		}

		this.lines = new ArrayList<>( lines );

		return this;
	}

	String[] parseHeapOptions( String line ) {
		// This is just for sizing
		String prefix = "java-options=-Xmm";

		int index = prefix.length();
		while( index < line.length() && Character.isDigit( line.charAt( index ) ) ) index++;
		String value = line.substring( prefix.length(), index );
		String unit = line.substring( index ).toLowerCase();

		return new String[]{ value, unit };
	}

}
