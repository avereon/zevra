package com.avereon.product;

import com.avereon.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ProductBundle {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Module module;

	private String rbPackage;

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
		String string = other;

		ResourceBundle bundle = ResourceBundle.getBundle( rbPackage + bundleKey, Locale.getDefault(), module );
		if( bundle.containsKey( valueKey ) ) string = MessageFormat.format( bundle.getString( valueKey ), values );

		return string;
	}

	}
