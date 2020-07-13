package com.avereon.product;

import com.avereon.util.Log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class is to simplify the interface and use of resource bundles.
 */
public class ProductBundle {

	private static final System.Logger log = Log.get();

	private static final String DEFAULT_PATH = "/bundles";

	private Class<? extends Product> product;

	private Module module;

	private String rbPackage;

	public ProductBundle( Product product ) {
		this( product.getClass(), DEFAULT_PATH );
	}

	public ProductBundle( Product product, String path ) {
		this( product.getClass(), path );
	}

	/**
	 * This constructor will determine the ProductBundle path by using the product
	 * package as the first part of the path and add the path argument. For
	 * example, if the product is com.example.Product and the path is
	 * &quot;/bundles&quot; the full lookup path is com/example/bundles.
	 *
	 * @param product The product for this bundle
	 * @param path Extra path information to append to package path
	 */
	private ProductBundle( Class<? extends Product> product, String path ) {
		this.product = product;
		this.module = product.getModule();
		this.rbPackage = product.getPackageName().replace( ".", "/" ) + path + "/";
	}

	public String text( String bundleKey, String valueKey, Object... values ) {
		String string = textOr( bundleKey, valueKey, null, values );

		if( string == null ) {
			string = bundleKey + " > " + valueKey;
			log.log( Log.WARN, "Unable to find resource: " + string, new MissingResourceException( string, bundleKey, string ) );
		}

		return string;
	}

	public String textOr( String bundleKey, String valueKey, String other, Object... values ) {
		// NOTE Be sure to open the bundles in the module definition
		// Example: opens com.avereon.xenon.bundles;
		ResourceBundle bundle = ResourceBundle.getBundle( rbPackage + bundleKey, Locale.getDefault(), module );
		return (valueKey != null && bundle.containsKey( valueKey ) ) ? MessageFormat.format( bundle.getString( valueKey ), values ) : other;
	}

}
