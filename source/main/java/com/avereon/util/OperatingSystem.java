package com.avereon.util;

import lombok.CustomLog;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@CustomLog
public class OperatingSystem {

	public enum Family {
		UNKNOWN,
		LINUX,
		UNIX,
		WINDOWS,
		OS2,
		MACOSX,
		MAC
	}

	public enum Architecture {
		UNKNOWN,
		X86,
		X64,
		PPC
	}

	public static String PROCESS_PRIVILEGE_KEY = OperatingSystem.class.getName() + ":process-privilege-key";

	public static String NORMAL_PRIVILEGE_VALUE = OperatingSystem.class.getName() + ":process-privilege-normal";

	public static String ELEVATED_PRIVILEGE_VALUE = OperatingSystem.class.getName() + ":process-privilege-elevated";

	private static Architecture architecture;

	private static Family family;

	private static String version;

	private static String name;

	private static String arch;

	private static Boolean elevated;

	private static boolean fileSystemCaseSensitive;

	private static Path userProgramDataFolder;

	private static Path sharedProgramDataFolder;

	/*
	 * Initialize the class.
	 */
	static {
		reset();
	}

	public static void reset() {
		init( System.getProperty( "os.name" ), System.getProperty( "os.arch" ), null, null, null );
	}

	public static String getName() {
		return name;
	}

	public static Family getFamily() {
		return family;
	}

	public static String getProvider() {
		switch( family ) {
			case WINDOWS: {
				return "Microsoft";
			}
			case MAC:
			case MACOSX: {
				return "Apple";
			}
			case LINUX: {
				return "Community";
			}
			case OS2: {
				return "IBM";
			}
			default: {
				return "Unknown";
			}
		}
	}

	public static String getVersion() {
		return version;
	}

	public static Architecture getArchitecture() {
		return architecture;
	}

	public static String getSystemArchitecture() {
		return arch;
	}

	public static boolean isPosix() {
		return family == Family.LINUX || family == Family.MACOSX || family == Family.UNIX;
	}

	public static boolean isLinux() {
		return family == Family.LINUX;
	}

	public static boolean isMac() {
		return family == Family.MACOSX;
	}

	public static boolean isUnix() {
		return family == Family.LINUX || family == Family.MACOSX || family == Family.UNIX;
	}

	public static boolean isWindows() {
		return family == Family.WINDOWS;
	}

	/**
	 * Check if the process has elevated privileges.
	 *
	 * @return true if the process has elevated privileges.
	 */
	public static boolean isProcessElevated() {
		String override = System.getProperty( PROCESS_PRIVILEGE_KEY );
		if( override == null ) override = System.getenv( PROCESS_PRIVILEGE_KEY );
		if( ELEVATED_PRIVILEGE_VALUE.equals( override ) ) elevated = Boolean.TRUE;
		if( NORMAL_PRIVILEGE_VALUE.equals( override ) ) elevated = Boolean.FALSE;
		return elevated == null ? isAdminUser() : elevated;
	}

	/**
	 * Determine if user has elevated privileges.
	 *
	 * @return true if user has elevated privileges.
	 */
	public static boolean isAdminUser() {
		if( isWindows() ) {
			try {
				Process process = Runtime.getRuntime().exec( "reg query \"HKU\\S-1-5-19\"" );
				process.waitFor();
				return (process.exitValue() == 0);
			} catch( Exception exception ) {
				return canWriteToProgramFiles();
			}
		}
		try {
			Process process = Runtime.getRuntime().exec( "id -u" );
			process.waitFor();

			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
			return bufferedReader.readLine().equals( "0" );
		} catch( Exception exception ) {
			return System.getProperty( "user.name" ).equals( "root" );
		}
	}

	public static boolean isElevateProcessSupported() {
		return OperatingSystem.isMac() || OperatingSystem.isUnix() || OperatingSystem.isWindows();
	}

	public static boolean isReduceProcessSupported() {
		return OperatingSystem.isMac() || OperatingSystem.isUnix();
	}

	/**
	 * Test the file system for case sensitivity.
	 */
	public static boolean isFileSystemCaseSensitive() {
		return fileSystemCaseSensitive;
	}

	public static Process startProcessElevated( String title, ProcessBuilder builder ) throws IOException {
		if( !OperatingSystem.isProcessElevated() ) elevateProcessBuilder( title, builder );
		return builder.start();
	}

	public static Process startProcessReduced( ProcessBuilder builder ) throws IOException {
		if( OperatingSystem.isProcessElevated() ) reduceProcessBuilder( builder );
		return builder.start();
	}

	/**
	 * Modify the process builder to attempt to elevate the process privileges when the process is started. The returned ProcessBuilder should not be modified
	 * after this call to avoid problems even though this cannot be enforced.
	 *
	 * @param title The name of the program requesting elevated privileges
	 * @param builder The process builder
	 * @return The process builder with elevate privilege commands
	 * @throws IOException if an error occurs
	 */
	public static ProcessBuilder elevateProcessBuilder( String title, ProcessBuilder builder ) throws IOException {
		List<String> command = getElevateCommands( title );
		command.addAll( builder.command() );
		builder.command( command );
		builder.environment().put( PROCESS_PRIVILEGE_KEY, ELEVATED_PRIVILEGE_VALUE );
		return builder;
	}

	/**
	 * Modify the process builder to reduce the process privileges when the process is started. The returned ProcessBuilder should not be modified after this call
	 * to avoid problems even though this cannot be enforced.
	 *
	 * @param builder The process builder
	 * @return The process builder with reduce privilege commands
	 * @throws IOException if an error occurs
	 */
	public static ProcessBuilder reduceProcessBuilder( ProcessBuilder builder ) throws IOException {
		List<String> command = getReduceCommands();

		if( isWindows() ) {
			// See the following links for further information:
			// http://stackoverflow.com/questions/2414991/how-to-launch-a-program-as-as-a-normal-user-from-a-uac-elevated-installer (comment 2 in answer)
			// http://mdb-blog.blogspot.com/2013/01/nsis-lunch-program-as-user-from-uac.html
			throw new IOException( "Launching a normal processes from an elevated processes is impossible in Windows." );
		} else {
			command.addAll( builder.command() );
			builder.command( command );
		}

		builder.command( command );

		return builder;
	}

	public static String getJavaLauncherName() {
		String launcherName;

		String customLauncherName = System.getProperty( "java.launcher.name" );
		launcherName = Objects.requireNonNullElseGet( customLauncherName, () -> isWindows() ? "javaw" : "java" );

		return launcherName + getExeSuffix();
	}

	/**
	 * Get the Java VM launcher path. Prior to Java 14 this returns the official
	 * java launcher that comes with the runtime. Starting with Java 14, if the
	 * java.launcher.path (set by the launcher) and the java.launcher.name (set
	 * by the application) are both set, then this returns the path to the
	 * custom launcher.
	 *
	 * @return The Java VM launcher path
	 */
	public static String getJavaLauncherPath() {
		// Custom launcher
		String launcherPath = System.getProperty( "java.launcher.path" );
		if( launcherPath != null ) return launcherPath + File.separator + getJavaLauncherName();

		// JPackage launcher
		if( System.getProperty( "jpackage.app-path" ) != null ) return System.getProperty( "jpackage.app-path" );

		// Official launcher
		return System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + getJavaLauncherName();
	}

	/**
	 * Returns the total system memory in bytes or -1 if it cannot be determined.
	 *
	 * @return The total system memory in bytes or -1 if it cannot be determined.
	 */
	@SuppressWarnings( "restriction" )
	public static long getTotalSystemMemory() {
		long memory = -1;
		try {
			memory = ((com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
		} catch( Throwable throwable ) {
			// Intentionally ignore exception.
		}
		return memory;
	}

	/**
	 * Get the program data folder for the operating system. On Windows systems
	 * this is the %APPDATA% location. On other systems this is $HOME.
	 * <p>
	 * Exapmles:
	 * <p>
	 * Windows 7: C:\Users\&lt;username&gt;\AppData\Roaming
	 * <br/> Linux: /home/&lt;username&gt;
	 *
	 * @return
	 */
	public static Path getUserProgramDataFolder() {
		return userProgramDataFolder;
	}

	/**
	 * Get the program data folder for the operating system using the program
	 * identifier and/or name. The program identifier is normally all lower case
	 * with no spaces. The name can be mixed case with spaces. Windows systems
	 * use the name instead of the identifier to generate the program data folder
	 * path.
	 *
	 * @param identifier The program identifier
	 * @param name The program name
	 * @return The user program data folder
	 */
	public static Path getUserProgramDataFolder( String identifier, String name ) {
		switch( family ) {
			case MACOSX:
			case WINDOWS: {
				return getUserProgramDataFolder().resolve( name );
			}
			case LINUX: {
				return getUserProgramDataFolder().resolve( identifier );
			}
			default: {
				return getUserProgramDataFolder().resolve( "." + identifier );
			}
		}
	}

	/**
	 * Get the shared program data folder for the operating system. On Windows
	 * systems this is the %ALLUSERSPROFILE% location. On Linux systems this is
	 * /usr/local/share/data.
	 * <p>
	 * Examples:
	 * <p>
	 * Windows 7: C:/ProgramData/<br/> Linux: /usr/local/share/data/
	 *
	 * @return The shared program data folder
	 */
	public static Path getSharedProgramDataFolder() {
		return sharedProgramDataFolder;
	}

	/**
	 * Get the shared program data folder for the operating system using the
	 * program identifier and/or name. The program identifier is normally all
	 * lower case with no spaces. The name can be mixed case with spaces.
	 * Windows systems use the name instead of the identifier to generate the
	 * program data folder path.
	 *
	 * @param identifier The program identifier
	 * @param name The program name
	 * @return The shared program data folder
	 */
	public static Path getSharedProgramDataFolder( String identifier, String name ) {
		switch( family ) {
			case MACOSX:
			case WINDOWS: {
				return getSharedProgramDataFolder().resolve( name );
			}
			case LINUX: {
				return getSharedProgramDataFolder().resolve( identifier );
			}
			default: {
				return getSharedProgramDataFolder().resolve( "." + identifier );
			}
		}
	}

	public static String resolveNativeLibPath( String libname ) {
		return String.format( "%s/%s/%s", getPlatformFolder(), getArchitectureFolder(), mapLibraryName( libname ) );
	}

	public static String getExeSuffix() {
		return isWindows() ? ".exe" : "";
	}

	static void clearProcessElevatedFlag() {
		elevated = null;
	}

	/**
	 * The init() method is intentionally package private, and separate from the
	 * static initializer, so the initialization logic can be tested.
	 *
	 * @param name The os name from System.getProperty( "os.name" ).
	 * @param arch The os arch from System.getProperty( "os.arch" ).
	 * @param version The os version from System.getProperty( "os.version" ).
	 * @param userData The program user data folder
	 * @param sharedData The program shared data folder
	 */
	static void init( String name, String arch, String version, String userData, String sharedData ) {
		OperatingSystem.name = name;
		OperatingSystem.arch = arch;

		// Determine the OS family
		if( name.contains( "Linux" ) ) {
			family = Family.LINUX;
		} else if( name.contains( "Windows" ) ) {
			family = Family.WINDOWS;
		} else if( name.contains( "OS/2" ) ) {
			family = Family.OS2;
		} else if( name.contains( "SunOS" ) | name.contains( "Solaris" ) | name.contains( "HP-UX" ) | name.contains( "AIX" ) | name.contains( "FreeBSD" ) ) {
			family = Family.UNIX;
		} else if( name.contains( "Mac OS" ) ) {
			if( name.contains( "Mac OS X" ) ) {
				family = Family.MACOSX;
			} else {
				family = Family.MAC;
			}
		} else {
			family = Family.UNKNOWN;
		}

		// Determine the OS architecture
		if( arch.matches( "x86" ) || arch.matches( "i.86" ) ) {
			OperatingSystem.architecture = Architecture.X86;
		} else if( "x86_64".equals( arch ) || "amd64".equals( arch ) ) {
			OperatingSystem.architecture = Architecture.X64;
		} else if( "ppc".equals( arch ) || "PowerPC".equals( arch ) ) {
			OperatingSystem.architecture = Architecture.PPC;
		} else {
			OperatingSystem.architecture = Architecture.UNKNOWN;
		}

		// Store the version
		if( version == null ) {
			switch( family ) {
				case WINDOWS: {
					OperatingSystem.version = getExtendedWindowsVersion();
					break;
				}
				default: {
					OperatingSystem.version = System.getProperty( "os.version" );
				}
			}
		} else {
			OperatingSystem.version = version;
		}

		// Case sensitive file system
		File fileOne = new File( "TeStFiLe" );
		File fileTwo = new File( "tEsTfIlE" );
		fileSystemCaseSensitive = !fileOne.equals( fileTwo );

		// User program data folder
		if( userData == null ) {
			switch( family ) {
				case WINDOWS: {
					userProgramDataFolder = Paths.get( System.getenv( "appdata" ) );
					break;
				}
				case MACOSX: {
					userProgramDataFolder = Paths.get( System.getProperty( "user.home" ), "/Library/Application Support" );
					break;
				}
				case LINUX: {
					userProgramDataFolder = Paths.get( System.getProperty( "user.home" ), ".config" );
					break;
				}
				default: {
					sharedProgramDataFolder = Paths.get( System.getProperty( "user.home" ) );
					break;
				}
			}
		} else {
			userProgramDataFolder = Paths.get( userData );
		}

		// Shared program data folder
		if( sharedData == null ) {
			switch( family ) {
				case WINDOWS: {
					sharedProgramDataFolder = Paths.get( System.getenv( "allusersprofile" ) );
					break;
				}
				case MACOSX: {
					sharedProgramDataFolder = Paths.get( "/Library/Application Support" );
					break;
				}
				case LINUX: {
					sharedProgramDataFolder = Paths.get( "/usr/local/share/data" );
					break;
				}
				default: {
					sharedProgramDataFolder = Paths.get( System.getProperty( "user.home" ) );
					break;
				}
			}
		} else {
			sharedProgramDataFolder = Paths.get( sharedData );
		}

		// Execution workaround
		if( isUnix() ) System.setProperty( "jdk.lang.Process.launchMechanism", "FORK" );
	}

	private static String getExtendedWindowsVersion() {
		try {
			Process process = new ProcessBuilder( "cmd", "/Q", "/C", "ver" ).start();
			BufferedReader reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
			String line;
			while( (line = reader.readLine()) != null ) {
				if( "".equals( line.trim() ) ) continue;
				return line.substring( "Microsoft Windows [Version ".length(), line.length() - 1 );
			}
		} catch( Exception exception ) {
			// Intentionally ignore exception
		}
		return System.getProperty( "os.version" );
	}

	private static String mapLibraryName( String libname ) {
		switch( family ) {
			case LINUX: {
				return "lib" + libname + ".so";
			}
			case MACOSX: {
				return "lib" + libname + ".jnilib";
			}
			case WINDOWS: {
				return libname + ".dll";
			}
			default: {
				return System.mapLibraryName( libname );
			}
		}
	}

	private static String getArchitectureFolder() {
		switch( architecture ) {
			case X86: {
				return "x86";
			}
			case X64: {
				return "x86_64";
			}
			default: {
				return architecture.name().toLowerCase();
			}
		}
	}

	private static String getPlatformFolder() {
		return family == Family.WINDOWS ? "win" : family.name().toLowerCase();
	}

	private static boolean canWriteToProgramFiles() {
		if( !OperatingSystem.isWindows() ) return false;
		try {
			String programFilesFolder = System.getenv( "ProgramFiles" );
			if( programFilesFolder == null ) programFilesFolder = "C:\\Program Files";
			File privilegeCheckFile = new File( programFilesFolder, "privilege.check.txt" );
			return privilegeCheckFile.createNewFile() && privilegeCheckFile.delete();
		} catch( IOException exception ) {
			return false;
		}
	}

	private static List<String> getElevateCommands( String title ) throws IOException {
		List<String> commands = new ArrayList<>();

		if( isMac() ) {
			commands.add( extractMacElevate().getPath() );
		} else if( isUnix() ) {
			File pkexec = new File( "/usr/bin/pkexec" );
			File gksudo = new File( "/usr/bin/gksudo" );
			File kdesudo = new File( "/usr/bin/kdesudo" );
			if( pkexec.exists() ) {
				commands.add( "/usr/bin/pkexec" );
			} else if( gksudo.exists() ) {
				commands.add( "/usr/bin/gksudo" );
				commands.add( "-D" );
				commands.add( title );
				commands.add( "--" );
			} else if( kdesudo.exists() ) {
				commands.add( "/usr/bin/kdesudo" );
				commands.add( "--" );
			} else {
				commands.add( "xterm" );
				commands.add( "-title" );
				commands.add( title );
				commands.add( "-e" );
				commands.add( "sudo" );
			}
		} else if( isWindows() ) {
			commands.add( "wscript" );
			commands.add( extractWinElevate().getPath() );
		}

		return commands;
	}

	private static List<String> getReduceCommands() {
		List<String> commands = new ArrayList<>();

		if( isWindows() ) {
			// NOTE It is not possible to reduce the process privilege on Windows
			//commands.add( "runas" );
			//commands.add( "/trustlevel:0x20000" );
		} else {
			commands.add( "su" );
			commands.add( "-" );
			commands.add( System.getenv( "SUDO_USER" ) );
			commands.add( "--" );
		}

		return commands;
	}

	private static File extractWinElevate() throws IOException {
		return extractWinElevate( "elevate.js" );
	}

	private static File extractWinElevate( String name ) throws IOException {
		InputStream source = OperatingSystem.class.getResourceAsStream( "/elevate/win/elevate.js" );
		File elevator = new File( System.getProperty( "java.io.tmpdir" ), name ).getCanonicalFile();
		return extractElevator( source, elevator );
	}

	private static File extractMacElevate() throws IOException {
		return extractMacElevate( "elevate" );
	}

	private static File extractMacElevate( String name ) throws IOException {
		InputStream source = OperatingSystem.class.getResourceAsStream( "/elevate/mac/elevate" );
		File elevator = new File( System.getProperty( "java.io.tmpdir" ), name ).getCanonicalFile();
		return extractElevator( source, elevator );
	}

	private static File extractElevator( InputStream source, File elevator ) throws IOException {
		try( source; FileOutputStream target = new FileOutputStream( elevator ) ) {
			IoUtil.copy( source, target );
		}
		if( !elevator.setExecutable( true ) ) throw new IOException( "Failed to set execute permission on " + elevator );
		return elevator;
	}

}
