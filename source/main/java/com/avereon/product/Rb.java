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
		if( bundlePath == null ) log.atWarning().log( "Product RB not registered: %s", product.getClass().getName() );
		return path.startsWith( "/" ) ? path : bundlePath + "/" + path;
	}

	public static String text( String rbKey, String valueKey, Object... values ) {
		return doGetText( getProduct(), DEFAULT_PATH, rbKey, valueKey, false, null, values );
	}

	public static String text( Product product, String rbKey, String valueKey, Object... values ) {
		return doGetText( product, DEFAULT_PATH, rbKey, valueKey, false, null, values );
	}

	public static String textOr( String rbKey, String valueKey, String other, Object... values ) {
		return doGetText( getProduct(), DEFAULT_PATH, rbKey, valueKey, true, other, values );
	}

	public static String textOr( Product product, String rbKey, String valueKey, String other, Object... values ) {
		return doGetText( product, DEFAULT_PATH, rbKey, valueKey, true, other, values );
	}

	private static String doGetText( Product product, String path, String rbKey, String valueKey, boolean useOther, String other, Object... values ) {
		return doGetText( product, product, path, rbKey, valueKey, useOther, other, values );
	}

	private static String doGetText(
		final Product originalProduct,
		final Product product,
		final String path,
		final String rbKey,
		final String valueKey,
		final boolean useOther,
		final String other,
		final Object... values
	) {
		if( product == null ) return other;

		String string = null;
		Product parent = product.getParent();
		String rbPath = getPath( product, path ) + "/" + rbKey;
		String missingResourceMessage = originalProduct.getCard().getArtifact() + " > " + rbKey + " > " + valueKey;

		try {
			ResourceBundle bundle = getResourceBundle( product, rbPath );
			if( valueKey != null && bundle.containsKey( valueKey ) ) string = MessageFormat.format( bundle.getString( valueKey ), values );
		} catch( MissingResourceException exception ) {
			log.atWarning().log( "Missing bundle for: %s", rbPath );
			if( parent == null ) {
				log.atWarning().log( "Missing parent bundle for: %s", rbPath );
				return null;
			}
		}

		if( string == null ) {
			if( parent != null ) {
				string = doGetText( originalProduct, parent, path, rbKey, valueKey, useOther, other, values );
			} else {
				if( useOther ) {
					string = other;
				} else {
					String prompt = "Missing value for: %s";
					string = missingResourceMessage;
					if( log.atDebug().isEnabled() ) {
						log.atWarning().withCause( new MissingResourceException( string, rbKey, string ) ).log( prompt, string );
					} else {
						log.atWarning().log( prompt, string );
					}
				}
			}
		}

		return string;
	}

	private static Product getProduct() {
		return products.get( JavaUtil.getCallingModuleName() );
	}

	private static ResourceBundle getResourceBundle( Product product, String path ) {
		return ResourceBundle.getBundle( path, Locale.getDefault(), product.getClass().getModule() );
	}

}
