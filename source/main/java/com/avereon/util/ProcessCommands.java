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

	/**
	 * Returns the command line representation of the current command.
	 * This method converts the command line list returned by the getCommandLine()
	 * method into a single string by joining the elements with a space delimiter.
	 *
	 * @return The command line as a string.
	 */
	public static String getCommandLineAsString() {
		return TextUtil.toString( getCommandLine(), " " );
	}

	/**
	 * Returns the command line representation of the current command. This method
	 * retrieves the Java launcher path using the {@link OperatingSystem#getJavaLauncherPath()}
	 * method and retrieves the input arguments using {@link RuntimeMXBean#getInputArguments()}.
	 * It then creates a list of strings consisting of the Java launcher path followed by the input arguments.
	 *
	 * @return The command line represented as a list of strings.
	 */
	public static List<String> getCommandLine() {
		List<String> commands = new ArrayList<>();
		commands.add( OperatingSystem.getJavaLauncherPath() );
		commands.addAll( ManagementFactory.getRuntimeMXBean().getInputArguments() );
		return commands;
	}

	/**
	 * Retrieves the command line representation for the launcher.
	 * If the Java launcher name starts with "java", it calls the forModule()
	 * method to get the command line for the module. Otherwise, it returns a list
	 * containing the Java launcher path.
	 *
	 * @return The command line represented as a list of strings.
	 */
	public static List<String> forLauncher() {
		String launcher = OperatingSystem.getJavaLauncherName();
		if( launcher.startsWith( "java" ) ) return forModule();
		return new ArrayList<>( List.of( OperatingSystem.getJavaLauncherPath() ) );
	}

	/**
	 * Retrieves the command line representation for the launcher.
	 * If the Java launcher name starts with "java", it calls the forModule()
	 * method to get the command line for the module. Otherwise, it returns a list
	 * containing the Java launcher path.
	 *
	 * @param mainClass The main class to launch.
	 * @return The command line represented as a list of strings.
	 */
	public static List<String> forLauncher( Class<?> mainClass ) {
		String launcher = OperatingSystem.getJavaLauncherName();
		if( launcher.startsWith( "java" ) ) return forModule( mainClass );
		return new ArrayList<>( List.of( OperatingSystem.getJavaLauncherPath() ) );
	}

	/**
	 * Generates the command line representation for the launcher using the
	 * specified parameters. This method combines the default commands generated
	 * by the {@link #forLauncher()} method with any extra commands provided.
	 *
	 * @param parameters The parameters to be included in the command line.
	 * @param extraCommands Additional commands to be included in the command line.
	 * @return The command line represented as a list of strings.
	 */
	public static List<String> forLauncher( Parameters parameters, String... extraCommands ) {
		List<String> commands = forLauncher();
		commands.addAll( getParameterCommands( parameters, extraCommands ) );
		return commands;
	}

	/**
	 * Retrieves the command line representation for the current module. This
	 * method uses the system properties <code>jdk.module.path</code> and
	 * <code>jdk.module.main.class</code> to determine the module path and main
	 * module class. It calls the private
	 * {@link #forModule(String, String, String, String)} method to get the
	 * command line representation.
	 *
	 * @return The command line represented as a list of strings.
	 */
	public static List<String> forModule() {
		String modulePath = System.getProperty( "jdk.module.path" );
		String mainModule = System.getProperty( "jdk.module.main" );
		String mainClass = System.getProperty( "jdk.module.main.class" );
		return forModule( null, modulePath, mainModule, mainClass );
	}

	/**
	 * Generates the command line representation for a specific module.
	 * It constructs a list of strings representing the command line arguments
	 * required to run the module specified by the source class.
	 *
	 * @param source The source class representing the main module class.
	 * @return The command line represented as a list of strings.
	 */
	public static List<String> forModule( Class<?> source ) {
		String modulePath = System.getProperty( "jdk.module.path" );
		String mainModule = source.getModule().getName();
		String mainClass = source.getName();
		return forModule( null, modulePath, mainModule, mainClass );
	}

	/**
	 * Generates the command line representation for a specific module using the
	 * specified parameters and extra commands.
	 *
	 * @param parameters The parameters to be included in the command line.
	 * @param extraCommands Additional commands to be included in the command line.
	 * @return The command line represented as a list of strings.
	 */
	public static List<String> forModule( Parameters parameters, String... extraCommands ) {
		List<String> commands = forModule();
		commands.addAll( getParameterCommands( parameters, extraCommands ) );
		return commands;
	}

	/**
	 * Generates the command line representation for a specific module using the
	 * specified parameters and extra commands.
	 *
	 * @param javaExecutablePath The path to the Java executable.
	 * @param modulePath The path to the module.
	 * @param mainModule The main module.
	 * @param mainClass The main class.
	 * @param parameters The parameters to be included in the command line.
	 * @param extraCommands Additional commands to be included in the command line.
	 * @return The command line represented as a list of strings.
	 */
	static List<String> forModule(
		String javaExecutablePath, String modulePath, String mainModule, String mainClass, Parameters parameters, String... extraCommands
	) {
		List<String> commands = forModule( javaExecutablePath, modulePath, mainModule, mainClass );
		commands.addAll( getParameterCommands( parameters, extraCommands ) );
		return commands;
	}

	/**
	 * Generates the command line representation for a specific module.
	 *
	 * @param javaLauncherPath The path to the Java launcher executable.
	 * @param modulePath The path to the module.
	 * @param mainModule The main module.
	 * @param mainClass The main class.
	 * @return The command line represented as a list of strings.
	 * @throws NullPointerException If mainModule or mainClass is null.
	 */
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

	/**
	 * Retrieves the parameter commands for the given parameters and extra commands.
	 *
	 * @param parameters The parameters to be included in the command line.
	 * @param extraCommands Additional commands to be included in the command line.
	 * @return The list of parameter commands.
	 */
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
