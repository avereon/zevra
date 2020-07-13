package com.avereon.product;

import com.avereon.settings.Settings;

import java.nio.file.Path;

public interface Product {

	ProductCard getCard();

	ClassLoader getClassLoader();

	ProductBundle rb();

	/**
	 * Get the product settings.
	 *
	 * @return The product settings
	 */
	Settings getSettings();

	/**
	 * Get the shared product data folder. This is the location where the product
	 * should be able to store files that are specific to the product. This path
	 * is operating system specific and can be different between different
	 * versions of operating system.
	 * <p>
	 * Note: This folder is shared by multiple instances of the product.
	 *
	 * @return The product data folder.
	 */
	Path getDataFolder();

}
