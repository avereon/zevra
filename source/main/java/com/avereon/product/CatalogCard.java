package com.avereon.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

@Getter
public class CatalogCard extends BaseCard {

	public static final String FILE = "catalog.card";

	// FIXME This is a temporary hack to get the repo card into the catalog card
	@Setter
	private RepoCard repo;

	@Setter
	private long timestamp;

	/**
	 * The set of products in the repository. The set contains the product
	 * artifact ids only.
	 */
	private Set<String> products = new HashSet<>();

	public CatalogCard() {}

	public CatalogCard( RepoCard repo ) {
		this.repo = repo;
	}

	public void setProducts( Set<String> products ) {
		this.products = products == null ? Set.of() : new HashSet<>( products );
	}

	public static CatalogCard fromJson( InputStream input ) throws IOException {
		return new ObjectMapper().readerFor( new TypeReference<CatalogCard>() {} ).readValue( input );
	}

}
