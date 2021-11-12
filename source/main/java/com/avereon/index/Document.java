package com.avereon.index;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Reader;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@RequiredArgsConstructor
@Accessors( fluent = true )
public class Document {

	private final URI uri;

	private final String name;

	private final Reader content;

	private final Set<String> tags = new CopyOnWriteArraySet<>();

	public Set<String> tags() {
		return new HashSet<>( tags );
	}

	public Document tag( String tag ) {
		return tags( tag );
	}

	public Document tags( String... tags ) {
		return tags( List.of( tags ) );
	}

	public Document tags( Collection<String> tags ) {
		this.tags.addAll( tags );
		return this;
	}

}
