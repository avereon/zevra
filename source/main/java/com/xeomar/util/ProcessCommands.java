package com.xeomar.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessCommands {

	public static List<String> forModule() {
		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );
		return forModule( modulePath, moduleMain, moduleMainClass );
	}

	public static List<String> forModule( Parameters parameters, String... extraCommands ) {
		List<String> commands = forModule();
		commands.addAll( getParameterCommands( parameters, extraCommands ) );
		return commands;
	}

	public static List<String> forModule( String modulePath, String moduleMain, String moduleMainClass, Parameters parameters, String... extraCommands ) {
		List<String> commands = forModule( modulePath, moduleMain, moduleMainClass );
		commands.addAll( getParameterCommands( parameters, extraCommands ) );
		return commands;
	}

	public static List<String> forModule( String modulePath, String moduleMain, String moduleMainClass ) {
		List<String> commands = new ArrayList<>();

		if( modulePath == null || moduleMain == null || moduleMainClass == null ) return commands;

		// Add the executable path
		commands.add( getExecutablePath() );

		// Add the VM parameters to the commands
		RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		for( String command : runtimeBean.getInputArguments() ) {
			// Skip some commands
			if( command.equals( "exit" ) ) continue;
			if( command.equals( "abort" ) ) continue;
			if( command.startsWith( "--module-path" ) ) continue;
			if( command.startsWith( "-Djdk.module.main" ) ) continue;
			if( command.startsWith( "-Djdk.module.main.class" ) ) continue;

			if( !commands.contains( command ) ) commands.add( command );
		}

		// Add the module information
		commands.add( "-p" );
		commands.add( modulePath );
		commands.add( "-m" );
		commands.add( moduleMain + "/" + moduleMainClass );

		return commands;
	}

	public static List<String> getParameterCommands( Parameters parameters, String... extraCommands ) {
		Parameters extraParameters = Parameters.parse( extraCommands ).add( parameters );

		// Collect program flags.
		Map<String, List<String>> flags = new HashMap<>();
		for( String name : extraParameters.getFlags() ) {
			flags.put( name, extraParameters.getValues( name ) );
		}

		List<String> commands = new ArrayList<>();

		// Add the collected flags.
		for( String flag : flags.keySet() ) {
			List<String> values = flags.get( flag );
			commands.add( flag );
			if( values.size() > 1 || !"true".equals( values.get( 0 ) ) ) commands.addAll( values );
		}

		// Add the collected URIs.
		List<String> uris = extraParameters.getUris();
		if( uris.size() > 0 ) commands.addAll( uris );

		return commands;
	}

	private static String getExecutablePath() {
		return OperatingSystem.getJavaExecutablePath();
	}

}