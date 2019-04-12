package com.xeomar.product;

import com.xeomar.util.JavaUtil;
import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ProductBundle {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Module module;

	private ClassLoader loader;

	private String prefix;

	private String rbPackage;

	public ProductBundle( Class<? extends Product> product ) {
		this( product, JavaUtil.getPackagePath( product ) );
	}

	public ProductBundle( Class<? extends Product> product, String prefix ) {
		this.module = product.getModule();
		this.loader = product.getClassLoader();
		this.prefix = prefix;

		this.rbPackage = product.getPackageName().replace( ".", "/" );
	}

	public String getString( String bundleKey, String valueKey, String... values ) {
		String string = getStringOrNull( bundleKey, valueKey, values );

		if( string == null ) {
			string = bundleKey + " > " + valueKey;
			log.warn( "Unable to find resource: " + string, new MissingResourceException( string, bundleKey, string ) );
		}

		return string;
	}

	public String getStringOrNull( String bundleKey, String valueKey, String... values ) {
		String string = null;

		ResourceBundle bundle = ResourceBundle.getBundle( rbPackage + "/" + bundleKey, Locale.getDefault(), module );
		if( bundle.containsKey( valueKey ) ) string = MessageFormat.format( bundle.getString( valueKey ), (Object[])values );

		return string;
	}

}
