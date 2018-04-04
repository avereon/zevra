package com.xeomar.product;

import com.xeomar.util.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;
import static java.util.ResourceBundle.getBundle;

public class ProductBundle {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ClassLoader loader;

	public ProductBundle( ClassLoader loader ) {
		this.loader = loader;
	}

	public String getString( String bundleKey, String valueKey, String... values ) {
		String string = null;

		ResourceBundle bundle = getBundle( "bundles/" + bundleKey, Locale.getDefault(), loader );
		if( bundle.containsKey( valueKey ) ) string = format( bundle.getString( valueKey ), (Object[])values );
		if( string == null ) {
			string = bundleKey + " > " + valueKey;
			log.warn( "Unable to find resource: " + string, new MissingResourceException( string, bundleKey, string ) );
		}

		return string;
	}

}
