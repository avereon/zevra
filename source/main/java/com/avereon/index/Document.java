package com.avereon.index;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Reader;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@Accessors( fluent = true )
public class Document {

	private final URI uri;

	private final String icon;

	private final String title;

	private Set<String> tags;

	@EqualsAndHashCode.Exclude
	private final Reader content;

	private boolean store;

	public Set<String> tags() {
		return new HashSet<>( tags == null ? Set.of() : tags );
	}

	public Document tags( Collection<String> tags ) {
		if( this.tags == null ) this.tags = new CopyOnWriteArraySet<>();
		this.tags.addAll( tags );
		return this;
	}

}
