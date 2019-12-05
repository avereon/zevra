package com.avereon.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class OperatingSystemTest {

	private static final String WINDOWS_USER_DATA = "C:\\Users\\user\\AppData\\Roaming";

	private static final String MACOSX_USER_DATA = "/home/user/Library/Application Support";

	private static final String UNIX_USER_DATA = "/home/user/.config";

	private static final String WINDOWS_SHARED_DATA = "C:\\ProgramData";

	private static final String MACOSX_SHARED_DATA = "/Library/Application Support";

	private static final String UNIX_SHARED_DATA = "/usr/local/share/data";

	@BeforeEach
	void setup() {
		System.clearProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY );
	}

	@Test
	void testLinux() {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.isPosix(), is( true ) );
		assertThat( OperatingSystem.isLinux(), is( true ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( true ) );
		assertThat( OperatingSystem.isWindows(), is( false ) );
		assertThat( OperatingSystem.getVersion(), is( "2.6.32_45" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86_64" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.LINUX ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "java" ) );
		assertThat( OperatingSystem.getProvider(), is( "Community" ) );
	}

	@Test
	void testMac() {
		OperatingSystem.init( "Mac OS X", "ppc", "10", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.isPosix(), is( true ) );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( true ) );
		assertThat( OperatingSystem.isUnix(), is( true ) );
		assertThat( OperatingSystem.isWindows(), is( false ) );
		assertThat( OperatingSystem.getVersion(), is( "10" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "ppc" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.MACOSX ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "java" ) );
		assertThat( OperatingSystem.getProvider(), is( "Apple" ) );

		// Test the process launch workaround
		assertThat( System.getProperty( "jdk.lang.Process.launchMechanism" ), is( "FORK" ) );
	}

	@Test
	void testWindows7() {
		OperatingSystem.init( "Windows 7", "x86", "6.1", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.isPosix(), is( false ) );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( false ) );
		assertThat( OperatingSystem.isWindows(), is( true ) );
		assertThat( OperatingSystem.getVersion(), is( "6.1" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.WINDOWS ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "javaw" ) );
		assertThat( OperatingSystem.getProvider(), is( "Microsoft" ) );
	}

	@Test
	void testWindows8() {
		OperatingSystem.init( "Windows 8", "x86", "6.2", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.isPosix(), is( false ) );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( false ) );
		assertThat( OperatingSystem.isWindows(), is( true ) );
		assertThat( OperatingSystem.getVersion(), is( "6.2" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.WINDOWS ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "javaw" ) );
		assertThat( OperatingSystem.getProvider(), is( "Microsoft" ) );
	}

	@Test
	void testWindows8_1() {
		OperatingSystem.init( "Windows 8.1", "x86", "6.3", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.isPosix(), is( false ) );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( false ) );
		assertThat( OperatingSystem.isWindows(), is( true ) );
		assertThat( OperatingSystem.getVersion(), is( "6.3" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.WINDOWS ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "javaw" ) );
		assertThat( OperatingSystem.getProvider(), is( "Microsoft" ) );
	}

	@Test
	void testWindows10() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.isPosix(), is( false ) );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( false ) );
		assertThat( OperatingSystem.isWindows(), is( true ) );
		assertThat( OperatingSystem.getVersion(), is( "10.0" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.WINDOWS ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "javaw" ) );
		assertThat( OperatingSystem.getProvider(), is( "Microsoft" ) );
	}

	@Test
	void testIsProcessElevatedMac() {
		OperatingSystem.init( "Mac OS X", "ppc", "10", UNIX_USER_DATA, UNIX_SHARED_DATA );
		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated(), is( false ) );

		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated(), is( true ) );
	}

	@Test
	void testIsProcessElevatedUnix() {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated(), is( false ) );

		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated(), is( true ) );
	}

	@Test
	void testIsProcessElevatedWindows() {
		OperatingSystem.init( "Windows 7", "x86", "6.1", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		OperatingSystem.clearProcessElevatedFlag();
		assertThat( OperatingSystem.isProcessElevated(), is( false ) );

		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		OperatingSystem.clearProcessElevatedFlag();
		assertThat( OperatingSystem.isProcessElevated(), is( true ) );
	}

	@Test
	void testElevateProcessMac() throws Exception {
		OperatingSystem.init( "Mac OS X", "ppc", "10", UNIX_USER_DATA, UNIX_SHARED_DATA );

		String programName = "Zevra";
		ProcessBuilder builder = new ProcessBuilder( programName );
		File elevate = new File( System.getProperty( "java.io.tmpdir" ), programName );

		OperatingSystem.elevateProcessBuilder( programName, builder );
		assertThat( builder.command().get( 0 ), is( elevate.getCanonicalPath() ) );
		assertThat( builder.command().get( 1 ), is( programName ) );
		assertThat( builder.command().size(), is( 2 ) );
	}

	@Test
	void testElevateProcessUnix() throws Exception {
		String program = "vi";
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		ProcessBuilder builder = new ProcessBuilder( program );
		OperatingSystem.elevateProcessBuilder( program, builder );

		File pkexec = new File( "/usr/bin/pkexec" );
		File gksudo = new File( "/usr/bin/gksudo" );
		File kdesudo = new File( "/usr/bin/kdesudo" );
		if( pkexec.exists() ) {
			assertThat( builder.command().get( 0 ), is( pkexec.toString() ) );
			assertThat( builder.command().get( 1 ), is( program ) );
			assertThat( builder.command().size(), is( 2 ) );
		} else if( gksudo.exists() ) {
			assertThat( builder.command().get( 0 ), is( gksudo.toString() ) );
			assertThat( builder.command().get( 1 ), is( "-D" ) );
			assertThat( builder.command().get( 2 ), is( program ) );
			assertThat( builder.command().get( 3 ), is( "--" ) );
			assertThat( builder.command().get( 4 ), is( program ) );
			assertThat( builder.command().size(), is( 5 ) );
		} else if( kdesudo.exists() ) {
			assertThat( builder.command().get( 0 ), is( kdesudo.toString() ) );
			assertThat( builder.command().get( 2 ), is( program ) );
			assertThat( builder.command().size(), is( 3 ) );
		} else {
			assertThat( builder.command().get( 0 ), is( "xterm" ) );
			assertThat( builder.command().get( 1 ), is( "-title" ) );
			assertThat( builder.command().get( 2 ), is( program ) );
			assertThat( builder.command().get( 3 ), is( "-e" ) );
			assertThat( builder.command().get( 4 ), is( "sudo" ) );
			assertThat( builder.command().get( 5 ), is( program ) );
			assertThat( builder.command().size(), is( 6 ) );
		}
	}

	@Test
	void testElevateProcessWindows() throws Exception {
		OperatingSystem.init( "Windows 7", "x86", "6.1", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		ProcessBuilder builder = new ProcessBuilder( "notepad.exe" );
		File elevate = new File( System.getProperty( "java.io.tmpdir" ), "elevate.js" );

		OperatingSystem.elevateProcessBuilder( "Notepad", builder );

		int index = 0;
		assertThat( builder.command().get( index++ ), is( "wscript" ) );
		assertThat( builder.command().get( index++ ), is( elevate.getCanonicalPath() ) );
		assertThat( builder.command().get( index++ ), is( "notepad.exe" ) );
		assertThat( builder.command().size(), is( index ) );
	}

	@Test
	void testReduceProcessMac() throws Exception {
		OperatingSystem.init( "Mac OS X", "ppc", "10", UNIX_USER_DATA, UNIX_SHARED_DATA );
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( "textmate" );

		OperatingSystem.reduceProcessBuilder( builder );

		int index = 0;
		assertThat( builder.command().get( index++ ), is( "su" ) );
		assertThat( builder.command().get( index++ ), is( "-" ) );
		assertThat( builder.command().get( index++ ), is( System.getenv( "SUDO_USER" ) ) );
		assertThat( builder.command().get( index++ ), is( "--" ) );
		assertThat( builder.command().get( index++ ), is( "textmate" ) );
		assertThat( builder.command().size(), is( index ) );
	}

	@Test
	void testReduceProcessUnix() throws Exception {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( "vi" );

		OperatingSystem.reduceProcessBuilder( builder );

		int index = 0;
		assertThat( builder.command().get( index++ ), is( "su" ) );
		assertThat( builder.command().get( index++ ), is( "-" ) );
		assertThat( builder.command().get( index++ ), is( System.getenv( "SUDO_USER" ) ) );
		assertThat( builder.command().get( index++ ), is( "--" ) );
		assertThat( builder.command().get( index++ ), is( "vi" ) );
		assertThat( builder.command().size(), is( index ) );
	}

	@Test
	void testReduceProcessWindows() {
		OperatingSystem.init( "Windows 7", "x86", "6.1", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( OperatingSystem.getJavaExecutablePath(),
			"-jar",
			"C:\\Program Files\\Escape\\program.jar",
			"-update",
			"false"
		);

		IOException exception = null;
		try {
			OperatingSystem.reduceProcessBuilder( builder );
			fail( "Launching a normal processes from an elevated processes in Windows is impossible." );
		} catch( IOException ioexception ) {
			exception = ioexception;
		}

		assertNotNull( exception );
	}

	@Test
	void testGetJavaExecutablePath() {
		String java = OperatingSystem.isWindows() ? "javaw" : "java";
		String javaPath = System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + java;
		assertThat( OperatingSystem.getJavaExecutablePath(), is( javaPath ) );
	}

	@Test
	void testResolveNativeLibPath() {
		OperatingSystem.init( "Windows 8", "x86", "6.2", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.resolveNativeLibPath( "rxtxSerial" ), is( "win/x86/rxtxSerial.dll" ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.resolveNativeLibPath( "rxtxSerial" ), is( "linux/x86_64/librxtxSerial.so" ) );
	}

	@Test
	void testGetUserProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder( "program", "Program" ), is( Paths.get( WINDOWS_USER_DATA ).resolve( "Program" ) ) );

		OperatingSystem.init( "Mac OS X", "x86_64", "14", MACOSX_USER_DATA, MACOSX_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder( "program", "Program" ), is( Paths.get( MACOSX_USER_DATA ).resolve( "Program" ) ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder( "program", "Program" ), is( Paths.get( UNIX_USER_DATA ).resolve( "program" ) ) );
	}

	@Test
	void testBaseGetUserProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder(), is( Paths.get( WINDOWS_USER_DATA ) ) );

		OperatingSystem.init( "Mac OS X", "x86_64", "14", MACOSX_USER_DATA, MACOSX_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder(), is( Paths.get( MACOSX_USER_DATA ) ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getUserProgramDataFolder(), is( Paths.get( UNIX_USER_DATA ) ) );
	}

	@Test
	void testGetSharedProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder( "program", "Program" ), is( Paths.get( WINDOWS_SHARED_DATA ).resolve( "Program" ) ) );

		OperatingSystem.init( "Mac OS X", "x86_64", "14", MACOSX_USER_DATA, MACOSX_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder( "program", "Program" ), is( Paths.get( MACOSX_SHARED_DATA ).resolve( "Program" ) ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder( "program", "Program" ), is( Paths.get( UNIX_SHARED_DATA ).resolve( "program" ) ) );
	}

	@Test
	void testGetBaseSharedProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", WINDOWS_USER_DATA, WINDOWS_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder(), is( Paths.get( WINDOWS_SHARED_DATA ) ) );

		OperatingSystem.init( "Mac OS X", "x86_64", "14", MACOSX_USER_DATA, MACOSX_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder(), is( Paths.get( MACOSX_SHARED_DATA ) ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", UNIX_USER_DATA, UNIX_SHARED_DATA );
		assertThat( OperatingSystem.getSharedProgramDataFolder(), is( Paths.get( UNIX_SHARED_DATA ) ) );
	}

}
