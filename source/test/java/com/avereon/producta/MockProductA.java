package com.avereon.producta;

import com.avereon.product.Product;
import com.avereon.product.ProductBundle;
import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.settings.Settings;

import java.nio.file.Path;

public class MockProductA implements Product {

	private Product parent;

	public MockProductA() {
		Rb.init(this);
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
}
