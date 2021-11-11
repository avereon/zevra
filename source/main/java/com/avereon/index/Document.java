package com.avereon.index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Reader;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class Document {

	private final Set<String> tags = new CopyOnWriteArraySet<>();

	private Reader content;

	public static Document of( Reader content ) {
		Document document = new Document();
		document.content = content;
		return document;
	}

	public void addTags( Collection<String> tags ) {
		this.tags.addAll( tags );
	}

}
