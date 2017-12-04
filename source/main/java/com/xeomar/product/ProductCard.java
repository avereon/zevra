package com.xeomar.product;

import com.xeomar.util.Contributor;
import com.xeomar.util.Maintainer;
import com.xeomar.util.Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

/**
 * This class must load the product "info" very quickly. The full product "card" can load more slowly.
 */
// TODO Use Lombok when it is supported in Java 9
public class ProductCard {

	private static final Logger log = LoggerFactory.getLogger( ProductCard.class );

	private static final String CARD = "/META-INF/product.card";

	private static final String INFO = "/META-INF/product.info";

	private String productKey;

	private String group;

	private String artifact;

	private String version;

	private String timestamp;

	private String iconUri;

	private String name;

	private String provider;

	private int inception;

	private String summary;

	private String description;

	private String copyrightSummary;

	private String licenseSummary;

	private String cardUri;

	private String packUri;

	private String mainClass;

	private String javaVersion;

	private Path installFolder;

	private List<Maintainer> maintainers;

	private List<Contributor> contributors;

	private Map<String, String> resources;

	public ProductCard() {
		loadInfo();
	}

	private void loadInfo() {
		Properties values = new Properties();
		try( InputStream stream = getClass().getResourceAsStream( INFO ) ) {
			if( stream == null ) return;
			values.load( stream );
		} catch( IOException exception ) {
			log.error( "Error loading product card", exception );
		}

		this.group = values.getProperty( "group" );
		this.artifact = values.getProperty( "artifact" );
		this.version = values.getProperty( "version" );
		this.timestamp = values.getProperty( "timestamp" );

		this.iconUri = values.getProperty( "icon" );
		this.name = values.getProperty( "name" );
		this.provider = values.getProperty( "provider" );
		this.inception = Integer.parseInt( values.getProperty( "inception" ) );

		this.summary = values.getProperty( "summary" );
		this.description = values.getProperty( "description" );
		this.copyrightSummary = values.getProperty( "copyright" );
		this.licenseSummary = values.getProperty( "license" );

		updateKey();
	}

	public void loadCard() throws IOException  {
		loadCard( getClass().getResourceAsStream( CARD ) );
	}

	public void loadCard( InputStream input ) throws IOException {
		loadCard( input , null );
	}

		@SuppressWarnings( "unchecked" )
	public void loadCard( InputStream input, URI source ) throws IOException {
		Map<String, Object> values;
		try( InputStream stream = input ) {
			values = (Map<String, Object>)new Yaml().load( stream );
		}

		this.group = (String)values.get( "group" );
		this.artifact = (String)values.get( "artifact" );
		this.version = (String)values.get( "version" );
		this.timestamp = (String)values.get( "timestamp" );

		this.iconUri = (String)values.get( "icon" );
		this.name = (String)values.get( "name" );
		this.provider = (String)values.get( "provider" );
		this.inception = (Integer)values.get( "inception" );

		this.summary = (String)values.get( "summary" );
		this.description = (String)values.get( "description" );
		this.copyrightSummary = (String)values.get( "copyright" );
		this.licenseSummary = (String)values.get( "license" );

		this.cardUri = (String)values.get( "card" );
		this.packUri = (String)values.get( "pack" );
		this.javaVersion = (String)values.get( "java" );

		this.maintainers = (List<Maintainer>)values.get( "maintainers" );
		this.contributors = (List<Contributor>)values.get( "contributors" );

		if( source != null ) this.cardUri = source.toString();

		updateKey();
	}

	public String getProductKey() {
		return productKey;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup( String group ) {
		this.group = group;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact( String artifact ) {
		this.artifact = artifact;
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
		return Release.create( version, timestamp );
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

	public String getCardUri() {
		return cardUri;
	}

	public void setCardUri( String cardUri ) {
		this.cardUri = cardUri;
	}

	public String getPackUri() {
		return packUri;
	}

	public void setPackUri( String packUri ) {
		this.packUri = packUri;
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

	public String[] getResourceUris( String type ) {
		return getPlatformResourceUris( type );
	}

	private void updateKey() {
		/*
		 * The use of '.' as the separator is the most benign of the characters
		 * tested. Changing the separator to a different character will most likely
		 * result in invalid file paths, setting paths, and other undesired side
		 * effects.
		 */
		productKey = group + "." + artifact;
	}

	private String[] getPlatformResourceUris( String path ) {
		String os = System.getProperty( "os.name" );
		String arch = System.getProperty( "os.arch" );

		// This code was originally intended to resolve os/architecture specific
		// resources needed for a product. For the moment, this feature is not
		// needed and this method simply returns an empty set.

		String[] uris;
		Set<String> resources = new HashSet<>();

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

}
