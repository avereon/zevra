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

	private static String path = DEFAULT_PATH;

	private static final Map<Module, Product> products;

	private static final Map<Module, String> paths;

	static {
		products = new ConcurrentHashMap<>();
		paths = new ConcurrentHashMap<>();
	}

	public static void init( Product product ) {
		Module module = product.getClass().getModule();
		String path = product.getClass().getPackageName().replace( ".", "/" );
		products.put( module, product );
		paths.put( module, path );
		System.out.println( "module=" + module.getName() + " path=" + path );
	}

	public static String text( String bundleKey, String valueKey, Object... values ) {
		Class<?> caller = JavaUtil.getCallingClass( 2 );
		return getText( caller.getModule(), DEFAULT_PATH, bundleKey, valueKey, values );
	}

	public static String text( String path, String bundleKey, String valueKey, Object... values ) {
		Class<?> caller = JavaUtil.getCallingClass( 2 );
		return getText( caller.getModule(), path, bundleKey, valueKey, values );
	}

	public static String textOr( String bundleKey, String valueKey, String other, Object... values ) {
		Class<?> caller = JavaUtil.getCallingClass( 2 );
		return getTextOr( caller.getModule(), DEFAULT_PATH, bundleKey, valueKey, other, values );
	}

	public static String textOr( String path, String bundleKey, String valueKey, String other, Object... values ) {
		Class<?> caller = JavaUtil.getCallingClass( 2 );
		return getTextOr( caller.getModule(), path, bundleKey, valueKey, other, values );
	}

	private static String getText( Module module, String path, String bundleKey, String valueKey, Object... values) {
		String string = getTextOr( module, path, bundleKey, valueKey, null, values );

		if( string == null ) {
			string = bundleKey + " > " + valueKey;
			if( log.isLoggable( Log.TRACE ) ) {
				log.log( Log.WARN, "Unable to find resource: " + string, new MissingResourceException( string, bundleKey, string ) );
			} else {
				log.log( Log.WARN, "Unable to find resource: " + string );
			}
		}

		System.out.println( "text=" + string );

		return string;
	}

	private static String getTextOr( Module module, String path, String bundleKey, String valueKey, String other, Object... values ) {
		if( valueKey == null || module == null ) return other;

		String rbPath = getPath( module ) + "/" + path + "/" + bundleKey;

		try {
			ResourceBundle bundle = getResourceBundle( module, rbPath );
			if( bundle.containsKey( valueKey ) ) return MessageFormat.format( bundle.getString( valueKey ), values );
		} catch( MissingResourceException exception ) {
			log.log( Log.WARN, exception.getMessage() );
		}

		Product product = products.get( module );
		if( product.getParent() == null ) return other;
		return getTextOr( product.getParent().getClass().getModule(), path, bundleKey, valueKey, other, values );
	}

	private static String getPath( Module module ) {
		String path = paths.get( module );
		if( path == null ) log.log( Log.WARN, "Path missing for loader: " + module.getName() );
		return path;
	}

	private static ResourceBundle getResourceBundle( Module module, String path ) {
		return ResourceBundle.getBundle( path, Locale.getDefault(), module );
	}

}
