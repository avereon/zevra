package com.xeomar.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xeomar.util.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;

/**
 * This class must load the product "info" very quickly. The full product "card" can load more slowly.
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class ProductCard {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public static final String CARD = "/META-INF/product.card";

	public static final String INFO = "/META-INF/product.info";

	@JsonIgnore
	private String productKey;

	private String group;

	private String artifact;

	private String version;

	private String timestamp;

	@JsonIgnore
	private Release release;

	private String iconUri;

	private String name;

	private String provider;

	private String providerUrl;

	private int inception;

	private String summary;

	private String description;

	private String copyrightSummary;

	private String licenseSummary;

	private String productUri;

	//private String cardUri;

	//private String packUri;

	private String mainClass;

	private String javaVersion;

	private Path installFolder;

	@JsonIgnore
	private List<Maintainer> maintainers;

	@JsonIgnore
	private List<Contributor> contributors;

	private boolean enabled;

	private boolean removable;

	private Map<String, String> resources;

	public ProductCard() {}

	public ProductCard init( Class<?> source ) throws IOException {
		try( InputStream input = source.getResourceAsStream( INFO ) ) {
			return init( input );
		}
	}

	public ProductCard init( InputStream input ) throws IOException {
		if( input == null ) throw new NullPointerException( "InputStream cannot be null" );

		Properties values = new Properties();
		values.load( input );

		this.group = values.getProperty( "group" );
		this.artifact = values.getProperty( "artifact" );
		this.version = values.getProperty( "version" );
		this.timestamp = values.getProperty( "timestamp" );
		this.release = Release.create( this.version, this.timestamp );

		this.iconUri = values.getProperty( "icon" );
		this.name = values.getProperty( "name" );
		this.provider = values.getProperty( "provider" );
		this.providerUrl = values.getProperty( "providerUri" );
		this.inception = Integer.parseInt( values.getProperty( "inception" ) );

		this.summary = values.getProperty( "summary" );
		this.description = values.getProperty( "description" );
		this.copyrightSummary = values.getProperty( "copyright" );
		this.licenseSummary = values.getProperty( "license" );

		this.updateKey();

		return this;
	}

	public ProductCard load( Class<? extends Product> product ) throws IOException {
		return load( product, null );
	}

	public ProductCard load( Class<? extends Product> product, URI base ) throws IOException {
		try( InputStream input = product.getResourceAsStream( CARD ) ) {
			return load( input, base );
		}
	}

	public ProductCard load( InputStream input ) throws IOException {
		return load( input, null );
	}

	public ProductCard load( InputStream input, URI source ) throws IOException {
		ProductCard card = new ObjectMapper().readerFor( new TypeReference<ProductCard>() {} ).readValue( input );

		this.group = card.group;
		this.artifact = card.artifact;
		this.version = card.version;
		this.timestamp = card.timestamp;
		this.release = Release.create( this.version, this.timestamp );

		this.iconUri = card.iconUri;
		this.name = card.name;
		this.provider = card.provider;
		this.providerUrl = card.providerUrl;
		this.inception = card.inception;

		this.summary = card.summary;
		this.description = card.description;
		this.copyrightSummary = card.copyrightSummary;
		this.licenseSummary = card.licenseSummary;

		this.productUri = card.productUri;
		this.javaVersion = card.javaVersion;

		this.maintainers = card.maintainers;
		this.contributors = card.contributors;

		this.enabled = card.enabled;
		this.removable = card.removable;

		this.resources = card.resources;

		if( source != null ) this.productUri = UriUtil.removeQueryAndFragment( source ).toString();

		updateKey();

		return this;
	}

	@JsonIgnore
	public String getProductKey() {
		return productKey;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup( String group ) {
		this.group = group;
		updateKey();
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact( String artifact ) {
		this.artifact = artifact;
		updateKey();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion( String version ) {
		this.version = version;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp( String timestamp ) {
		this.timestamp = timestamp;
	}

	public Release getRelease() {
		return release;
	}

	public String getIconUri() {
		return iconUri;
	}

	public void setIconUri( String iconUri ) {
		this.iconUri = iconUri;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider( String provider ) {
		this.provider = provider;
	}

	public String getProviderUrl() {
		return providerUrl;
	}

	public void setProviderUrl( String providerUrl ) {
		this.providerUrl = providerUrl;
	}

	public int getInception() {
		return inception;
	}

	public void setInception( int inception ) {
		this.inception = inception;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary( String summary ) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription( String description ) {
		this.description = description;
	}

	public String getCopyrightSummary() {
		return copyrightSummary;
	}

	public void setCopyrightSummary( String copyrightSummary ) {
		this.copyrightSummary = copyrightSummary;
	}

	public String getLicenseSummary() {
		return licenseSummary;
	}

	public void setLicenseSummary( String licenseSummary ) {
		this.licenseSummary = licenseSummary;
	}

	public URI getProductUri( Map<String, String> parameters ) throws URISyntaxException {
		StringBuilder query = new StringBuilder();
		for( String key : parameters.keySet() ) {
			query.append( "&" );
			query.append( key );
			query.append( "=" );
			query.append( parameters.get( key ) );
		}

		return new URI( productUri + "?" + query.substring( 1 ) );
	}

	public String getProductUri() {
		return productUri;
	}

	public void setProductUri( String productUri ) {
		this.productUri = productUri;
	}

	@Deprecated
	public String getCardUri() {
		return null;
	}

	@Deprecated
	public URI getCardUri( String channel ) throws URISyntaxException {
		return null;
	}

	@Deprecated
	public URI getCardUri( String channel, String platform ) throws URISyntaxException {
		return null;
	}

	@Deprecated
	public void setCardUri( String cardUri ) {
		//this.cardUri = cardUri;
	}

	@Deprecated
	public String getPackUri() {
		return null;
	}

	@Deprecated
	public URI getPackUri( String channel ) throws URISyntaxException {
		return null;
	}

	@Deprecated
	public URI getPackUri( String channel, String platform ) throws URISyntaxException {
		return null;
	}

	@Deprecated
	public void setPackUri( String packUri ) {
		//this.packUri = packUri;
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass( String mainClass ) {
		this.mainClass = mainClass;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion( String javaVersion ) {
		this.javaVersion = javaVersion;
	}

	public Path getInstallFolder() {
		return installFolder;
	}

	public void setInstallFolder( Path installFolder ) {
		this.installFolder = installFolder;
	}

	public List<Maintainer> getMaintainers() {
		return maintainers;
	}

	public void setMaintainers( List<Maintainer> maintainers ) {
		this.maintainers = maintainers;
	}

	public List<Contributor> getContributors() {
		return contributors;
	}

	public void setContributors( List<Contributor> contributors ) {
		this.contributors = contributors;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	public boolean isRemovable() {
		return removable;
	}

	public void setRemovable( boolean removable ) {
		this.removable = removable;
	}

	public String[] getResourceUris( String channel ) {
		return getPlatformResourceUris( channel );
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

	private String[] getPlatformResourceUris( String channel ) {
		String os = System.getProperty( "os.name" );
		String arch = System.getProperty( "os.arch" );

		Set<String> resources = new HashSet<>();

		// Add the product pack URI
		//resources.add( getPackUri( channel ) );

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

		return resources.toArray( new String[ resources.size() ] );
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
		return this.group.equals( that.group ) && this.artifact.equals( that.artifact );
	}

	public boolean deepEquals( Object object ) {
		if( !(object instanceof ProductCard) ) return false;
		ProductCard that = (ProductCard)object;

		boolean equals = true;
		equals = equals && this.group.equals( that.group );
		equals = equals && this.artifact.equals( that.artifact );
		equals = equals && this.release.equals( that.release );
		equals = equals && this.iconUri.equals( that.iconUri );
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
