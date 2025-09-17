package com.avereon.product;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class VersionTest {

	private static final String[] VERSIONS_QUALIFIER = { "1-alpha2snapshot", "1-alpha2", "1-alpha-123", "1-beta-2", "1-beta123", "1-m2", "1-m11", "1-rc", "1-cr2", "1-rc123", "1-SNAPSHOT", "1", "1-sp", "1-sp2", "1-sp123", "1-abc", "1-def", "1-pom-1", "1-1-snapshot", "1-1", "1-2", "1-123" };

	private static final String[] VERSIONS_NUMBER = { "2.0", "2-1", "2.0.a", "2.0.0.a", "2.0.2", "2.0.123", "2.1.0", "2.1-a", "2.1b", "2.1-c", "2.1-1", "2.1.0.1", "2.2", "2.123", "11.a2", "11.a11", "11.b2", "11.b11", "11.m2", "11.m11", "11", "11.a", "11b", "11c", "11m" };

	private final Version version = new Version( "3.4.5-b-06" );

	private final Version majorGreater = new Version( "4.4.5-b-06" );

	private final Version majorLesser = new Version( "2.4.5-b-06" );

	private final Version minorGreater = new Version( "3.5.3-b-04" );

	private final Version minorLesser = new Version( "3.3.3-b-04" );

	private final Version microGreater = new Version( "3.4.6-b-04" );

	private final Version microLesser = new Version( "3.4.4-b-04" );

	private final Version revisionGreater = new Version( "3.4.5-u-04" );

	private final Version revisionLesser = new Version( "3.4.5-a-04" );

	private final Version buildGreater = new Version( "3.4.5-b-07" );

	private final Version buildLesser = new Version( "3.4.5-b-05" );

	private final Version alpha = new Version( "3.4.5-a-06" );

	private final Version beta = new Version( "3.4.5-b-06" );

	private final Version release = new Version( "3.4.5-u-00" );

	private final Version update = new Version( "3.4.5-u-06" );

	private final Version snapshot = new Version( "3.4.5-SNAPSHOT" );

	@Test
	void constructor() {
		assertThat( new Version().toHumanString() ).isEqualTo( "UNKNOWN" );
		assertThat( new Version( null ).toHumanString() ).isEqualTo( "UNKNOWN" );
		assertThat( new Version( "" ).toHumanString() ).isEqualTo( "" );
		assertThat( new Version( "1" ).toString() ).isEqualTo( "1" );
	}

	@Test
	void testIsSnapshot() {
		assertThat( (new Version( "1-alpha2snapshot" ).isSnapshot()) ).isTrue();
		assertThat( (new Version( "1-alpha2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-alpha-123" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-beta-2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-beta123" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-m2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-m11" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-rc" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-cr2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-rc123" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-SNAPSHOT" ).isSnapshot()) ).isTrue();
		assertThat( (new Version( "1" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-sp" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-sp2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-sp123" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-abc" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-def" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-pom-1" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-1-snapshot" ).isSnapshot()) ).isTrue();
		assertThat( (new Version( "1-1" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "1-123" ).isSnapshot()) ).isFalse();

		assertThat( (new Version( "2.0" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2-1" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.0.a" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.0.0.a" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.0.2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.0.123" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.1.0" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.1-a" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.1b" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.1-c" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.1-1" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.1.0.1" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "2.123" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11.a2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11.a11" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11.b2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11.b11" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11.m2" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11.m11" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11.a" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11b" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11c" ).isSnapshot()) ).isFalse();
		assertThat( (new Version( "11m" ).isSnapshot()) ).isFalse();
	}

	@Test
	void testHasQulifierAlpha() {
		assertThat( (new Version( "1-alpha2snapshot" ).hasQualifier( "alpha" )) ).isTrue();
		assertThat( (new Version( "1-alpha2" ).hasQualifier( "alpha" )) ).isTrue();
		assertThat( (new Version( "1-alpha-123" ).hasQualifier( "alpha" )) ).isTrue();
		assertThat( (new Version( "1-beta-2" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-beta123" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-m2" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-m11" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-rc" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-cr2" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-rc123" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-SNAPSHOT" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-sp" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-sp2" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-sp123" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-abc" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-def" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-pom-1" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-1-snapshot" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-1" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-2" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "1-123" ).hasQualifier( "alpha" )) ).isFalse();

		assertThat( (new Version( "2.0" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2-1" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2.0.a" ).hasQualifier( "alpha" )) ).isTrue();
		assertThat( (new Version( "2.0.0.a" ).hasQualifier( "alpha" )) ).isTrue();
		assertThat( (new Version( "2.0.2" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2.0.123" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2.1.0" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2.1-a" ).hasQualifier( "alpha" )) ).isTrue();
		assertThat( (new Version( "2.1b" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2.1-c" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2.1-1" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2.1.0.1" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2.2" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "2.123" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "11.a2" ).hasQualifier( "alpha" )) ).isTrue();
		assertThat( (new Version( "11.a11" ).hasQualifier( "alpha" )) ).isTrue();
		assertThat( (new Version( "11.b2" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "11.b11" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "11.m2" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "11.m11" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "11" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "11.a" ).hasQualifier( "alpha" )) ).isTrue();
		assertThat( (new Version( "11b" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "11c" ).hasQualifier( "alpha" )) ).isFalse();
		assertThat( (new Version( "11m" ).hasQualifier( "alpha" )) ).isFalse();
	}

	@Test
	void testHasQualifierBeta() {
		assertThat( (new Version( "1-alpha2snapshot" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-alpha2" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-alpha-123" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-beta-2" ).hasQualifier( "beta" )) ).isTrue();
		assertThat( (new Version( "1-beta123" ).hasQualifier( "beta" )) ).isTrue();
		assertThat( (new Version( "1-m2" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-m11" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-rc" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-cr2" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-rc123" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-SNAPSHOT" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-sp" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-sp2" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-sp123" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-abc" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-def" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-pom-1" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-1-snapshot" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-1" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-2" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "1-123" ).hasQualifier( "beta" )) ).isFalse();

		assertThat( (new Version( "2.0" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2-1" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.0.a" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.0.0.a" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.0.2" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.0.123" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.1.0" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.1-a" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.1b" ).hasQualifier( "beta" )) ).isTrue();
		assertThat( (new Version( "2.1-c" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.1-1" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.1.0.1" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.2" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "2.123" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "11.a2" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "11.a11" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "11.b2" ).hasQualifier( "beta" )) ).isTrue();
		assertThat( (new Version( "11.b11" ).hasQualifier( "beta" )) ).isTrue();
		assertThat( (new Version( "11.m2" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "11.m11" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "11" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "11.a" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "11b" ).hasQualifier( "beta" )) ).isTrue();
		assertThat( (new Version( "11c" ).hasQualifier( "beta" )) ).isFalse();
		assertThat( (new Version( "11m" ).hasQualifier( "beta" )) ).isFalse();
	}

	@Test
	void testCompareVersions() {
		assertThat( Version.compare( version, version ) ).isEqualTo( 0 );

		assertThat( Version.compare( version, majorGreater ) ).isEqualTo( -1 );
		assertThat( Version.compare( version, majorLesser ) ).isEqualTo( 1 );

		assertThat( Version.compare( version, minorGreater ) ).isEqualTo( -1 );
		assertThat( Version.compare( version, minorLesser ) ).isEqualTo( 1 );

		assertThat( Version.compare( version, microGreater ) ).isEqualTo( -1 );
		assertThat( Version.compare( version, microLesser ) ).isEqualTo( 1 );

		assertThat( Version.compare( version, revisionGreater ) ).isEqualTo( -1 );
		assertThat( Version.compare( version, revisionLesser ) ).isEqualTo( 1 );

		assertThat( Version.compare( version, buildGreater ) ).isEqualTo( -1 );
		assertThat( Version.compare( version, buildLesser ) ).isEqualTo( 1 );
	}

	@Test
	void testCompareVersionWithSnapshot() {
		assertThat( Version.compare( version, snapshot ) ).isEqualTo( 1 );
		assertThat( Version.compare( snapshot, version ) ).isEqualTo( -1 );
	}

	@Test
	void testToString() {
		assertThat( new Version( null ).toString() ).isEqualTo( "unknown" );

		assertThat( alpha.toString() ).isEqualTo( "3.4.5-a-06" );
		assertThat( beta.toString() ).isEqualTo( "3.4.5-b-06" );
		assertThat( release.toString() ).isEqualTo( "3.4.5-u-00" );
		assertThat( update.toString() ).isEqualTo( "3.4.5-u-06" );
		assertThat( snapshot.toString() ).isEqualTo( "3.4.5-SNAPSHOT" );

		for( String string : VERSIONS_NUMBER ) {
			assertThat( new Version( string ).toString() ).isEqualTo( string );
		}
		for( String string : VERSIONS_QUALIFIER ) {
			assertThat( new Version( string ).toString() ).isEqualTo( string );
		}
	}

	@Test
	void testToHumanString() {
		assertThat( new Version( "2.0" ).toHumanString() ).isEqualTo( "2.0" );
		assertThat( new Version( "2-1" ).toHumanString() ).isEqualTo( "2-1" );
		assertThat( new Version( "2.0.a" ).toHumanString() ).isEqualTo( "2.0 Alpha" );
		assertThat( new Version( "2.0.0.a" ).toHumanString() ).isEqualTo( "2.0.0 Alpha" );
		assertThat( new Version( "2.0.2" ).toHumanString() ).isEqualTo( "2.0.2" );
		assertThat( new Version( "2.0.123" ).toHumanString() ).isEqualTo( "2.0.123" );
		assertThat( new Version( "2.1.0" ).toHumanString() ).isEqualTo( "2.1.0" );
		assertThat( new Version( "2.1-a" ).toHumanString() ).isEqualTo( "2.1 Alpha" );
		assertThat( new Version( "2.1b" ).toHumanString() ).isEqualTo( "2.1 Beta" );
		assertThat( new Version( "2.1-c" ).toHumanString() ).isEqualTo( "2.1 c" );
		assertThat( new Version( "2.1-1" ).toHumanString() ).isEqualTo( "2.1-1" );
		assertThat( new Version( "2.1.0.1" ).toHumanString() ).isEqualTo( "2.1.0.1" );
		assertThat( new Version( "2.2" ).toHumanString() ).isEqualTo( "2.2" );
		assertThat( new Version( "2.123" ).toHumanString() ).isEqualTo( "2.123" );
		assertThat( new Version( "11.a2" ).toHumanString() ).isEqualTo( "11 Alpha 2" );
		assertThat( new Version( "11.a11" ).toHumanString() ).isEqualTo( "11 Alpha 11" );
		assertThat( new Version( "11.b2" ).toHumanString() ).isEqualTo( "11 Beta 2" );
		assertThat( new Version( "11.b11" ).toHumanString() ).isEqualTo( "11 Beta 11" );
		assertThat( new Version( "11.m2" ).toHumanString() ).isEqualTo( "11 Milestone 2" );
		assertThat( new Version( "11.m11" ).toHumanString() ).isEqualTo( "11 Milestone 11" );
		assertThat( new Version( "11" ).toHumanString() ).isEqualTo( "11" );
		assertThat( new Version( "11.a" ).toHumanString() ).isEqualTo( "11 Alpha" );
		assertThat( new Version( "11b" ).toHumanString() ).isEqualTo( "11 Beta" );
		assertThat( new Version( "11c" ).toHumanString() ).isEqualTo( "11 c" );
		assertThat( new Version( "11m" ).toHumanString() ).isEqualTo( "11 Milestone" );

		assertThat( new Version( "1-alpha2snapshot" ).toHumanString() ).isEqualTo( "1 Alpha 2 SNAPSHOT" );
		assertThat( new Version( "1-alpha2" ).toHumanString() ).isEqualTo( "1 Alpha 2" );
		assertThat( new Version( "1-alpha-123" ).toHumanString() ).isEqualTo( "1 Alpha 123" );
		assertThat( new Version( "1-beta-2" ).toHumanString() ).isEqualTo( "1 Beta 2" );
		assertThat( new Version( "1-beta123" ).toHumanString() ).isEqualTo( "1 Beta 123" );
		assertThat( new Version( "1-m2" ).toHumanString() ).isEqualTo( "1 Milestone 2" );
		assertThat( new Version( "1-m11" ).toHumanString() ).isEqualTo( "1 Milestone 11" );
		assertThat( new Version( "1-rc" ).toHumanString() ).isEqualTo( "1 Release Candidate" );
		assertThat( new Version( "1-cr2" ).toHumanString() ).isEqualTo( "1 Release Candidate 2" );
		assertThat( new Version( "1-rc123" ).toHumanString() ).isEqualTo( "1 Release Candidate 123" );
		assertThat( new Version( "1-SNAPSHOT" ).toHumanString() ).isEqualTo( "1 SNAPSHOT" );
		assertThat( new Version( "1" ).toHumanString() ).isEqualTo( "1" );
		assertThat( new Version( "1-sp" ).toHumanString() ).isEqualTo( "1 Service Pack" );
		assertThat( new Version( "1-sp2" ).toHumanString() ).isEqualTo( "1 Service Pack 2" );
		assertThat( new Version( "1-sp123" ).toHumanString() ).isEqualTo( "1 Service Pack 123" );
		assertThat( new Version( "1-abc" ).toHumanString() ).isEqualTo( "1 abc" );
		assertThat( new Version( "1-def" ).toHumanString() ).isEqualTo( "1 def" );
		assertThat( new Version( "1-pom-1" ).toHumanString() ).isEqualTo( "1 pom 1" );
		assertThat( new Version( "1-1-snapshot" ).toHumanString() ).isEqualTo( "1-1 SNAPSHOT" );
		assertThat( new Version( "1-1" ).toHumanString() ).isEqualTo( "1-1" );
		assertThat( new Version( "1-2" ).toHumanString() ).isEqualTo( "1-2" );
		assertThat( new Version( "1-123" ).toHumanString() ).isEqualTo( "1-123" );

		assertThat( alpha.toHumanString() ).isEqualTo( "3.4.5 Alpha 06" );
		assertThat( beta.toHumanString() ).isEqualTo( "3.4.5 Beta 06" );
		assertThat( release.toHumanString() ).isEqualTo( "3.4.5 Update 00" );
		assertThat( update.toHumanString() ).isEqualTo( "3.4.5 Update 06" );
		assertThat( snapshot.toHumanString() ).isEqualTo( "3.4.5 SNAPSHOT" );
	}

	@Test
	void testVersionsQualifier() {
		checkVersionsOrder( VERSIONS_QUALIFIER );
	}

	@Test
	void testVersionsNumber() {
		checkVersionsOrder( VERSIONS_NUMBER );
	}

	@Test
	void testVersionsEqual() {
		checkVersionsEqual( "1", "1" );
		checkVersionsEqual( "1", "1.0" );
		checkVersionsEqual( "1", "1.0.0" );
		checkVersionsEqual( "1.0", "1.0.0" );
		checkVersionsEqual( "1", "1-0" );
		checkVersionsEqual( "1", "1.0-0" );
		checkVersionsEqual( "1.0", "1.0-0" );

		// no separator between number and character
		checkVersionsEqual( "1a", "1.a" );
		checkVersionsEqual( "1a", "1-a" );
		checkVersionsEqual( "1a", "1.0-a" );
		checkVersionsEqual( "1a", "1.0.0-a" );
		checkVersionsEqual( "1.0a", "1.0.a" );
		checkVersionsEqual( "1.0.0a", "1.0.0.a" );
		checkVersionsEqual( "1x", "1.x" );
		checkVersionsEqual( "1x", "1-x" );
		checkVersionsEqual( "1x", "1.0-x" );
		checkVersionsEqual( "1x", "1.0.0-x" );
		checkVersionsEqual( "1.0x", "1.0.x" );
		checkVersionsEqual( "1.0.0x", "1.0.0.x" );

		// aliases
		checkVersionsEqual( "1ga", "1" );
		checkVersionsEqual( "1final", "1" );
		checkVersionsEqual( "1cr", "1rc" );

		// special "aliases" a, b, m, p and u for alpha, beta, milestone, patch and update
		checkVersionsEqual( "1a1", "1alpha1" );
		checkVersionsEqual( "1b2", "1beta2" );
		checkVersionsEqual( "1m3", "1milestone3" );
		checkVersionsEqual( "1p4", "1patch4" );
		checkVersionsEqual( "1u5", "1update5" );

		checkVersionsEqual( "1.0-patch", "1.0-patch-0" );
		checkVersionsEqual( "1.0-update", "1.0-update-0" );

		// case insensitive
		checkVersionsEqual( "1X", "1x" );
		checkVersionsEqual( "1A", "1a" );
		checkVersionsEqual( "1B", "1b" );
		checkVersionsEqual( "1M", "1m" );
		checkVersionsEqual( "1Ga", "1" );
		checkVersionsEqual( "1GA", "1" );
		checkVersionsEqual( "1Final", "1" );
		checkVersionsEqual( "1FinaL", "1" );
		checkVersionsEqual( "1FINAL", "1" );
		checkVersionsEqual( "1Cr", "1Rc" );
		checkVersionsEqual( "1cR", "1rC" );
		checkVersionsEqual( "1m3", "1Milestone3" );
		checkVersionsEqual( "1m3", "1MileStone3" );
		checkVersionsEqual( "1m3", "1MILESTONE3" );
	}

	@Test
	void testVersionComparing() {
		checkVersionsOrder( "1", "2" );
		checkVersionsOrder( "1.5", "2" );
		checkVersionsOrder( "1", "2.5" );
		checkVersionsOrder( "1.0", "1.1" );
		checkVersionsOrder( "1.1", "1.2" );
		checkVersionsOrder( "1.0.0", "1.1" );
		checkVersionsOrder( "1.0.1", "1.1" );
		checkVersionsOrder( "1.1", "1.2.0" );

		checkVersionsOrder( "1.0-alpha-1", "1.0" );
		checkVersionsOrder( "1.0-alpha-1", "1.0-alpha-2" );
		checkVersionsOrder( "1.0-alpha-1", "1.0-beta-1" );

		checkVersionsOrder( "1.0-beta-1", "1.0-SNAPSHOT" );
		checkVersionsOrder( "1.0-SNAPSHOT", "1.0" );
		checkVersionsOrder( "1.0-alpha-1-SNAPSHOT", "1.0-alpha-1" );

		checkVersionsOrder( "1.0", "1.0-1" );
		checkVersionsOrder( "1.0-1", "1.0-2" );
		checkVersionsOrder( "1.0.0", "1.0-1" );

		checkVersionsOrder( "2.0-1", "2.0.1" );
		checkVersionsOrder( "2.0.1-klm", "2.0.1-lmn" );
		checkVersionsOrder( "2.0.1", "2.0.1-xyz" );

		checkVersionsOrder( "2.0.1", "2.0.1-123" );
		checkVersionsOrder( "2.0.1-xyz", "2.0.1-123" );

		// Patch versions.
		checkVersionsOrder( "1.0", "1.0-p" );
		checkVersionsOrder( "1.0", "1.0-patch" );
		checkVersionsOrder( "1.0-patch", "1.0-patch-1" );
		checkVersionsOrder( "1.0-patch-1", "1.0-patch-2" );

		// Update versions.
		checkVersionsOrder( "1.0", "1.0-u" );
		checkVersionsOrder( "1.0", "1.0-update" );
		checkVersionsOrder( "1.0-update", "1.0-update-1" );
		checkVersionsOrder( "1.0-update-1", "1.0-update-2" );

		// Java style versions.
		checkVersionsOrder( "1.6", "1.6.0_22" );
		checkVersionsOrder( "1.6.0_22", "1.7" );
	}

	@Test
	void testLocaleIndependent() {
		Locale original = Locale.getDefault();
		Locale[] locales = { Locale.ENGLISH, Locale.of( "tr" ), Locale.getDefault() };
		try {
			for( Locale locale : locales ) {
				Locale.setDefault( locale );
				checkVersionsEqual( "1-abcdefghijklmnopqrstuvwxyz", "1-ABCDEFGHIJKLMNOPQRSTUVWXYZ" );
			}
		} finally {
			Locale.setDefault( original );
		}
	}

	@Test
	void testHashCode() {
		Version a = new Version( "1" );
		Version b = new Version( "1" );
		Version c = new Version( "2" );
		assertThat( a.hashCode() ).isEqualTo( b.hashCode() );
		assertThat( a.hashCode() ).isNotEqualTo( c.hashCode() );
		assertThat( b.hashCode() ).isNotEqualTo( c.hashCode() );
	}

	private Version newComparable( String version ) {
		return new Version( version );
	}

	private void checkVersionsOrder( String[] strings ) {
		Version[] versions = new Version[ strings.length ];
		for( int index = 0; index < strings.length; index++ ) {
			versions[ index ] = newComparable( strings[ index ] );
		}

		for( int lowIndex = 1; lowIndex < strings.length; lowIndex++ ) {
			Version low = versions[ lowIndex - 1 ];
			for( int highIndex = lowIndex; highIndex < strings.length; highIndex++ ) {
				Version high = versions[ highIndex ];
				assertThat( low.compareTo( high ) ).isLessThan( 0 );
				assertThat( high.compareTo( low ) ).isGreaterThan( 0 );
			}
		}
	}

	private void checkVersionsEqual( String string1, String string2 ) {
		Version version1 = newComparable( string1 );
		Version version2 = newComparable( string2 );
		assertThat( version1.compareTo( version2 ) ).isEqualTo( 0 );
		assertThat( version2.compareTo( version1 ) ).isEqualTo( 0 );
		assertThat( version1.hashCode() ).isEqualTo( version2.hashCode() );
		assertThat( version1 ).isEqualTo( version2 );
		assertThat( version2 ).isEqualTo( version1 );
	}

	private void checkVersionsOrder( String string1, String string2 ) {
		Version version1 = newComparable( string1 );
		Version version2 = newComparable( string2 );
		assertThat( version1.compareTo( version2 ) ).withFailMessage( "expected " + string1 + " < " + string2 ).isLessThan( 0 );
		assertThat( version2.compareTo( version1 ) ).withFailMessage( "expected " + string2 + " > " + string1 ).isGreaterThan( 0 );
	}

}
