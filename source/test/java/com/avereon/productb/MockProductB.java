package com.avereon.productb;

import com.avereon.product.Product;
import com.avereon.product.ProductBundle;
import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.settings.Settings;

import java.nio.file.Path;

public class MockProductB implements Product {

	private Product parent;

	public MockProductB( Product parent ) {
		this.parent = parent;
		Rb.init( this );
	}

	@Override
	public ProductCard getCard() {
		return null;
	}

	@Override
	public Product getParent() {
		return parent;
	}

	@Override
	public ProductBundle rb() {
		return null;
	}

	@Override
	public Settings getSettings() {
		return null;
	}

	@Override
	public Path getDataFolder() {
		return null;
	}

	public String getTheme() {
		return Rb.textOr(this, "test", "theme-color", null );
	}

}
