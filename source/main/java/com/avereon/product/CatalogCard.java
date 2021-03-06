package com.avereon.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class CatalogCard extends BaseCard {

	public static final String FILE = "catalog.card";

	private RepoCard repo;

	private long timestamp;

	private Set<String> products = new HashSet<>();

	public CatalogCard() {}

	public CatalogCard( RepoCard repo ) {
		this.repo = repo;
	}

	public RepoCard getRepo() {
		return repo;
	}

	public void setRepo( RepoCard repo ) {
		this.repo = repo;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp( long timestamp ) {
		this.timestamp = timestamp;
	}

	public Set<String> getProducts() {
		return products;
	}

	/**
	 * The set of products in the repository. The set contains the product
	 * artifact ids only.
	 *
	 * @param products The set of product ids
	 */
	public void setProducts( Set<String> products ) {
		this.products = products == null ? Set.of() : new HashSet<>( products );
	}

	public static CatalogCard fromJson( RepoCard repo, InputStream input ) throws IOException {
		CatalogCard catalog = new ObjectMapper().readerFor( new TypeReference<CatalogCard>() {} ).readValue( input );
		catalog.setRepo( repo );
		return catalog;
	}

}
