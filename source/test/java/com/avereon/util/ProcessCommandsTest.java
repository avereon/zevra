package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class ProcessCommandsTest {

	@Test
	void testGetLauncherPath() {
		// This assumes both java.launcher.path and java.launcher.name are null
		assertThat( ProcessCommands.getLauncherPath(), is( OperatingSystem.getJavaExecutablePath() ) );

		System.setProperty( "java.launcher.path", "/this/is/the/launcher/path" );
		System.setProperty( "java.launcher.name", "Mock" );
		assertThat( ProcessCommands.getLauncherPath(), is( "/this/is/the/launcher/path" + File.separator + "Mock" + OperatingSystem.getExeSuffix() ) );
	}

	@Test
	void testForModuleWithNull() {
		try {
			ProcessCommands.forModule( null, null, null, null );
			fail( "Should throw NullPointerException" );
		} catch( NullPointerException exception ) {
			// This exception should be thrown
		}
	}

	@Test
	void testForModule() {
		String javaPath = OperatingSystem.getJavaExecutablePath();
		String modulePath = "/var/tmp/util/modules";
		String moduleMain = "com.avereon.util";
		String moduleMainClass = "com.avereon.util.Module";
		List<String> commands = ProcessCommands.forModule( javaPath, modulePath, moduleMain, moduleMainClass );

		// Determine the runtime commands
		List<String> runtimeCommands = getRuntimeCommands();

		// Assert all the commands
		int index = 0;
		assertThat( commands.get( index++ ), is( javaPath ) );
		for( String command : runtimeCommands ) {
			assertThat( commands.get( index++ ), is( command ) );
		}
		assertThat( commands.get( index++ ), is( "-p" ) );
		assertThat( commands.get( index++ ), is( modulePath ) );
		assertThat( commands.get( index++ ), is( "-m" ) );
		assertThat( commands.get( index++ ), is( moduleMain + "/" + moduleMainClass ) );
		assertThat( commands.size(), is( index ) );
	}

	@Test
	void testForModuleWithParameters() {
		String javaPath = OperatingSystem.getJavaExecutablePath();
		String modulePath = "/var/tmp/util/modules";
		String moduleMain = "com.avereon.util";
		String moduleMainClass = "com.avereon.util.Module";
		Parameters parameters = Parameters.parse( "-flag", "value" );
		List<String> commands = ProcessCommands.forModule( javaPath, modulePath, moduleMain, moduleMainClass, parameters );

		// Determine the runtime commands
		List<String> runtimeCommands = getRuntimeCommands();

		// Assert all the commands
		int index = 0;
		assertThat( commands.get( index++ ), is( javaPath ) );
		for( String command : runtimeCommands ) {
			assertThat( commands.get( index++ ), is( command ) );
		}
		assertThat( commands.get( index++ ), is( "-p" ) );
		assertThat( commands.get( index++ ), is( modulePath ) );
		assertThat( commands.get( index++ ), is( "-m" ) );
		assertThat( commands.get( index++ ), is( moduleMain + "/" + moduleMainClass ) );
		assertThat( commands.get( index++ ), is( "-flag" ) );
		assertThat( commands.get( index++ ), is( "value" ) );
		assertThat( commands.size(), is( index ) );
	}

	@Test
	void testForModuleWithParametersAndExtraCommands() {
		String javaPath = OperatingSystem.getJavaExecutablePath();
		String modulePath = "/var/tmp/util/modules";
		String moduleMain = "com.avereon.util";
		String moduleMainClass = "com.avereon.util.Module";
		Parameters parameters = Parameters.parse( "-flag", "value" );
		List<String> commands = ProcessCommands.forModule( javaPath, modulePath, moduleMain, moduleMainClass, parameters, "-hello" );

		// Determine the runtime commands
		List<String> runtimeCommands = getRuntimeCommands();

		// Assert all the commands
		int index = 0;
		assertThat( commands.get( index++ ), is( javaPath ) );
		for( String command : runtimeCommands ) {
			assertThat( commands.get( index++ ), is( command ) );
		}
		assertThat( commands.get( index++ ), is( "-p" ) );
		assertThat( commands.get( index++ ), is( modulePath ) );
		assertThat( commands.get( index++ ), is( "-m" ) );
		assertThat( commands.get( index++ ), is( moduleMain + "/" + moduleMainClass ) );
		assertThat( commands.get( index++ ), is( "-flag" ) );
		assertThat( commands.get( index++ ), is( "value" ) );
		assertThat( commands.get( index++ ), is( "-hello" ) );
		assertThat( commands.size(), is( index ) );
	}

	private List<String> getRuntimeCommands() {
		List<String> runtimeCommands = new ArrayList<>();
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		for( String command : runtimeBean.getInputArguments() ) {
			// Skip some commands
			if( command.equals( "exit" ) ) continue;
			if( command.equals( "abort" ) ) continue;
			if( command.startsWith( "--module-path" ) ) continue;
			if( command.startsWith( "-Djdk.module.main" ) ) continue;
			if( command.startsWith( "-Djdk.module.main.class" ) ) continue;
			if( !runtimeCommands.contains( command ) ) runtimeCommands.add( command );
		}
		return runtimeCommands;
	}

}
