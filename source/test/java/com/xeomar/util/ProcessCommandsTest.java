package com.xeomar.util;

import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ProcessCommandsTest {

	@Test
	public void testForModuleWithNull() {
		try {
			ProcessCommands.forModule( null, null, null );
			fail( "Should throw NullPointerException");
		} catch( NullPointerException exception ) {
			// This exception should be thrown
		}
	}

	@Test
	public void testForModule() {
		String modulePath = "/var/tmp/util/modules";
		String moduleMain = "com.xeomar.util";
		String moduleMainClass = "com.xeomar.util.Module";
		List<String> commands = ProcessCommands.forModule( modulePath, moduleMain, moduleMainClass );

		// Determine the runtime parameters
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

		// Assert all the commands
		int index = 0;
		assertThat( commands.get(index++), is( OperatingSystem.getJavaExecutablePath() ) );
		for( String command : runtimeCommands ) {
			assertThat( commands.get(index++), is( command ) );
		}
		assertThat( commands.get(index++), is( "-p" ) );
		assertThat( commands.get(index++), is( modulePath ) );
		assertThat( commands.get(index++), is( "-m" ) );
		assertThat( commands.get(index++), is( moduleMain + "/" + moduleMainClass ) );
		assertThat( commands.size(), is( index ) );
	}

	@Test
	public void testForModuleWithParameters() {
		String modulePath = "/var/tmp/util/modules";
		String moduleMain = "com.xeomar.util";
		String moduleMainClass = "com.xeomar.util.Module";
		Parameters parameters = Parameters.parse( new String[]{ "-flag", "value" } );
		List<String> commands = ProcessCommands.forModule( modulePath, moduleMain, moduleMainClass, parameters );

		// Determine the runtime parameters
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

		// Assert all the commands
		int index = 0;
		assertThat( commands.get(index++), is( OperatingSystem.getJavaExecutablePath() ) );
		for( String command : runtimeCommands ) {
			assertThat( commands.get(index++), is( command ) );
		}
		assertThat( commands.get(index++), is( "-p" ) );
		assertThat( commands.get(index++), is( modulePath ) );
		assertThat( commands.get(index++), is( "-m" ) );
		assertThat( commands.get(index++), is( moduleMain + "/" + moduleMainClass ) );
		assertThat( commands.get(index++), is( "-flag" ) );
		assertThat( commands.get(index++), is( "value" ) );
		assertThat( commands.size(), is( index ) );
	}

	@Test
	public void testForModuleWithParametersAndExtraCommands() {
		String modulePath = "/var/tmp/util/modules";
		String moduleMain = "com.xeomar.util";
		String moduleMainClass = "com.xeomar.util.Module";
		Parameters parameters = Parameters.parse( new String[]{ "-flag", "value" } );
		List<String> commands = ProcessCommands.forModule( modulePath, moduleMain, moduleMainClass, parameters, "-hello" );

		// Determine the runtime parameters
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

		// Assert all the commands
		int index = 0;
		assertThat( commands.get(index++), is( OperatingSystem.getJavaExecutablePath() ) );
		for( String command : runtimeCommands ) {
			assertThat( commands.get(index++), is( command ) );
		}
		assertThat( commands.get(index++), is( "-p" ) );
		assertThat( commands.get(index++), is( modulePath ) );
		assertThat( commands.get(index++), is( "-m" ) );
		assertThat( commands.get(index++), is( moduleMain + "/" + moduleMainClass ) );
		assertThat( commands.get(index++), is( "-flag" ) );
		assertThat( commands.get(index++), is( "value" ) );
		assertThat( commands.get(index++), is( "-hello" ) );
		assertThat( commands.size(), is( index ) );
	}

}
