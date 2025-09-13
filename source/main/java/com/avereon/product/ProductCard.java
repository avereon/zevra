package com.avereon.product;

import com.avereon.util.UriUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

/**
 * This class represents the product information. The product information
 * includes group, name, version, etc.
 * <p>
 * Product information is stored
 * in two files, one that loads quickly (properties file), but does not support
 * hierarchical data, and one that supports hierarchical data (JSON file), but
 * loads much more slowly.
 * </p>
 * <p>
 * This class must load the product "info" very quickly. The full product "card"
 * can load more slowly.
 * </p>
 */
@CustomLog
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
@Accessors( chain = true )
public class ProductCard extends BaseCard {

	private static final String CARD = "META-INF/product.card";

	private static final String INFO = "META-INF/product.info";

	@Getter
	@JsonIgnore
	private String productKey;

	@Getter
	private String group;

	@Getter
	private String artifact;

	@Getter
	private String version;

	@Getter
	private String timestamp;

	@Getter
	private String packaging;

	@Getter
	private String packagingVersion;

	@Getter
	@JsonIgnore
	private Release release;

	@Getter
	private List<String> icons;

	@Getter
	@Setter
	private String name;

	@Getter
	@Setter
	private String provider;

	@Getter
	@Setter
	private String providerUrl;

	@Getter
	@Setter
	private int inception;

	@Getter
	@Setter
	private String summary;

	@Getter
	@Setter
	private String description;

	@Getter
	@Setter
	private String copyrightSummary;

	@Getter
	@Setter
	private String licenseSummary;

	@Getter
	@Setter
	@JsonIgnore
	private String productUri;

	@Getter
	@Setter
	private String javaVersion;

	@Getter
	@Setter
	private Path installFolder;

	@Getter
	@Setter
	private List<Maintainer> maintainers;

	@Getter
	@Setter
	private List<Contributor> contributors;

	@Getter
	@Setter
	private boolean enabled;

	@Getter
	@Setter
	private boolean removable;

	@Getter
	@Setter
	@JsonIgnore
	private RepoCard repo;

	@JsonIgnore
	private Map<String, String> resources;

	public ProductCard() {}

	public static ProductCard card( Path path ) throws IOException {
		try( FileInputStream input = new FileInputStream( path.resolve( CARD ).toFile() ) ) {
			return new ProductCard().fromJson( input );
		}
	}

	public static ProductCard info( Path path ) throws IOException {
		try( FileInputStream input = new FileInputStream( path.resolve( INFO ).toFile() ) ) {
			return new ProductCard().fromInfo( input );
		}
	}

	public static ProductCard info( Product product ) {
		return info( product.getClass() );
	}

	public static ProductCard info( Class<?> source ) {
		try {
			return new ProductCard().fromInfo( source );
		} catch( IOException exception ) {
			throw new RuntimeException( "Error loading product card", exception );
		}
	}

	private ProductCard fromInfo( Class<?> source ) throws IOException {
		/*
		 * NOTE Using the class loader instead of the class to find the resource
		 * does not work as expected when loading products from the classpath.
		 */
		return fromInfo( source.getResourceAsStream( "/" + INFO ) );
	}

	private ProductCard fromInfo( InputStream input ) throws IOException {
		if( input == null ) throw new NullPointerException( "InputStream cannot be null" );

		Properties values = new Properties();
		values.load( input );

		this.group = values.getProperty( "group" );
		this.artifact = values.getProperty( "artifact" );
		this.packaging = values.getProperty( "packaging" );
		this.version = values.getProperty( "version" );
		this.timestamp = values.getProperty( "timestamp" );

		this.icons = List.of( values.getProperty( "icon" ) );
		this.name = values.getProperty( "name" );
		this.provider = values.getProperty( "provider" );
		this.providerUrl = values.getProperty( "providerUri" );

		try {
			this.inception = Integer.parseInt( values.getProperty( "inception" ) );
		} catch( NumberFormatException exception ) {
			throw new IllegalArgumentException( "The product card has not been processed by Maven" );
		}

		this.summary = values.getProperty( "summary" );
		this.description = values.getProperty( "description" );
		this.copyrightSummary = values.getProperty( "copyright" );
		this.licenseSummary = values.getProperty( "license" );

		this.updateKey();
		this.updateRelease();

		return this;
	}

	public static ProductCard card( Product product ) {
		return card( product.getClass() );
	}

	public static ProductCard card( Class<?> source ) {
		try {
			return new ProductCard().fromJson( source );
		} catch( IOException exception ) {
			throw new RuntimeException( "Error loading product card: " + source.getName(), exception );
		}
	}

	private ProductCard fromJson( Class<?> clazz ) throws IOException {
		/*
		 * NOTE Using the class loader instead of the class to find the resource
		 * does not work as expected when loading products from the classpath.
		 */
		return fromJson( clazz.getResourceAsStream( "/" + CARD ) );
	}

	private ProductCard fromJson( InputStream input ) throws IOException {
		return fromJson( input, null );
	}

	public ProductCard fromJson( InputStream input, URI source ) throws IOException {
		if( input == null ) throw new NullPointerException( "InputStream cannot be null" );
		ProductCard card = new ObjectMapper().readerFor( new TypeReference<ProductCard>() {} ).readValue( input );
		if( source != null ) this.productUri = UriUtil.removeQueryAndFragment( source ).toString();
		return copyFrom( card );
	}

	public ProductCard copyFrom( ProductCard card ) {
		this.group = card.group;
		this.artifact = card.artifact;
		this.version = card.version;
		this.timestamp = card.timestamp;

		this.packaging = card.packaging;
		this.packagingVersion = card.packagingVersion;

		this.icons = card.icons;
		this.name = card.name;
		this.provider = card.provider;
		this.providerUrl = card.providerUrl;
		this.inception = card.inception;

		this.summary = card.summary;
		this.description = card.description;
		this.copyrightSummary = card.copyrightSummary;
		this.licenseSummary = card.licenseSummary;

		this.javaVersion = card.javaVersion;

		this.maintainers = card.maintainers;
		this.contributors = card.contributors;

		this.enabled = card.enabled;
		this.removable = card.removable;

		this.resources = card.resources;

		this.updateKey();
		this.updateRelease();

		return this;
	}

	public ProductCard setGroup( String group ) {
		this.group = group;
		updateKey();
		return this;
	}

	public ProductCard setArtifact( String artifact ) {
		this.artifact = artifact;
		updateKey();
		return this;
	}

	public ProductCard setVersion( String version ) {
		this.version = version;
		updateRelease();
		return this;
	}

	public ProductCard setTimestamp( String timestamp ) {
		this.timestamp = timestamp;
		updateRelease();
		return this;
	}

	public ProductCard setPackaging( String packaging ) {
		this.packaging = packaging;
		return this;
	}

	public ProductCard setPackagingVersion( String version ) {
		this.packagingVersion = version;
		return this;
	}

	public ProductCard setIcons( List<String> icons ) {
		this.icons = Collections.unmodifiableList( icons == null ? List.of() : icons );
		return this;
	}

	@JsonIgnore
	public String[] getResourceUris() {
		return getPlatformResourceUris();
	}

	private void updateKey() {
		/*
		 * The use of '.' as the separator is the most benign of the characters
		 * tested. Changing the separator to a different character will most likely
		 * result in invalid file paths, setting paths, and other undesired side
		 * effects.
		 */
		if( group != null && artifact != null ) productKey = group + "." + artifact;
	}

	private void updateRelease() {
		release = Release.create( this.version, this.timestamp );
	}

	private String[] getPlatformResourceUris() {
		String os = System.getProperty( "os.name" );
		String arch = System.getProperty( "os.arch" );

		Set<String> resources = new HashSet<>();

		// Add the product pack URI
		resources.add( getProductUri() );

		// This code was originally intended to resolve os/architecture specific
		// resources needed for a product. For the moment, this feature is not
		// needed and this method simply returns an empty set.
		//
		//		path += "/@uri";
		//
		//		// Determine the resources.
		//		Node[] nodes = descriptor.getNodes( ProductCard.RESOURCES_PATH );
		//		for( Node node : nodes ) {
		//			XmlDescriptor resourcesDescriptor = new XmlDescriptor( node );
		//			Node osNameNode = node.getAttributes().getNamedItem( "os" );
		//			Node osArchNode = node.getAttributes().getNamedItem( "arch" );
		//
		//			String osName = osNameNode == null ? null : osNameNode.getTextContent();
		//			String osArch = osArchNode == null ? null : osArchNode.getTextContent();
		//
		//			// Determine what resources should not be included.
		//			if( osName != null && !os.startsWith( osName ) ) continue;
		//			if( osArch != null && !arch.equals( osArch ) ) continue;
		//
		//			uris = resourcesDescriptor.getValues( path );
		//			if( uris != null ) resources.addAll( Arrays.asList( uris ) );
		//		}

		return resources.toArray( new String[ 0 ] );
	}

	@Override
	public String toString() {
		return getProductKey();
	}

	@Override
	public int hashCode() {
		return getProductKey().hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof ProductCard that) ) return false;
		return this.getProductKey().equals( that.getProductKey() );
	}

	public boolean deepEquals( Object object ) {
		if( !(object instanceof ProductCard that) ) return false;

		boolean equals = this.group.equals( that.group );
		equals = equals && this.artifact.equals( that.artifact );
		equals = equals && this.packaging.equals( that.packaging );
		equals = equals && this.release.equals( that.release );
		equals = equals && this.icons.equals( that.icons );
		equals = equals && this.name.equals( that.name );
		equals = equals && this.provider.equals( that.provider );
		equals = equals && this.inception == that.inception;
		equals = equals && this.summary.equals( that.summary );
		equals = equals && this.description.equals( that.description );
		equals = equals && this.copyrightSummary.equals( that.copyrightSummary );
		equals = equals && this.licenseSummary.equals( that.licenseSummary );
		equals = equals && this.productUri.equals( that.productUri );

		return equals;
	}

}
