package com.avereon.product;

import com.avereon.producta.MockProductA;
import com.avereon.productb.MockProductB;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RbTest {

	@Test
	void testGetPathRelative() {
		Product product = new MockProduct();
		assertThat( Rb.getPath( product, "action" ), is( getClass().getPackageName().replace( ".", "/" ) + "/action" ) );
	}

	@Test
	void testGetPathAbsolute() {
		Product product = new MockProduct();
		assertThat( Rb.getPath( product, "/action" ), is( "/action" ) );
	}

	@Test
	void testText() {
		Product product = new MockProductA();
		assertThat( Rb.text( product, "test", null ), is( "test > null" ) );
		assertThat( Rb.text( product, "test", "invalid" ), is( "test > invalid" ) );
		assertThat( Rb.text( product, "test", "name" ), is( "Product A" ) );
	}

	@Test
	void testTextOr() {
		Product product = new MockProductB( new MockProductA() );
		assertThat( Rb.textOr( product, "test", null, "" ), is( "" ) );
		assertThat( Rb.textOr( product, "test", "invalid", "" ), is( "" ) );
		assertThat( Rb.textOr( product, "test", "name", "" ), is( "Product B" ) );
	}

	@Test
	void testTextOrWithParentProduct() {
		Product productA = new MockProductA();
		MockProductB productB = new MockProductB( new MockProduct() );

		// Make sure bundleB does not have a theme-color, it should use the one from productA
		assertNull( productB.getTheme() );

		// Now check that the productA theme-color can be retrieved using productB
		productB = new MockProductB( productA );
		assertThat( productB.getTheme(), is( "blue" ) );
	}

}
