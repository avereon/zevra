package com.xeomar.product;

import com.xeomar.util.JavaUtil;
import com.xeomar.util.LogUtil;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;
import static java.util.ResourceBundle.getBundle;

public class ProductBundle {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private Module module;

	private ClassLoader loader;

	private String prefix;

	public ProductBundle( Class<? extends Product> product ) {
		this( product, JavaUtil.getPackagePath( product ) );
	}

	public ProductBundle( Class<? extends Product> product, String prefix ) {
		this.module = product.getModule();
		this.loader = product.getClassLoader();
		this.prefix = prefix;

//		if( product.getName().contains( "xenon" ) ) {
//			try {
//				System.out.println( product.getName() );
//				InputStream moduleInput = product.getModule().getResourceAsStream( "/META-INF/bundles/action.properties" );
//				System.out.println( moduleInput != null ? "FOUND MODULE RESOURCE" : "COULD NOT FIND MODULE RESOURCE" );
//				InputStream classInput = product.getResourceAsStream( "/bundles/action.properties" );
//				System.out.println( classInput != null ? "FOUND CLASS RESOURCE" : "COULD NOT FIND CLASS RESOURCE" );
//			} catch( IOException e ) {
//				e.printStackTrace();
//			}
//		}
	}

	public String getString( String bundleKey, String valueKey, String... values ) {
		String string = null;

		ResourceBundle bundle = getBundle( "bundles/" + bundleKey, Locale.getDefault(), module );
		if( bundle.containsKey( valueKey ) ) string = format( bundle.getString( valueKey ), (Object[])values );
		if( string == null ) {
			string = bundleKey + " > " + valueKey;
			log.warn( "Unable to find resource: " + string, new MissingResourceException( string, bundleKey, string ) );
		}

		return string;
	}

}
