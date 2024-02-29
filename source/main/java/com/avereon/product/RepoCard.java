package com.avereon.product;

import com.avereon.util.UriUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Accessors( chain = true )
@JsonIgnoreProperties( ignoreUnknown = true )
public class RepoCard extends BaseCard {

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
