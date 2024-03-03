package com.avereon.util;

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

	public static List<String> forLauncher() {
		String launcher = OperatingSystem.getJavaLauncherName();
		if( launcher.startsWith( "java" ) ) return forModule();
		return new ArrayList<>( List.of( OperatingSystem.getJavaLauncherPath() ) );
	}

	public static List<String> forLauncher( Class<?> mainClass ) {
		String launcher = OperatingSystem.getJavaLauncherName();
		if( launcher.startsWith( "java" ) ) return forModule( mainClass );
		return new ArrayList<>( List.of( OperatingSystem.getJavaLauncherPath() ) );
	}

	public static List<String> forLauncher( Parameters parameters, String... extraCommands ) {
		List<String> commands = forLauncher();
		commands.addAll( getParameterCommands( parameters, extraCommands ) );
		return commands;
	}

	public static List<String> forModule() {
		String modulePath = System.getProperty( "jdk.module.path" );
		String mainModule = System.getProperty( "jdk.module.main" );
		String mainClass = System.getProperty( "jdk.module.main.class" );
		return forModule( null, modulePath, mainModule, mainClass );
	}

	public static List<String> forModule( Class<?> source ) {
		String modulePath = System.getProperty( "jdk.module.path" );
		String mainModule = source.getModule().getName();
		String mainClass = source.getName();
		return forModule( null, modulePath, mainModule, mainClass );
	}

	public static List<String> forModule( Parameters parameters, String... extraCommands ) {
		List<String> commands = forModule();
		commands.addAll( getParameterCommands( parameters, extraCommands ) );
		return commands;
	}

	static List<String> forModule(
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
		Parameters allParameters = Parameters.create().add( parameters ).add( Parameters.parse( extraCommands ) );

		// Collect program flags
		Map<String, List<String>> flags = new HashMap<>();
		for( String name : allParameters.getFlags() ) {
			flags.put( name, allParameters.getValues( name ) );
		}

		List<String> commands = new ArrayList<>();

		// Add the collected flags
		for( String flag : flags.keySet() ) {
			List<String> values = flags.get( flag );
			commands.add( flag );
			if( values.size() > 1 || !"true".equals( values.get( 0 ) ) ) commands.addAll( values );
		}

		// Add the collected URIs
		List<String> uris = allParameters.getUris();
		if( !uris.isEmpty() ) commands.addAll( uris );

		return commands;
	}

}
