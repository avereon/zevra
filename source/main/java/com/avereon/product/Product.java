package com.avereon.product;

import com.avereon.settings.Settings;

import java.nio.file.Path;

public interface Product {

	/**
	 * Get the product's {@link ProductCard}.
	 *
	 * @return The product's product card
	 */
	ProductCard getCard();

	/**
	 * Get the product parent. If the product is a Program then this should return
	 * null. If the product is a Mod this should return the parent product. Mods
	 * can also be parents of other Mods.
	 *
	 * @return The parent Product or null if the product is a Program
	 */
	default Product getParent() {return null;}

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
