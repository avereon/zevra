package com.xeomar.util;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class OperatingSystemTest {

	@Before
	public void setup() throws Exception {
		System.clearProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY );
	}

	@Test
	public void testLinux() throws Exception {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45" );
		assertThat( OperatingSystem.isLinux(), is( true ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( true ) );
		assertThat( OperatingSystem.isWindows(), is( false ) );
		assertThat( OperatingSystem.getVersion(), is( "2.6.32_45" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86_64" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.LINUX ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "java" ) );
	}

	@Test
	public void testMac() throws Exception {
		OperatingSystem.init( "Mac OS X", "ppc", "10" );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( true ) );
		assertThat( OperatingSystem.isUnix(), is( true ) );
		assertThat( OperatingSystem.isWindows(), is( false ) );
		assertThat( OperatingSystem.getVersion(), is( "10" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "ppc" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.MACOSX ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "java" ) );
	}

	@Test
	public void testWindows7() throws Exception {
		OperatingSystem.init( "Windows 7", "x86", "6.1" );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( false ) );
		assertThat( OperatingSystem.isWindows(), is( true ) );
		assertThat( OperatingSystem.getVersion(), is( "6.1" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.WINDOWS ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "javaw" ) );
	}

	@Test
	public void testWindows8() throws Exception {
		OperatingSystem.init( "Windows 8", "x86", "6.2" );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( false ) );
		assertThat( OperatingSystem.isWindows(), is( true ) );
		assertThat( OperatingSystem.getVersion(), is( "6.2" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.WINDOWS ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "javaw" ) );
	}

	@Test
	public void testWindows8_1() throws Exception {
		OperatingSystem.init( "Windows 8.1", "x86", "6.3" );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( false ) );
		assertThat( OperatingSystem.isWindows(), is( true ) );
		assertThat( OperatingSystem.getVersion(), is( "6.3" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.WINDOWS ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "javaw" ) );
	}

	@Test
	public void testWindows10() throws Exception {
		OperatingSystem.init( "Windows 10", "x86", "10.0" );
		assertThat( OperatingSystem.isLinux(), is( false ) );
		assertThat( OperatingSystem.isMac(), is( false ) );
		assertThat( OperatingSystem.isUnix(), is( false ) );
		assertThat( OperatingSystem.isWindows(), is( true ) );
		assertThat( OperatingSystem.getVersion(), is( "10.0" ) );
		assertThat( OperatingSystem.getSystemArchitecture(), is( "x86" ) );
		assertThat( OperatingSystem.getFamily(), is( OperatingSystem.Family.WINDOWS ) );
		assertThat( OperatingSystem.getJavaExecutableName(), is( "javaw" ) );
	}

	@Test
	public void testIsProcessElevatedMac() throws Exception {
		OperatingSystem.init( "Mac OS X", "ppc", "10" );
		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated(), is( false ) );

		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated(), is( true ) );
	}

	@Test
	public void testIsProcessElevatedUnix() throws Exception {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45" );
		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.NORMAL_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated(), is( false ) );

		OperatingSystem.clearProcessElevatedFlag();
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		assertThat( OperatingSystem.isProcessElevated(), is( true ) );
	}

	@Test
	public void testIsProcessElevatedWindows() throws Exception {
		OperatingSystem.init( "Windows 7", "x86", "6.1" );
		OperatingSystem.clearProcessElevatedFlag();
		assertThat( OperatingSystem.isProcessElevated(), is( false ) );

		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		OperatingSystem.clearProcessElevatedFlag();
		assertThat( OperatingSystem.isProcessElevated(), is( true ) );
	}

	@Test
	public void testElevateProcessMac() throws Exception {
		OperatingSystem.init( "Mac OS X", "ppc", "10" );
		ProcessBuilder builder = new ProcessBuilder( "textmate" );
		File elevate = new File( System.getProperty( "java.io.tmpdir" ), "elevate" );

		OperatingSystem.elevateProcessBuilder( "textmate", builder );
		assertThat( builder.command().get( 0 ), is( elevate.getCanonicalPath() ) );
		assertThat( builder.command().get( 1 ), is( "textmate" ) );
		assertThat( builder.command().size(), is( 2 ) );
	}

	@Test
	public void testElevateProcessUnix() throws Exception {
		String program = "vi";
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45" );
		ProcessBuilder builder = new ProcessBuilder( program );
		OperatingSystem.elevateProcessBuilder( program, builder );

		File gksudo = new File( "/usr/bin/gksudo" );
		File kdesudo = new File( "/usr/bin/kdesudo" );
		if( gksudo.exists() ) {
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
	public void testElevateProcessWindows() throws Exception {
		OperatingSystem.init( "Windows 7", "x86", "6.1" );
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
	public void testReduceProcessMac() throws Exception {
		OperatingSystem.init( "Mac OS X", "ppc", "10" );
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
	public void testReduceProcessUnix() throws Exception {
		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45" );
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
	public void testReduceProcessWindows() throws Exception {
		OperatingSystem.init( "Windows 7", "x86", "6.1" );
		System.setProperty( OperatingSystem.ELEVATED_PRIVILEGE_KEY, OperatingSystem.ELEVATED_PRIVILEGE_VALUE );
		ProcessBuilder builder = new ProcessBuilder( OperatingSystem.getJavaExecutablePath(), "-jar", "C:\\Program Files\\Escape\\program.jar", "-update", "false" );

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
	public void testGetJavaExecutablePath() {
		String java = OperatingSystem.isWindows() ? "javaw" : "java";
		String javaPath = System.getProperty( "java.home" ) + File.separator + "bin" + File.separator + java;
		assertThat( OperatingSystem.getJavaExecutablePath(), is( javaPath ) );
	}

	@Test
	public void testResolveNativeLibPath() throws Exception {
		OperatingSystem.init( "Windows 8", "x86", "6.2" );
		assertThat( OperatingSystem.resolveNativeLibPath( "rxtxSerial" ), is( "win/x86/rxtxSerial.dll" ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45" );
		assertThat( OperatingSystem.resolveNativeLibPath( "rxtxSerial" ), is( "linux/x86_64/librxtxSerial.so" ) );
	}

	@Test
	public void testGetUserProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", "C:\\Users\\user\\AppData\\Roaming", null );
		assertThat( OperatingSystem.getUserProgramDataFolder(), is( Paths.get( "C:/Users/user/AppData/Roaming" ) ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", "/home/user/.config", null );
		assertThat( OperatingSystem.getUserProgramDataFolder(), is( Paths.get( "/home/user/.config" ) ) );
	}

	@Test
	public void testGetSharedProgramDataFolder() {
		OperatingSystem.init( "Windows 10", "x86", "10.0", null, "C:\\ProgramData" );
		assertThat( OperatingSystem.getSharedProgramDataFolder(), is( Paths.get( "C:/ProgramData" ) ) );

		OperatingSystem.init( "Linux", "x86_64", "2.6.32_45", null, "/usr/local/share/data" );
		assertThat( OperatingSystem.getSharedProgramDataFolder(), is( Paths.get( "/usr/local/share/data" ) ) );
	}

}
