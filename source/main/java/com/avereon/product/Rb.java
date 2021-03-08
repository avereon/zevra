package com.avereon.product;

import com.avereon.util.JavaUtil;
import com.avereon.util.Log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class Rb {

	private static final System.Logger log = Log.get();

	private static final String DEFAULT_PATH = "bundles";

	private static final Map<Module, Product> products;

	private static final Map<Product, String> productPaths;

	static {
		products = new ConcurrentHashMap<>();
		productPaths = new ConcurrentHashMap<>();
	}

	public static void init( Product product ) {
		Module module = product.getClass().getModule();
		String path = product.getClass().getPackageName().replace( ".", "/" );
		products.put( module, product );
		productPaths.put( product, path );
	}

	public static String getPath() {
		return getPath( getProduct() );
	}

	public static String getPath( Product product ) {
		return getPath( product, DEFAULT_PATH );
	}

	public static String getPath( Product product, String path ) {
		String bundlePath = productPaths.get( product );
		if( bundlePath == null ) log.log( Log.WARN, "Path missing for product: " + product.getClass().getName() );
		return path.startsWith( "/" ) ? path : bundlePath + "/" + path;
	}

	public static String text( String bundleKey, String valueKey, Object... values ) {
		return getText( getProduct(), DEFAULT_PATH, bundleKey, valueKey, values );
	}

	public static String text( String path, String bundleKey, String valueKey, Object... values ) {
		return getText( getProduct(), path, bundleKey, valueKey, values );
	}

	public static String textOr( String bundleKey, String valueKey, String other, Object... values ) {
		return getTextOr( getProduct(), DEFAULT_PATH, bundleKey, valueKey, other, values );
	}

	public static String textOr( String path, String bundleKey, String valueKey, String other, Object... values ) {
		return getTextOr( getProduct(), path, bundleKey, valueKey, other, values );
	}

	public static String text( Product product, String bundleKey, String valueKey, Object... values ) {
		return getText( product, DEFAULT_PATH, bundleKey, valueKey, values );
	}

	public static String textOr( Product product, String bundleKey, String valueKey, String other, Object... values ) {
		return getTextOr( product, DEFAULT_PATH, bundleKey, valueKey, other, values );
	}

	private static Product getProduct() {
		Class<?> callingClass = JavaUtil.getCallingClass();
		log.log( Log.INFO, "products=" + products );
		log.log( Log.INFO, "callingClass=" + callingClass );
		if( callingClass != null ) log.log( Log.INFO, "module=" + callingClass.getModule() );
		return products.get( callingClass.getModule() );
	}

	private static String getText( Product product, String path, String bundleKey, String valueKey, Object... values ) {
		String string = getTextOr( product, path, bundleKey, valueKey, null, values );

		if( string == null ) {
			string = bundleKey + " > " + valueKey;
			if( log.isLoggable( Log.TRACE ) ) {
				log.log( Log.WARN, "Unable to find resource: " + string, new MissingResourceException( string, bundleKey, string ) );
			} else {
				log.log( Log.WARN, "Unable to find resource: " + string );
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
			log.log( Log.WARN, exception.getMessage() );
		}

		if( product.getParent() == null ) return other;
		return getTextOr( product.getParent(), path, bundleKey, valueKey, other, values );
	}

	private static ResourceBundle getResourceBundle( Product product, String path ) {
		return ResourceBundle.getBundle( path, Locale.getDefault(), product.getClass().getModule() );
	}

}
