package com.avereon.product;

import com.avereon.settings.Settings;

import java.nio.file.Path;

public class MockProduct implements Product {

	private final Product parent;

	private final String rbPath;

	public MockProduct() {
		this( null, null );
	}

	public MockProduct( String rbPath ) {
		this( null, rbPath );
	}

	public MockProduct( Product parent, String rbPath ) {
		this.parent = parent;
		this.rbPath = rbPath;
	}

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
		return new ProductBundle( parent, this, rbPath );
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
