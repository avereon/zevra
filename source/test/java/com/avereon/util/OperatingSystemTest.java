package com.avereon.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

class OperatingSystemTest {

	private static final String WINDOWS_USER_DATA = "C:\\Users\\user\\AppData\\Roaming";

	private static final String MACOSX_USER_DATA = "/home/user/Library/Application Support";

	private static final String UNIX_USER_DATA = "/home/user/.config";

	private static final String WINDOWS_SHARED_DATA = "C:\\ProgramData";

	private static final String MACOSX_SHARED_DATA = "/Library/Application Support";

	private static final String UNIX_SHARED_DATA = "/usr/local/share/data";

	@BeforeEach
	void setup() {
		System.clearProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY );
		System.clearProperty( OperatingSystem.CUSTOM_LAUNCHER_PATH );
		System.clearProperty( OperatingSystem.CUSTOM_LAUNCHER_NAME );
		System.clearProperty( OperatingSystem.JPACKAGE_APP_PATH );
	}

	@Test
	void testLinux() {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.isPosix() ).isTrue();
		assertThat( OperatingSystem.isLinux() ).isTrue();
		assertThat( OperatingSystem.isMac() ).isFalse();
		assertThat( OperatingSystem.isUnix() ).isTrue();
		assertThat( OperatingSystem.isWindows() ).isFalse();
		assertThat( OperatingSystem.getVersion() ).isEqualTo( "2.6.32_45" );
		assertThat( OperatingSystem.getSystemArchitecture() ).isEqualTo( "x86_64" );
		assertThat( OperatingSystem.getFamily() ).isEqualTo( OperatingSystem.Family.LINUX );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "java" );
		assertThat( OperatingSystem.getProvider() ).isEqualTo( "Community" );
		assertThat( OperatingSystem.getExeSuffix() ).isEmpty();
	}

	@Test
	void testMac() {
		OperatingSystem.init( "Mac OS X", "ppc", "10", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.isPosix() ).isTrue();
		assertThat( OperatingSystem.isLinux() ).isFalse();
		assertThat( OperatingSystem.isMac() ).isTrue();
		assertThat( OperatingSystem.isUnix() ).isTrue();
		assertThat( OperatingSystem.isWindows() ).isFalse();
		assertThat( OperatingSystem.getVersion() ).isEqualTo( "10" );
		assertThat( OperatingSystem.getSystemArchitecture() ).isEqualTo( "ppc" );
		assertThat( OperatingSystem.getFamily() ).isEqualTo( OperatingSystem.Family.MACOSX );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "java" );
		assertThat( OperatingSystem.getProvider() ).isEqualTo( "Apple" );
		assertThat( OperatingSystem.getExeSuffix() ).isEmpty();

		// Test the process launch workaround
		assertThat( System.getProperty( "jdk.lang.Process.launchMechanism" ) ).isEqualTo( "FORK" );
	}

	@Test
	void testWindows7() {
		OperatingSystem.init( "Windows 7", "x86", "6.1", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.isPosix() ).isFalse();
		assertThat( OperatingSystem.isLinux() ).isFalse();
		assertThat( OperatingSystem.isMac() ).isFalse();
		assertThat( OperatingSystem.isUnix() ).isFalse();
		assertThat( OperatingSystem.isWindows() ).isTrue();
		assertThat( OperatingSystem.getVersion() ).isEqualTo( "6.1" );
		assertThat( OperatingSystem.getSystemArchitecture() ).isEqualTo( "x86" );
		assertThat( OperatingSystem.getFamily() ).isEqualTo( OperatingSystem.Family.WINDOWS );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "javaw.exe" );
		assertThat( OperatingSystem.getProvider() ).isEqualTo( "Microsoft" );
		assertThat( OperatingSystem.getExeSuffix() ).isEqualTo( ".exe" );
	}

	@Test
	void testWindows8() {
		OperatingSystem.init( "Windows 8", "x86", "6.2", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.isPosix() ).isFalse();
		assertThat( OperatingSystem.isLinux() ).isFalse();
		assertThat( OperatingSystem.isMac() ).isFalse();
		assertThat( OperatingSystem.isUnix() ).isFalse();
		assertThat( OperatingSystem.isWindows() ).isTrue();
		assertThat( OperatingSystem.getVersion() ).isEqualTo( "6.2" );
		assertThat( OperatingSystem.getSystemArchitecture() ).isEqualTo( "x86" );
		assertThat( OperatingSystem.getFamily() ).isEqualTo( OperatingSystem.Family.WINDOWS );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "javaw.exe" );
		assertThat( OperatingSystem.getProvider() ).isEqualTo( "Microsoft" );
		assertThat( OperatingSystem.getExeSuffix() ).isEqualTo( ".exe" );
	}

	@Test
	void testWindows8_1() {
		OperatingSystem.init( "Windows 8.1", "x86", "6.3", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.isPosix() ).isFalse();
		assertThat( OperatingSystem.isLinux() ).isFalse();
		assertThat( OperatingSystem.isMac() ).isFalse();
		assertThat( OperatingSystem.isUnix() ).isFalse();
		assertThat( OperatingSystem.isWindows() ).isTrue();
		assertThat( OperatingSystem.getVersion() ).isEqualTo( "6.3" );
		assertThat( OperatingSystem.getSystemArchitecture() ).isEqualTo( "x86" );
		assertThat( OperatingSystem.getFamily() ).isEqualTo( OperatingSystem.Family.WINDOWS );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "javaw.exe" );
		assertThat( OperatingSystem.getProvider() ).isEqualTo( "Microsoft" );
		assertThat( OperatingSystem.getExeSuffix() ).isEqualTo( ".exe" );
	}

	@Test
	void testWindows10() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.isPosix() ).isFalse();
		assertThat( OperatingSystem.isLinux() ).isFalse();
		assertThat( OperatingSystem.isMac() ).isFalse();
		assertThat( OperatingSystem.isUnix() ).isFalse();
		assertThat( OperatingSystem.isWindows() ).isTrue();
		assertThat( OperatingSystem.getVersion() ).isEqualTo( "10.0" );
		assertThat( OperatingSystem.getSystemArchitecture() ).isEqualTo( "x86" );
		assertThat( OperatingSystem.getFamily() ).isEqualTo( OperatingSystem.Family.WINDOWS );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "javaw.exe" );
		assertThat( OperatingSystem.getProvider() ).isEqualTo( "Microsoft" );
		assertThat( OperatingSystem.getExeSuffix() ).isEqualTo( ".exe" );
	}

	@Test
	void testWindows11() {
		OperatingSystem.init( "Microsoft Windows 11 Pro", "x86", "10.0.22621", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.isPosix() ).isFalse();
		assertThat( OperatingSystem.isLinux() ).isFalse();
		assertThat( OperatingSystem.isMac() ).isFalse();
		assertThat( OperatingSystem.isUnix() ).isFalse();
		assertThat( OperatingSystem.isWindows() ).isTrue();
		assertThat( OperatingSystem.getVersion() ).isEqualTo( "10.0.22621" );
		assertThat( OperatingSystem.getSystemArchitecture() ).isEqualTo( "x86" );
		assertThat( OperatingSystem.getFamily() ).isEqualTo( OperatingSystem.Family.WINDOWS );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "javaw.exe" );
		assertThat( OperatingSystem.getProvider() ).isEqualTo( "Microsoft" );
		assertThat( OperatingSystem.getExeSuffix() ).isEqualTo( ".exe" );
	}

	@Test
	void testIsProcessElevatedMac() {
		OperatingSystem.init( "Mac OS X", "ppc", "10", UNIX_USER_DATA, UNIX_SHARED_DATA );
		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated() ).isFalse();

		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated() ).isTrue();
	}

	@Test
	void testIsProcessElevatedUnix() {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated() ).isFalse();

		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated() ).isTrue();
	}

	@Test
	void testIsProcessElevatedWindows() {
		OperatingSystem.init( "Windows 7", "x86", "6.1", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		OperatingSystem.clearProcessElevatedFlag();
		assertThat( OperatingSystem.isProcessElevated() ).isFalse();

		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		OperatingSystem.clearProcessElevatedFlag();
		assertThat( OperatingSystem.isProcessElevated() ).isTrue();
	}

	@Test
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	void testElevateProcessMac() throws Exception {
		OperatingSystem.init( "Mac OS X", "ppc", "10", UNIX_USER_DATA, UNIX_SHARED_DATA );

		String program = "vi";
		ProcessBuilder builder = new ProcessBuilder( program );
		File elevate = new File( System.getProperty( "java.io.tmpdir" ), "elevate" );
		elevate.delete();

		OperatingSystem.elevateProcessBuilder( program, builder );

		assertThat( builder.command().get( 0 ) ).isEqualTo( elevate.getCanonicalPath() );
		assertThat( builder.command().get( 1 ) ).isEqualTo( program );
		assertThat( builder.command() ).hasSize( 2 );
		elevate.delete();
	}

	@Test
	void testElevateProcessUnix() throws Exception {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );

		String program = "vi";
		ProcessBuilder builder = new ProcessBuilder( program );
		OperatingSystem.elevateProcessBuilder( program, builder );

		File pkexec = new File( "/usr/bin/pkexec" );
		File gksudo = new File( "/usr/bin/gksudo" );
		File kdesudo = new File( "/usr/bin/kdesudo" );
		if( pkexec.exists() ) {
			assertThat( builder.command().get( 0 ) ).isEqualTo( pkexec.toString() );
			assertThat( builder.command().get( 1 ) ).isEqualTo( program );
			assertThat( builder.command() ).hasSize( 2 );
		} else if( gksudo.exists() ) {
			assertThat( builder.command().get( 0 ) ).isEqualTo( gksudo.toString() );
			assertThat( builder.command().get( 1 ) ).isEqualTo( "-D" );
			assertThat( builder.command().get( 2 ) ).isEqualTo( program );
			assertThat( builder.command().get( 3 ) ).isEqualTo( "--" );
			assertThat( builder.command().get( 4 ) ).isEqualTo( program );
			assertThat( builder.command() ).hasSize( 5 );
		} else if( kdesudo.exists() ) {
			assertThat( builder.command().get( 0 ) ).isEqualTo( kdesudo.toString() );
			assertThat( builder.command().get( 2 ) ).isEqualTo( program );
			assertThat( builder.command() ).hasSize( 3 );
		} else {
			assertThat( builder.command().get( 0 ) ).isEqualTo( "xterm" );
			assertThat( builder.command().get( 1 ) ).isEqualTo( "-title" );
			assertThat( builder.command().get( 2 ) ).isEqualTo( program );
			assertThat( builder.command().get( 3 ) ).isEqualTo( "-e" );
			assertThat( builder.command().get( 4 ) ).isEqualTo( "sudo" );
			assertThat( builder.command().get( 5 ) ).isEqualTo( program );
			assertThat( builder.command() ).hasSize( 6 );
		}
	}

	@Test
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	void testElevateProcessWindows() throws Exception {
		OperatingSystem.init( "Windows 7", "x86", "6.1", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );

		String program = "notepad.exe";
		ProcessBuilder builder = new ProcessBuilder( program );
		File elevate = new File( System.getProperty( "java.io.tmpdir" ), "elevate.js" );
		elevate.delete();

		OperatingSystem.elevateProcessBuilder( "Notepad", builder );

		int index = 0;
		assertThat( builder.command().get( index++ ) ).isEqualTo( "wscript" );
		assertThat( builder.command().get( index++ ) ).isEqualTo( elevate.getCanonicalPath() );
		assertThat( builder.command().get( index++ ) ).isEqualTo( program );
		assertThat( builder.command() ).hasSize( index );

		elevate.delete();
	}

	@Test
	void testReduceProcessMac() throws Exception {
		OperatingSystem.init( "Mac OS X", "ppc", "10", UNIX_USER_DATA, UNIX_SHARED_DATA );
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( "textmate" );

		OperatingSystem.reduceProcessBuilder( builder );

		int index = 0;
		assertThat( builder.command().get( index++ ) ).isEqualTo( "su" );
		assertThat( builder.command().get( index++ ) ).isEqualTo( "-" );
		assertThat( builder.command().get( index++ ) ).isEqualTo( System.getenv( "SUDO_USER" ) );
		assertThat( builder.command().get( index++ ) ).isEqualTo( "--" );
		assertThat( builder.command().get( index++ ) ).isEqualTo( "textmate" );
		assertThat( builder.command() ).hasSize( index );
	}

	@Test
	void testReduceProcessUnix() throws Exception {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( "vi" );

		OperatingSystem.reduceProcessBuilder( builder );

		int index = 0;
		assertThat( builder.command().get( index++ ) ).isEqualTo( "su" );
		assertThat( builder.command().get( index++ ) ).isEqualTo( "-" );
		assertThat( builder.command().get( index++ ) ).isEqualTo( System.getenv( "SUDO_USER" ) );
		assertThat( builder.command().get( index++ ) ).isEqualTo( "--" );
		assertThat( builder.command().get( index++ ) ).isEqualTo( "vi" );
		assertThat( builder.command() ).hasSize( index );
	}

	@Test
	void testReduceProcessWindows() {
		OperatingSystem.init( "Windows 7", "x86", "6.1", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		System.setProperty( OperatingSystem.PROCESS_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( OperatingSystem.getJavaLauncherPath(), "-jar", "C:\\Program Files\\Escape\\program.jar", "-update", "false" );

		IOException exception = null;
		try {
			OperatingSystem.reduceProcessBuilder( builder );
			fail( "Launching a normal processes from an elevated processes in Windows is impossible." );
		} catch( IOException ioexception ) {
			exception = ioexception;
		}

		assertThat( exception ).isNotNull();
	}

	@Test
	void testGetJavaExecutablePath() {
		String java = OperatingSystem.isWindows() ? "javaw.exe" : "java";
		String javaPath = System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + java;
		assertThat( OperatingSystem.getJavaLauncherPath() ).isEqualTo( javaPath );
	}

	@Test
	void testResolveNativeLibPath() {
		OperatingSystem.init( "Windows 8", "x86", "6.2", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.resolveNativeLibPath( "rxtxSerial" ) ).isEqualTo( "win/x86/rxtxSerial.dll" );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.resolveNativeLibPath( "rxtxSerial" ) ).isEqualTo( "linux/x86_64/librxtxSerial.so" );
	}

	@Test
	void testGetUserProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder( "program", "Program" ) ).isEqualTo( Paths.get( WINDOWS_USER_DATA ).resolve( "Program" ) );

		OperatingSystem.init( "Mac OS X", "x86_64", "14", MACOSX_USER_DATA, MACOSX_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder( "program", "Program" ) ).isEqualTo( Paths.get( MACOSX_USER_DATA ).resolve( "Program" ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder( "program", "Program" ) ).isEqualTo( Paths.get( UNIX_USER_DATA ).resolve( "program" ) );
	}

	@Test
	void testBaseGetUserProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder() ).isEqualTo( Paths.get( WINDOWS_USER_DATA ) );

		OperatingSystem.init( "Mac OS X", "x86_64", "14", MACOSX_USER_DATA, MACOSX_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder() ).isEqualTo( Paths.get( MACOSX_USER_DATA ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder() ).isEqualTo( Paths.get( UNIX_USER_DATA ) );
	}

	@Test
	void testGetSharedProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder( "program", "Program" ) ).isEqualTo( Paths.get( WINDOWS_SHARED_DATA ).resolve( "Program" ) );

		OperatingSystem.init( "Mac OS X", "x86_64", "14", MACOSX_USER_DATA, MACOSX_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder( "program", "Program" ) ).isEqualTo( Paths.get( MACOSX_SHARED_DATA ).resolve( "Program" ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder( "program", "Program" ) ).isEqualTo( Paths.get( UNIX_SHARED_DATA ).resolve( "program" ) );
	}

	@Test
	void testGetBaseSharedProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder() ).isEqualTo( Paths.get( WINDOWS_SHARED_DATA ) );

		OperatingSystem.init( "Mac OS X", "x86_64", "14", MACOSX_USER_DATA, MACOSX_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder() ).isEqualTo( Paths.get( MACOSX_SHARED_DATA ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder() ).isEqualTo( Paths.get( UNIX_SHARED_DATA ) );
	}

	@Test
	void testGetJavaLauncherName() {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( OperatingSystem.isWindows() ? "javaw.exe" : "java" );

		System.setProperty( OperatingSystem.CUSTOM_LAUNCHER_NAME, "Mock" );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "Mock" );
	}

	@Test
	void testGetJavaLauncherPath() {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getJavaLauncherPath() ).isEqualTo( System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + OperatingSystem.getJavaLauncherName() );

		System.setProperty( OperatingSystem.CUSTOM_LAUNCHER_PATH, "/this/is/the/launcher/path" );
		System.setProperty( OperatingSystem.CUSTOM_LAUNCHER_NAME, "Mock" );
		assertThat( OperatingSystem.getJavaLauncherPath() ).isEqualTo( "/this/is/the/launcher/path" + File.separator + "Mock" );
	}

	@Test
	void testGetJavaLauncherNameWithJPackageAppPath() {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( OperatingSystem.isWindows() ? "javaw.exe" : "java" );

		System.setProperty( OperatingSystem.JPACKAGE_APP_PATH, "/this/is/the/launcher/path/Mock" );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "Mock" );
	}

	@Test
	void testGetJavaLauncherPathWithJPackageAppPath() {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getJavaLauncherPath() ).isEqualTo( System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + OperatingSystem.getJavaLauncherName() );

		System.setProperty( OperatingSystem.JPACKAGE_APP_PATH, "/this/is/the/launcher/path/Mock" );
		assertThat( OperatingSystem.getJavaLauncherPath() ).isEqualTo( "/this/is/the/launcher/path/Mock" );
	}

	@Test
	void testGetJavaLauncherNameWithWindowsPath() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( OperatingSystem.isWindows() ? "javaw.exe" : "java" );

		String launcherPath = "C:\\Program Files\\Mock\\Mock.exe";
		System.setProperty( OperatingSystem.JPACKAGE_APP_PATH, launcherPath );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( "Mock.exe" );
	}

	@Test
	void testGetJavaLauncherPathWithWindowsPath() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getJavaLauncherName() ).isEqualTo( OperatingSystem.isWindows() ? "javaw.exe" : "java" );

		String launcherPath = "C:\\Program Files\\Mock\\Mock.exe";
		System.setProperty( OperatingSystem.JPACKAGE_APP_PATH, launcherPath );
		assertThat( OperatingSystem.getJavaLauncherPath() ).isEqualTo( "C:\\Program Files\\Mock\\Mock.exe" );
	}

}
