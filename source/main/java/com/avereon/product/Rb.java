package com.avereon.product;

import com.avereon.util.JavaUtil;
import lombok.CustomLog;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

@CustomLog
public class Rb {

	private static final String DEFAULT_PATH = "bundles";

	// Map of module name to products
	private static final Map<String, Product> products;

	private static final Map<Product, String> productPaths;

	static {
		products = new ConcurrentHashMap<>();
		productPaths = new ConcurrentHashMap<>();
	}

	public static void init( Product product ) {
		products.put( product.getClass().getModule().getName(), product );
		productPaths.put( product, product.getClass().getPackageName().replace( ".", "/" ) );
	}

	public static String getPath() {
		return getPath( getProduct() );
	}

	public static String getPath( Product product ) {
		return getPath( product, DEFAULT_PATH );
	}

	public static String getPath( Product product, String path ) {
		String bundlePath = productPaths.get( product );
		if( bundlePath == null ) log.atWarning().log( "Path missing for product: %s", product.getClass().getName() );
		return path.startsWith( "/" ) ? path : bundlePath + "/" + path;
	}

	public static String text( String bundleKey, String valueKey, Object... values ) {
		return getText( getProduct(), DEFAULT_PATH, bundleKey, valueKey, values );
	}

	public static String text( Product product, String bundleKey, String valueKey, Object... values ) {
		return getText( product, DEFAULT_PATH, bundleKey, valueKey, values );
	}

	public static String textOr( String bundleKey, String valueKey, String other, Object... values ) {
		return getTextOr( getProduct(), DEFAULT_PATH, bundleKey, valueKey, other, values );
	}

	public static String textOr( Product product, String bundleKey, String valueKey, String other, Object... values ) {
		return getTextOr( product, DEFAULT_PATH, bundleKey, valueKey, other, values );
	}

	private static String getText( Product product, String path, String bundleKey, String valueKey, Object... values ) {
		String string = getTextOr( product, path, bundleKey, valueKey, null, values );

		if( string == null ) {
			string = product.getCard().getArtifact() + " > " + bundleKey + " > " + valueKey;
			if( log.atFiner().isEnabled() ) {
				log.atWarning().withCause( new MissingResourceException( string, bundleKey, string ) ).log( "Unable to find resource: %s", string );
			} else {
				log.atWarning().log( "Unable to find resource: %s", string );
			}
		}

		return string;
	}

	private static String getTextOr( Product product, String path, String bundleKey, String valueKey, String other, Object... values ) {
		if( valueKey == null || product == null ) return other;

		String rbPath = getPath( product, path ) + "/" + bundleKey;

		try {
			ResourceBundle bundle = getResourceBundle( product, rbPath );
			if( bundle.containsKey( valueKey ) ) return MessageFormat.format( bundle.getString( valueKey ), values );
		} catch( MissingResourceException exception ) {
			if( log.atFiner().isEnabled() ) {
				log.atWarning().withCause( exception ).log( "Unable to find resource" );
			} else {
				log.atWarning().log( "Unable to find resource: %s", exception.getMessage() );
			}
		}

		if( product.getParent() == null ) return other;
		return getTextOr( product.getParent(), path, bundleKey, valueKey, other, values );
	}

	private static Product getProduct() {
		return products.get( JavaUtil.getCallingModuleName() );
	}

	private static ResourceBundle getResourceBundle( Product product, String path ) {
		return ResourceBundle.getBundle( path, Locale.getDefault(), product.getClass().getModule() );
	}

}
