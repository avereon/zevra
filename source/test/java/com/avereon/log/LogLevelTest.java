package com.avereon.log;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;

public class LogLevelTest {

	@Test
	void testNONE() {
		assertThat( LogLevel.NONE.getName()).isEqualTo( "NONE" );
		assertThat( LogLevel.NONE.intValue()).isEqualTo( Level.OFF.intValue() );
	}

	@Test
	void testERROR() {
		assertThat( LogLevel.ERROR.getName()).isEqualTo( "ERROR" );
		assertThat( LogLevel.ERROR.intValue()).isEqualTo( Level.SEVERE.intValue() );
	}

	@Test
	void testWARN() {
		assertThat( LogLevel.WARN.getName()).isEqualTo( "WARN" );
		assertThat( LogLevel.WARN.intValue()).isEqualTo( Level.WARNING.intValue() );
	}

	@Test
	void testINFO() {
		assertThat( LogLevel.INFO.getName()).isEqualTo( "INFO" );
		assertThat( LogLevel.INFO.intValue()).isEqualTo( Level.INFO.intValue() );
	}

	@Test
	void testDEBUG() {
		assertThat( LogLevel.DEBUG.getName()).isEqualTo( "DEBUG" );
		assertThat( LogLevel.DEBUG.intValue()).isEqualTo( Level.FINE.intValue() );
	}

	@Test
	void testTRACE() {
		assertThat( LogLevel.TRACE.getName()).isEqualTo( "TRACE" );
		assertThat( LogLevel.TRACE.intValue()).isEqualTo( Level.FINEST.intValue() );
	}

	@Test
	void testALL() {
		assertThat( LogLevel.ALL.getName()).isEqualTo( "ALL" );
		assertThat( LogLevel.ALL.intValue()).isEqualTo( Level.ALL.intValue() );
	}

}
