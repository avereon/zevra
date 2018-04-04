package com.xeomar.product;

import com.xeomar.util.JavaUtil;
import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;
import static java.util.ResourceBundle.getBundle;

public class ProductBundle {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ClassLoader loader;

	private String prefix;

	public ProductBundle( Class<? extends  Product> product ) {
		this.loader = product.getClassLoader();
		this.prefix = JavaUtil.getPackagePath( product );

		InputStream resourceInput = product.getResourceAsStream( "/bundles/action.properties" );
		if( resourceInput == null ) throw new NullPointerException( "COULD NOT LOAD ACTION RESOURCE BUNDLE" );
	}

	public String getString( String bundleKey, String valueKey, String... values ) {
		String string = null;

		ResourceBundle bundle = getBundle( prefix + "/" + bundleKey, Locale.getDefault(), loader );
		if( bundle.containsKey( valueKey ) ) string = format( bundle.getString( valueKey ), (Object[])values );
		if( string == null ) {
			string = bundleKey + " > " + valueKey;
			log.warn( "Unable to find resource: " + string, new MissingResourceException( string, bundleKey, string ) );
		}

		return string;
	}

}
