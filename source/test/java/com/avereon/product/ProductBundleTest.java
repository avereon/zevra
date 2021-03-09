package com.avereon.product;

public class ProductBundleTest {

//	@Test
//	void testGetPathRelative() {
//		Product product = new MockProduct();
//		ProductBundle bundle = new ProductBundle( product, "action" );
//		assertThat( bundle.getPath(), is( getClass().getPackageName().replace( ".", "/" ) + "/action" ) );
//	}
//
//	@Test
//	void testGetPathAbsolute() {
//		Product product = new MockProduct();
//		ProductBundle bundle = new ProductBundle( product, "/action" );
//		assertThat( bundle.getPath(), is( "/action" ) );
//	}
//
//	@Test
//	void testText() {
//		Product product = new MockProduct( "/com/avereon/producta/bundles" );
//		assertThat( product.rb().text( "test", null ), is( "test > null" ) );
//		assertThat( product.rb().text( "test", "invalid" ), is( "test > invalid" ) );
//		assertThat( product.rb().text( "test", "name" ), is( "Product A" ) );
//	}
//
//	@Test
//	void testTextOr() {
//		Product product = new MockProduct( "/com/avereon/productb/bundles" );
//		assertThat( product.rb().textOr( "test", null, "" ), is( "" ) );
//		assertThat( product.rb().textOr( "test", "invalid", "" ), is( "" ) );
//		assertThat( product.rb().textOr( "test", "name", "" ), is( "Product B" ) );
//	}
//
//	@Test
//	void testTextOrWithParentProduct() {
//		Product productA = new MockProduct( "/com/avereon/producta/bundles" );
//		Product productB = new MockProduct( productA, "/com/avereon/productb/bundles" );
//		ProductBundle bundleB = new ProductBundle( productB, "/com/avereon/productb/bundles" );
//
//		// Make sure bundleB does not have a theme-color, it should use the one from productA
//		assertNull( bundleB.textOr( "test", "theme-color", null ) );
//
//		// Now check that the productA theme-color can be retrieved using productB
//		assertThat( productB.rb().textOr( "test", "theme-color", null ), is( "blue" ) );
//	}

}
