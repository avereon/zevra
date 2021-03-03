package com.avereon.product;

import com.avereon.settings.Settings;

import java.nio.file.Path;

public class MockProduct implements Product {

	@Override
	public ProductCard getCard() {
		return new ProductCard();
	}

	@Override
	public ClassLoader getClassLoader() {
		return getClassLoader();
	}

	@Override
	public ProductBundle rb() {
		return new ProductBundle( this );
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
