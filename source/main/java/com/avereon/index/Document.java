package com.avereon.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Reader;
import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors( fluent = true )
public class Document {

	private final Set<String> tags = new CopyOnWriteArraySet<>();

	private URI uri;

	private Reader content;

	public static Document of( URI uri, Reader content ) {
		return new Document().content(content).uri(uri);
	}

	public void addTags( Collection<String> tags ) {
		this.tags.addAll( tags );
	}

}
