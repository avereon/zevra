package com.avereon.product;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ProductBundleTest {

	@Test
	void testText() {
		Product product = new MockProduct();
		ProductBundle bundle = new ProductBundle( product, "/com/avereon/producta/bundles" );
		assertThat( bundle.text( "test", null ), is( "test > null" ) );
		assertThat( bundle.text( "test", "invalid" ), is( "test > invalid" ) );
		assertThat( bundle.text( "test", "name" ), is( "Product A" ) );
	}

	@Test
	void testTextOr() {
		Product product = new MockProduct();
		ProductBundle bundle = new ProductBundle( product, "/com/avereon/producta/bundles" );
		assertThat( bundle.textOr( "test", null, "" ), is( "" ) );
		assertThat( bundle.textOr( "test", "invalid", "" ), is( "" ) );
		assertThat( bundle.textOr( "test", "name", "" ), is( "Product A" ) );
	}

}
