package com.avereon.product;

import com.avereon.util.Log;
import com.avereon.util.UriUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

/**
 * This class must load the product "info" very quickly. The full product "card" can load more slowly.
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class ProductCard extends BaseCard {

	private static final System.Logger log = Log.get();

	private static final String CARD = "META-INF/product.card";

	private static final String INFO = "META-INF/product.info";

	@JsonIgnore
	private String productKey;

	private String group;

	private String artifact;

	private String packaging;

	private String version;

	private String timestamp;

	@JsonIgnore
	private Release release;

	private List<String> icons;

	private String name;

	private String provider;

	private String providerUrl;

	private int inception;

	private String summary;

	private String description;

	private String copyrightSummary;

	private String licenseSummary;

	@JsonIgnore
	private String productUri;

	private String javaVersion;

	private Path installFolder;

	private List<Maintainer> maintainers;

	private List<Contributor> contributors;

	private boolean enabled;

	private boolean removable;

	@JsonIgnore
	private RepoCard repo;

	private Map<String, String> resources;

	public ProductCard() {}

	public ProductCard init( Product product ) throws IOException {
		return init( product.getClass() );
	}

	public ProductCard init( Class<?> source ) throws IOException {
		/*
		 * NOTE Using the class loader instead of the class to find the resource
		 * does not work as expected when loading products from the classpath.
		 */
		return init( source.getResourceAsStream( "/" + INFO ) );
	}

	//	public ProductCard init( ClassLoader loader ) throws IOException {
	//		try( InputStream input = loader.getResourceAsStream( INFO ) ) {
	//			return init( input );
	//		}
	//	}

	public ProductCard init( InputStream input ) throws IOException {
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
		this.inception = Integer.parseInt( values.getProperty( "inception" ) );

		this.summary = values.getProperty( "summary" );
		this.description = values.getProperty( "description" );
		this.copyrightSummary = values.getProperty( "copyright" );
		this.licenseSummary = values.getProperty( "license" );

		this.updateKey();
		this.updateRelease();

		return this;
	}

	public ProductCard load( Product product ) throws IOException {
		return load( product.getClass() );
	}

	public ProductCard load( Class<?> clazz ) throws IOException {
		/*
		 * NOTE Using the class loader instead of the class to find the resource
		 * does not work as expected when loading products from the classpath.
		 */
		return load( clazz.getResourceAsStream( "/" + CARD ) );
	}

	//	public ProductCard load( ClassLoader loader ) throws IOException {
	//		try( InputStream input = loader.getResourceAsStream( CARD ) ) {
	//			return load( input, null );
	//		}
	//	}

	public ProductCard load( InputStream input ) throws IOException {
		return load( input, null );
	}

	public ProductCard load( InputStream input, URI source ) throws IOException {
		if( input == null ) throw new NullPointerException( "InputStream cannot be null" );
		ProductCard card = new ObjectMapper().readerFor( new TypeReference<ProductCard>() {} ).readValue( input );
		if( source != null ) this.productUri = UriUtil.removeQueryAndFragment( source ).toString();
		return copyFrom( card );
	}

	public ProductCard copyFrom( ProductCard card ) {
		this.group = card.group;
		this.artifact = card.artifact;
		this.packaging = card.packaging;
		this.version = card.version;
		this.timestamp = card.timestamp;

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

	@JsonIgnore
	public String getProductKey() {
		return productKey;
	}

	public String getGroup() {
		return group;
	}

	public ProductCard setGroup( String group ) {
		this.group = group;
		updateKey();
		return this;
	}

	public String getArtifact() {
		return artifact;
	}

	public ProductCard setArtifact( String artifact ) {
		this.artifact = artifact;
		updateKey();
		return this;
	}

	public String getPackaging() {
		return packaging;
	}

	public ProductCard setPackaging( String packaging ) {
		this.packaging = packaging;
		return this;
	}

	public String getVersion() {
		return version;
	}

	public ProductCard setVersion( String version ) {
		this.version = version;
		updateRelease();
		return this;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public ProductCard setTimestamp( String timestamp ) {
		this.timestamp = timestamp;
		updateRelease();
		return this;
	}

	public Release getRelease() {
		return release;
	}

	public List<String> getIcons() {
		return icons;
	}

	public ProductCard setIcons( List<String> icons ) {
		this.icons = Collections.unmodifiableList( icons == null ? List.of() : icons );
		return this;
	}

	public String getName() {
		return name;
	}

	public ProductCard setName( String name ) {
		this.name = name;
		return this;
	}

	public String getProvider() {
		return provider;
	}

	public ProductCard setProvider( String provider ) {
		this.provider = provider;
		return this;
	}

	public String getProviderUrl() {
		return providerUrl;
	}

	public ProductCard setProviderUrl( String providerUrl ) {
		this.providerUrl = providerUrl;
		return this;
	}

	public int getInception() {
		return inception;
	}

	public ProductCard setInception( int inception ) {
		this.inception = inception;
		return this;
	}

	public String getSummary() {
		return summary;
	}

	public ProductCard setSummary( String summary ) {
		this.summary = summary;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public ProductCard setDescription( String description ) {
		this.description = description;
		return this;
	}

	public String getCopyrightSummary() {
		return copyrightSummary;
	}

	public ProductCard setCopyrightSummary( String copyrightSummary ) {
		this.copyrightSummary = copyrightSummary;
		return this;
	}

	public String getLicenseSummary() {
		return licenseSummary;
	}

	public ProductCard setLicenseSummary( String licenseSummary ) {
		this.licenseSummary = licenseSummary;
		return this;
	}

	public String getProductUri() {
		return productUri;
	}

	public ProductCard setProductUri( String productUri ) {
		this.productUri = productUri;
		return this;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public ProductCard setJavaVersion( String javaVersion ) {
		this.javaVersion = javaVersion;
		return this;
	}

	public Path getInstallFolder() {
		return installFolder;
	}

	public ProductCard setInstallFolder( Path installFolder ) {
		this.installFolder = installFolder;
		return this;
	}

	public List<Maintainer> getMaintainers() {
		return maintainers;
	}

	public ProductCard setMaintainers( List<Maintainer> maintainers ) {
		this.maintainers = Collections.unmodifiableList( maintainers );
		return this;
	}

	public List<Contributor> getContributors() {
		return contributors;
	}

	public ProductCard setContributors( List<Contributor> contributors ) {
		this.contributors = Collections.unmodifiableList( contributors );
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public ProductCard setEnabled( boolean enabled ) {
		this.enabled = enabled;
		return this;
	}

	public boolean isRemovable() {
		return removable;
	}

	public ProductCard setRemovable( boolean removable ) {
		this.removable = removable;
		return this;
	}

	public RepoCard getRepo() {
		return repo;
	}

	public ProductCard setRepo( RepoCard repo ) {
		this.repo = repo;
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
		if( !(object instanceof ProductCard) ) return false;
		ProductCard that = (ProductCard)object;
		return this.getProductKey().equals( that.getProductKey() );
	}

	public boolean deepEquals( Object object ) {
		if( !(object instanceof ProductCard) ) return false;
		ProductCard that = (ProductCard)object;

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
