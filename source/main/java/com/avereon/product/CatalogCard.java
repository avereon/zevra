package com.avereon.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Getter
@JsonInclude( JsonInclude.Include.NON_NULL )
@JsonIgnoreProperties( ignoreUnknown = true )
public class CatalogCard extends RepoCard {

	public static final String FILE = "catalog.card";

	@Setter
	private long timestamp;

	/**
	 * The set of products in the repository.
	 * <p>
	 * The set contains the product artifact ids only.
	 */
	private Set<String> products = new HashSet<>();

	public CatalogCard() {}

	public void setProducts( Set<String> products ) {
		this.products = products == null ? Set.of() : new HashSet<>( products );
	}

	@Override
	public RepoCard copyFrom( RepoCard card ) {
		super.copyFrom( card );
		if( card == null ) return null;
		((CatalogCard)card).setTimestamp( timestamp );
		((CatalogCard)card).setProducts( products );
		return this;
	}

	public static CatalogCard fromJson( InputStream input ) throws IOException {
		return new ObjectMapper().readerFor( new TypeReference<CatalogCard>() {} ).readValue( input );
	}

}
