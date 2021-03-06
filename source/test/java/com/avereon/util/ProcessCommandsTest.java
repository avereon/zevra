package com.avereon.util;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class ProcessCommandsTest {

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
		String javaPath = OperatingSystem.getJavaLauncherPath();
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
		String javaPath = OperatingSystem.getJavaLauncherPath();
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
		String javaPath = OperatingSystem.getJavaLauncherPath();
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
