package com.avereon.index;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Reader;
import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@RequiredArgsConstructor
@Accessors( fluent = true )
public class Document {

	private final URI uri;

	private final Reader content;

	private final Set<String> tags = new CopyOnWriteArraySet<>();

	public static Document of( URI uri, Reader content ) {
		return new Document( uri, content );
	}

	public void addTags( Collection<String> tags ) {
		this.tags.addAll( tags );
	}

}
