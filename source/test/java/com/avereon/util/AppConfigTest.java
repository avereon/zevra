package com.avereon.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class AppConfigTest {

	private final Path path = Paths.get( "source/test/resources/Application.cfg" );

	@BeforeEach
	void setup() {
		assertThat( path ).exists();
	}

	@Test
	void of() throws IOException {
		AppConfig config = AppConfig.of( path );
		assertThat( config.getJvmHeapMin() ).isEqualTo( 128 );
		assertThat( config.getJvmHeapMax() ).isEqualTo( 2048 );
		assertThat( config.getJvmHeapMinUnit() ).isEqualTo( "m" );
		assertThat( config.getJvmHeapMaxUnit() ).isEqualTo( "m" );
	}

	@Test
	void loadWithNoLines() {
		AppConfig config = new AppConfig( path );
		List<String> lines = new ArrayList<>();
		config.load( lines );
		assertThat( config.getJvmHeapMin() ).isEqualTo( 0 );
		assertThat( config.getJvmHeapMax() ).isEqualTo( 0 );
		assertThat( config.getJvmHeapMinUnit() ).isEqualTo( "" );
		assertThat( config.getJvmHeapMaxUnit() ).isEqualTo( "" );
		assertThat( config.getJvmMinHeapIndex() ).isEqualTo( -1 );
		assertThat( config.getJvmMaxHeapIndex() ).isEqualTo( -1 );
	}

	@Test
	void loadWithDefaultLines() {
		AppConfig config = new AppConfig( path );
		List<String> lines = getDefaultConfigLines();
		config.load( lines );
		assertThat( config.getJvmHeapMin() ).isEqualTo( 0 );
		assertThat( config.getJvmHeapMax() ).isEqualTo( 0 );
		assertThat( config.getJvmHeapMinUnit() ).isEqualTo( "" );
		assertThat( config.getJvmHeapMaxUnit() ).isEqualTo( "" );
		assertThat( config.getJvmMinHeapIndex() ).isEqualTo( -1 );
		assertThat( config.getJvmMaxHeapIndex() ).isEqualTo( -1 );
	}

	@Test
	void loadWithOnlyMinHeap() {
		AppConfig config = new AppConfig( path );
		List<String> lines = getDefaultConfigLines();
		lines.add( "java-options=-Xms1024" );
		config.load( lines );
		assertThat( config.getJvmHeapMin() ).isEqualTo( 1024 );
		assertThat( config.getJvmHeapMax() ).isEqualTo( 0 );
		assertThat( config.getJvmHeapMinUnit() ).isEqualTo( "" );
		assertThat( config.getJvmHeapMaxUnit() ).isEqualTo( "" );
		assertThat( config.getJvmMinHeapIndex() ).isEqualTo( 7 );
		assertThat( config.getJvmMaxHeapIndex() ).isEqualTo( -1 );
	}

	@Test
	void loadWithOnlyMaxHeap() {
		AppConfig config = new AppConfig( path );
		List<String> lines = getDefaultConfigLines();
		lines.add( "java-options=-Xmx2G" );
		config.load( lines );
		assertThat( config.getJvmHeapMin() ).isEqualTo( 0 );
		assertThat( config.getJvmHeapMax() ).isEqualTo( 2 );
		assertThat( config.getJvmHeapMinUnit() ).isEqualTo( "" );
		assertThat( config.getJvmHeapMaxUnit() ).isEqualTo( "g" );
		assertThat( config.getJvmMinHeapIndex() ).isEqualTo( -1 );
		assertThat( config.getJvmMaxHeapIndex() ).isEqualTo( 7 );
	}

	@Test
	void loadWithBothHeapValues() {
		AppConfig config = new AppConfig( path );
		List<String> lines = getDefaultConfigLines();
		lines.add( "java-options=-Xms512" );
		lines.add( "java-options=-Xmx2G" );
		config.load( lines );
		assertThat( config.getJvmHeapMin() ).isEqualTo( 512 );
		assertThat( config.getJvmHeapMax() ).isEqualTo( 2 );
		assertThat( config.getJvmHeapMinUnit() ).isEqualTo( "" );
		assertThat( config.getJvmHeapMaxUnit() ).isEqualTo( "g" );
		assertThat( config.getJvmMinHeapIndex() ).isEqualTo( 7 );
		assertThat( config.getJvmMaxHeapIndex() ).isEqualTo( 8 );
	}

	@Test
	void saveWithNoLinesAndBothHeapValuesAdded() {
		AppConfig config = new AppConfig( path );
		config.load( new ArrayList<>() );
		assertThat( config.getLines() ).isEmpty();

		// when
		config.setJvmHeapMin( 512 );
		config.setJvmHeapMax( 2 );
		config.setJvmHeapMinUnit( "b" );
		config.setJvmHeapMaxUnit( "g" );
		List<String> updatedLines = config.save( config.getLines() );

		// then
		assertThat( updatedLines ).contains( "[JavaOptions]" );
		assertThat( updatedLines ).contains( "java-options=-Xms512" );
		assertThat( updatedLines ).contains( "java-options=-Xmx2g" );
	}

	@Test
	void saveWithBothHeapValuesRemoved() {
		AppConfig config = new AppConfig( path );
		List<String> lines = getDefaultConfigLines();
		lines.add( "java-options=-Xms512" );
		lines.add( "java-options=-Xmx2G" );
		config.load( lines );

		// when
		config.setJvmHeapMin( -1 );
		config.setJvmHeapMax( -1 );
		config.setJvmHeapMinUnit( "g" );
		config.setJvmHeapMaxUnit( "g" );
		List<String> updatedLines = config.save( config.getLines() );

		// then
		assertThat( updatedLines ).contains( "[JavaOptions]" );
		assertThat( updatedLines ).doesNotContain( "java-options=-Xms512" );
		assertThat( updatedLines ).doesNotContain( "java-options=-Xmx2G" );
	}

	@Test
	void saveWithNeitherHeapValueChanged() {
		AppConfig config = new AppConfig( path );
		List<String> lines = getDefaultConfigLines();
		lines.add( "java-options=-Xms512" );
		lines.add( "java-options=-Xmx2G" );
		config.load( lines );

		// when
		config.setJvmHeapMin( 512 );
		config.setJvmHeapMax( 2 );
		config.setJvmHeapMinUnit( "b" );
		config.setJvmHeapMaxUnit( "g" );
		List<String> updatedLines = config.save( config.getLines() );

		// then
		assertThat( updatedLines ).contains( "[JavaOptions]" );
		assertThat( updatedLines ).contains( "java-options=-Xms512" );
		assertThat( updatedLines ).contains( "java-options=-Xmx2g" );
	}

	@Test
	void saveWithBothHeapValuesChanged() {
		AppConfig config = new AppConfig( path );
		List<String> lines = getDefaultConfigLines();
		lines.add( "java-options=-Xms512" );
		lines.add( "java-options=-Xmx2G" );
		config.load( lines );

		// when
		config.setJvmHeapMin( 1 );
		config.setJvmHeapMax( 4 );
		config.setJvmHeapMinUnit( "g" );
		config.setJvmHeapMaxUnit( "g" );
		List<String> updatedLines = config.save( config.getLines() );

		// then
		assertThat( updatedLines ).contains( "[JavaOptions]" );
		assertThat( updatedLines ).contains( "java-options=-Xms1g" );
		assertThat( updatedLines ).contains( "java-options=-Xmx4g" );
	}

	@ParameterizedTest
	@MethodSource
	void parseHeapOptions( String line, int expectedValue, String expectedUnit ) {
		AppConfig config = new AppConfig( path );
		String[] result = config.parseHeapOptions( line );
		assertThat( result[ 0 ] ).isEqualTo( String.valueOf( expectedValue ) );
		assertThat( result[ 1 ] ).isEqualTo( expectedUnit );
	}

	public static Stream<Arguments> parseHeapOptions() {
		return Stream.of(
			Arguments.of( "java-options=-Xmx2", 2, "" ),
			Arguments.of( "java-options=-Xmx2k", 2, "k" ),
			Arguments.of( "java-options=-Xmx2m", 2, "m" ),
			Arguments.of( "java-options=-Xmx2g", 2, "g" ),
			Arguments.of( "java-options=-Xmx2K", 2, "k" ),
			Arguments.of( "java-options=-Xmx2M", 2, "m" ),
			Arguments.of( "java-options=-Xmx2G", 2, "g" )
		);
	}

	private List<String> getDefaultConfigLines() {
		List<String> lines = new ArrayList<>();
		lines.add( "[Application]" );
		lines.add( "" );
		lines.add( "app.mainmodule=com.example.application/com.example.application.Launcher" );
		lines.add( "" );
		lines.add( "[JavaOptions]" );
		lines.add( "java-options=-Djpackage.app-version=1.9" );
		lines.add( "java-options=-Dprism.forceGPU=true" );
		return lines;
	}

}
