package com.avereon.product;

import com.avereon.util.UriUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties( ignoreUnknown = true )
public class RepoCard extends BaseCard {

	public static final String CONFIG = "/META-INF/repositories.json";

	private String name;

	private List<String> icons;

	private String url;

	public RepoCard() {
		this( null );
	}

	public RepoCard( String url ) {
		this.url = url;
		this.icons = List.of();
	}

	public RepoCard fromJson( InputStream input ) throws IOException {
		return fromJson( input, null );
	}

	public RepoCard fromJson( InputStream input, URI source ) throws IOException {
		RepoCard card = new ObjectMapper().readerFor( new TypeReference<RepoCard>() {} ).readValue( input );
		if( source != null ) this.url = UriUtil.removeQueryAndFragment( source ).toString();
		return copyFrom( card );
	}

	public RepoCard setInternalId( String internalId ) {
		super.setInternalId( internalId );
		return this;
	}

	public String getName() {
		return name;
	}

	public RepoCard setName( String name ) {
		this.name = name;
		return this;
	}

	public List<String> getIcons() {
		return icons;
	}

	public RepoCard setIcons( List<String> icons ) {
		this.icons = Collections.unmodifiableList( icons == null ? List.of() : icons );
		return this;
	}

	public String getUrl() {
		return url;
	}

	public RepoCard setUrl( String url ) {
		this.url = url;
		return this;
	}

	public RepoCard copyFrom( RepoCard card ) {
		if( card == null ) return null;
		this.name = card.name;
		//this.icon = card.icon;
		this.setIcons( card.getIcons() );
		this.url = card.url;
		return this;
	}

	@Override
	public String toString() {
		return "[" + name + "] " + url;
	}

	@Override
	public boolean equals( Object object ) {
		if( this == object ) return true;
		if( !(object instanceof RepoCard) ) return false;
		RepoCard that = (RepoCard)object;
		return Objects.equals( this.url, that.url );
	}

	@Override
	public int hashCode() {
		return Objects.hash( url );
	}

}
