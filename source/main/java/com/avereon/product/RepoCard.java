package com.avereon.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/*
FIXME The catalog.card file, and therefore this class, serve two purposes but
probably should not. One purpose is to specify the repository location of the
market/store/repo. The other purpose is to specify the products available at
the market/store/repo.

This dual purpose may simply be a historical artifact of how the original repo
was set up and operated. It provided the initial repo configuration for the
program and was a reasonable repo index at the same time.
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class RepoCard {

	public static final String CONFIG = "/META-INF/repositories.json";

	private String name;

	private String icon;

	private String repo;

	private boolean enabled;

	private boolean removable;

	private int rank;

	public RepoCard() {}

	public RepoCard( String repo ) {
		this.repo = repo;
	}

	public static List<RepoCard> forProduct( Class<?> loader ) throws IOException {
		try( InputStream input = loader.getResourceAsStream( CONFIG ) ) {
			return loadCards( input );
		}
	}

	public static List<RepoCard> loadCards( InputStream input ) throws IOException {
		return new ObjectMapper().readerFor( new TypeReference<List<RepoCard>>() {} ).readValue( input );
	}

	public String getName() {
		return name;
	}

	public RepoCard setName( String name ) {
		this.name = name;
		return this;
	}

	public String getIcon() {
		return icon;
	}

	public RepoCard setIcon( String icon ) {
		this.icon = icon;
		return this;
	}

	public String getRepo() {
		return repo;
	}

	public RepoCard setRepo( String repo ) {
		this.repo = repo;
		return this;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public RepoCard setEnabled( boolean enabled ) {
		this.enabled = enabled;
		return this;
	}

	public boolean isRemovable() {
		return removable;
	}

	public RepoCard setRemovable( boolean removable ) {
		this.removable = removable;
		return this;
	}

	public int getRank() {
		return rank;
	}

	public RepoCard setRank( int rank ) {
		this.rank = rank;
		return this;
	}

	public RepoCard copyFrom( RepoCard card ) {
		this.name = card.name;
		this.icon = card.icon;
		this.repo = card.repo;
		this.enabled = card.enabled;
		this.removable = card.removable;
		this.rank = card.rank;
		return this;
	}

	@Override
	public String toString() {
		return repo;
	}

	@Override
	public boolean equals( Object object ) {
		if( this == object ) return true;
		if( !(object instanceof RepoCard) ) return false;
		RepoCard that = (RepoCard)object;
		return Objects.equals( this.repo, that.repo );
	}

	@Override
	public int hashCode() {
		return Objects.hash( repo );
	}

}
