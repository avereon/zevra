package com.avereon.util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Convenience class to generate command line values for processes.
 * </p>
 * <p>
 * Prior to Java 14 the only official way to start a Java VM was to use the java
 * (or javaw) launchers provided with the Java runtime. Starting with Java 14
 * there is an official way to generate platform custom runtime launchers that
 * are usually named for the product.
 * </p>
 */
public class ProcessCommands {

	public static String getCommandLineAsString() {
		return TextUtil.toString( getCommandLine(), " " );
	}

	public static List<String> getCommandLine() {
		List<String> commands = new ArrayList<>();
		commands.add( OperatingSystem.getJavaLauncherPath() );
		commands.addAll( ManagementFactory.getRuntimeMXBean().getInputArguments() );
		return commands;
	}

	public static List<String> forModule() {
		String modulePath = System.getProperty( "jdk.module.path" );
		String moduleMain = System.getProperty( "jdk.module.main" );
		String moduleMainClass = System.getProperty( "jdk.module.main.class" );
		return forModule( null, modulePath, moduleMain, moduleMainClass );
	}

	//	@Deprecated
	//	public static List<String> forModule( String mainModule, String mainClass ) {
	//		return forModule( null, mainModule, mainClass );
	//	}
	//
	//	@Deprecated
	//	public static List<String> forModule( String modulePath, String mainModule, String mainClass ) {
	//		return forModule( null, modulePath, mainModule, mainClass );
	//	}
	//
	//	@Deprecated
	//	public static List<String> forModule( String modulePath, String mainModule, String mainClass, Parameters parameters, String... extraCommands ) {
	//		return forModule( null, modulePath, mainModule, mainClass, parameters, extraCommands );
	//	}

	public static List<String> forModule(
		String javaExecutablePath, String modulePath, String mainModule, String mainClass, Parameters parameters, String... extraCommands
	) {

		List<String> commands = forModule( javaExecutablePath, modulePath, mainModule, mainClass );
		commands.addAll( getParameterCommands( parameters, extraCommands ) );
		return commands;
	}

	public static List<String> forModule( String javaLauncherPath, String modulePath, String mainModule, String mainClass ) {
		List<String> commands = new ArrayList<>();

		//if( modulePath == null ) throw new NullPointerException( "Module path cannot be null"  );
		if( mainModule == null ) throw new NullPointerException( "Main module cannot be null" );
		if( mainClass == null ) throw new NullPointerException( "Module main class cannot be null" );

		// Add the java executable path
		commands.add( javaLauncherPath == null ? OperatingSystem.getJavaLauncherPath() : javaLauncherPath );

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
		if( modulePath != null ) {
			commands.add( "-p" );
			commands.add( modulePath );
		}

		commands.add( "-m" );
		commands.add( mainModule + "/" + mainClass );

		return commands;
	}

	private static List<String> getParameterCommands( Parameters parameters, String... extraCommands ) {
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

}
