package com.avereon.index;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Document {

	private final Set<String> tags;

	private InputStream data;

	public Document() {
		tags = new CopyOnWriteArraySet<>();
	}

	public static Document of( InputStream data ) {
		Document document = new Document();
		document.data = data;
		return document;
	}

	public void addTags( Collection<String> tags ) {
		this.tags.addAll( tags );
	}

	public Set<String> getTags() {
		return new HashSet<>( tags );
	}

}
