package com.avereon.product;

import com.avereon.util.Log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * This class is to simplify the interface and use of resource bundles.
 */
public class ProductBundle {

	private static final System.Logger log = Log.get();

	private static final String DEFAULT_PATH = "bundles";

	private final Module module;

	private final String path;

	private final Product parent;

	public ProductBundle( Product product ) {
		this( null, product.getClass(), DEFAULT_PATH );
	}

	public ProductBundle( Product product, String path ) {
		this( null, product.getClass(), path );
	}

	public ProductBundle( Product parent, Product product ) {
		this( parent, product, DEFAULT_PATH );
	}

	public ProductBundle( Product parent, Product product, String path ) {
		this( parent, product.getClass(), path );
	}

	/**
	 * This constructor will determine the ProductBundle path by using the
	 * product package as the first part of the path and add the path argument.
	 * For example, if the product is com.example.Product and the path is
	 * &quot;/bundles&quot; the full lookup path is com/example/bundles.
	 * <p>
	 * NOTE Be sure to open the bundles in the module definition
	 * <p>
	 * Example: opens com.avereon.xenon.bundles;
	 *
	 * @param product The product for this bundle
	 * @param path    Extra path information to append to package path
	 */
	private ProductBundle( Product parent, Class<? extends Product> product, String path ) {
		this.parent = parent;
		this.module = product.getModule();
		this.path = path.startsWith( "/" ) ? path : product.getPackageName().replace( ".", "/" ) + "/" + path;
	}

	public String getPath() {
		return path;
	}

	public String text( String bundleKey, String valueKey, Object... values ) {
		String string = textOr( bundleKey, valueKey, null, values );

		if( string == null ) {
			string = bundleKey + " > " + valueKey;
			log.log( Log.WARN, "Unable to find resource: " + string, new MissingResourceException( string, bundleKey, string ) );
		}

		return string;
	}

	public String textOr( String bundleKey, String valueKey, String other, Object... values ) {
		if( valueKey == null ) return other;
		System.out.println( "bundle=" + path + "/" + bundleKey);
		ResourceBundle bundle = ResourceBundle.getBundle( path + "/" + bundleKey, Locale.getDefault(), module );
		if( bundle.containsKey( valueKey ) ) return MessageFormat.format( bundle.getString( valueKey ), values );
		return parent != null ? parent.rb().textOr( bundleKey, valueKey, other, values ) : other;
	}

}
