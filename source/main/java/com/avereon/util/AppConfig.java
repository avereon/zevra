package com.avereon.util;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
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
@CustomLog
public class AppConfig {

	public static final List<String> HEAP_UNITS = List.of( "B", "K", "M", "G" );

	public static final String JAVA_OPTIONS_HEADER = "[JavaOptions]";

	public static final String JAVA_OPTIONS = "java-options=";

	public static final String MX = "-Xmx";

	public static final String MS = "-Xms";

	@Setter( AccessLevel.NONE )
	private Path path;

	/**
	 * The configuration file lines.
	 */
	private List<String> lines;

	/**
	 * The minimum heap size. Less than zero will be interpreted as automatic,
	 * and the JVM option will not be set.
	 */
	private int jvmHeapMin;

	/**
	 * The maximum heap size. Less than zero will be interpreted as automatic,
	 * and the JVM option will not be set.
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

	public static AppConfig of( Path path ) {
		return new AppConfig( path );
	}

	public AppConfig save() throws IOException {
		List<String> updatedLines = save( this.lines );

		try {
			Files.write( path, updatedLines, StandardCharsets.UTF_8 );
		} catch( AccessDeniedException exception ) {
			log.atWarn().withCause( exception ).log( "Unable to save the application configuration" );
		}
		return this;
	}

	List<String> save( List<String> lines ) {
		int minAction = getAction( jvmMinHeapIndex, jvmHeapMin < 0 ? -1 : 0 );
		int maxAction = getAction( jvmMaxHeapIndex, jvmHeapMax < 0 ? -1 : 0 );

		if( minAction == 0 && maxAction == 0 ) new ArrayList<>( lines );

		if( jvmMaxHeapIndex > jvmMinHeapIndex ) {
			applyAction( maxAction, lines, jvmMaxHeapIndex, MX, jvmHeapMax, jvmHeapMaxUnit );
			applyAction( minAction, lines, jvmMinHeapIndex, MS, jvmHeapMin, jvmHeapMinUnit );
		} else {
			applyAction( minAction, lines, jvmMinHeapIndex, MS, jvmHeapMin, jvmHeapMinUnit );
			applyAction( maxAction, lines, jvmMaxHeapIndex, MX, jvmHeapMax, jvmHeapMaxUnit );
		}

		if( minAction == 2 ) jvmMinHeapIndex = -1;
		if( maxAction == 2 ) jvmMaxHeapIndex = -1;

		return new ArrayList<>( lines );
	}

	private void applyAction( int action, List<String> lines, int index, String option, int value, String unit ) {
		int javaOptionsIndex = lines.indexOf( JAVA_OPTIONS_HEADER );
		if( javaOptionsIndex < 0 ) {
			lines.add( JAVA_OPTIONS_HEADER );
			javaOptionsIndex = lines.size() - 1;
		}

		if( unit.equalsIgnoreCase( HEAP_UNITS.getFirst() ) ) unit = "";

		switch( action ) {
			case 0:
				// Nothing
				lines.set( index, JAVA_OPTIONS + option + value + unit.toLowerCase() );
				break;
			case 1:
				// Insert
				if( index < 0 ) index = javaOptionsIndex + 1;
				lines.add( index, JAVA_OPTIONS + option + value + unit.toLowerCase() );
				break;
			case 2:
				// Remove
				if( index >= 0 ) lines.remove( index );
				break;
			case 3:
				// Update
				lines.set( index, JAVA_OPTIONS + option + value + unit.toLowerCase() );
				break;
		}
	}

	private int getAction( int incoming, int outgoing ) {
		// Nothing
		if( incoming < 0 && outgoing < 0 ) return 0;
		// Insert (no incoming, adding the outgoing)
		if( incoming < 0 ) return 1;
		// Remove (has incoming, removing the outgoing)
		if( outgoing < 0 ) return 2;
		// Update (has both incoming and outgoing)
		return 3;
	}

	public AppConfig load() throws IOException {
		if( !Files.exists( path ) ) throw new FileNotFoundException( "Application configuration file does not exist: " + path );
		if( !Files.isRegularFile( path ) ) throw new IOException( "Application configuration file is not a regular file: " + path );
		if( !Files.isReadable( path ) ) throw new IOException( "Application configuration file is not readable: " + path );
		return load( Files.readAllLines( path, StandardCharsets.UTF_8 ) );
	}

	AppConfig load( List<String> lines ) {
		// Find the min line
		String minLine = lines.stream().filter( line -> line.startsWith( JAVA_OPTIONS + MS ) ).findFirst().orElse( null );
		// Find the max line
		String maxLine = lines.stream().filter( line -> line.startsWith( JAVA_OPTIONS + MX ) ).findFirst().orElse( null );

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
		String prefix = JAVA_OPTIONS + MS;

		int index = prefix.length();
		while( index < line.length() && Character.isDigit( line.charAt( index ) ) ) index++;
		String value = line.substring( prefix.length(), index );
		String unit = line.substring( index ).toLowerCase();

		return new String[]{ value, unit };
	}

}
