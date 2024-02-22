package com.avereon.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Getter
@Setter
public class CatalogCard extends BaseCard {

	public static final String FILE = "catalog.card";

	/**
	 * The timestamp the catalog was last updated.
	 */
	private long timestamp;

	/**
	 * The set of products in the repository.
	 * <p>
	 * The set contains only the product artifact ids, not the full product cards.
	 */
	private Set<String> products;

	public CatalogCard() {}

	public static CatalogCard fromJson( InputStream input ) throws IOException {
		return new ObjectMapper().readerFor( new TypeReference<CatalogCard>() {} ).readValue( input );
	}

}
