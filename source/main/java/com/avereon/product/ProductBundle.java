package com.avereon.product;

import com.avereon.util.LogUtil;
import com.avereon.util.TextUtil;
import org.slf4j.Logger;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class is to simplify the interface and use of resource bundles.
 */
public class ProductBundle {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Class<? extends Product> product;

	private Module module;

	private String rbPackage;

	public ProductBundle( Product product ) {
		this( product.getClass() );
	}

	public ProductBundle( Product product, String path ) {
		this( product.getClass(), path );
	}

	/**
	 * Calls the main constructor with the path &quot;/bundles&quot;.
	 *
	 * @param product The product for this bundle
	 */
	public ProductBundle( Class<? extends Product> product ) {
		this( product, "/bundles" );
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
	public ProductBundle( Class<? extends Product> product, String path ) {
		this.product = product;
		this.module = product.getModule();
		this.rbPackage = product.getPackageName().replace( ".", "/" ) + path + "/";
	}

	public String text( String bundleKey, String valueKey, Object... values ) {
		String string = textOr( bundleKey, valueKey, null, values );

		if( string == null ) {
			string = bundleKey + " > " + valueKey;
			log.warn( "Unable to find resource: " + string, new MissingResourceException( string, bundleKey, string ) );
		}

		return string;
	}

	public String textOr( String bundleKey, String valueKey, String other, Object... values ) {
		String string = null;

		if( product.getSimpleName().equals( "Mazer" ) ) {
			log.warn( "Getting text resource for " + product.getName() + "/" + bundleKey + "/" + valueKey );
			String resource = "bundles/" + bundleKey + ".properties";
			log.warn( "Looking for resource: " + resource );
			InputStream input = product.getResourceAsStream( resource );
			if( input != null ) log.warn( "FOUND THE RESOURCE BUNDLE");
		}

		// FIXME Using the module or class loader does not work correctly
		// Apparently using the class does
		ResourceBundle bundle = ResourceBundle.getBundle( rbPackage + bundleKey, Locale.getDefault(), module );
		//ResourceBundle bundle = ResourceBundle.getBundle( rbPackage + bundleKey, Locale.getDefault(), product.getClassLoader() );
		if( bundle.containsKey( valueKey ) ) string = MessageFormat.format( bundle.getString( valueKey ), values );

		return TextUtil.isEmpty( string ) ? other : string;
	}

}
