package com.avereon.index;

import com.avereon.util.TextUtil;
import com.avereon.util.TokenReplacingReader;
import lombok.CustomLog;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@Accessors( fluent = true )
@CustomLog
public class Document {

	public enum MediaType {
		TEXT,
		HTML
	}

	/**
	 * The media type of the document. This is set to SupportedMediaType.TEXT by
	 * default but should be changed to match the media type of the document. For
	 * example, when indexing an HTML document, the media type should be set to
	 * SupportedMediaType.HTML so the HTML tags are not indexed.
	 */
	private MediaType mediaType;

	/**
	 * The document URI. The URI is a unique identifier for this document,
	 * regardless of language, replacement values, or the location of the content.
	 * The uri is not the same as the {@link #url}, but are often related.
	 */
	private URI uri;

	/**
	 * The icon key or URL.
	 */
	private String icon;

	/**
	 * The document title.
	 */
	private String title;

	/**
	 * The document tags.
	 */
	private Set<String> tags;

	/**
	 * The URL from where to load the document content. The url is not the same as
	 * the {@link #uri}, but are often related.
	 */
	private URL url;

	/**
	 * The content loaded from the {@link #url}. This is the data that will be
	 * indexed.
	 */
	private String content;

	/**
	 * The replacement values for the content. These are be replaced in the
	 * content before the content is indexed.
	 */
	private Map<String, String> values;

	/**
	 * The document properties. These properties have no special meaning to the
	 * indexer but are available to store meta-data regarding the document.
	 */
	private Map<String, Object> properties;

	@EqualsAndHashCode.Exclude
	private int searchPoints;

	@EqualsAndHashCode.Exclude
	private int searchPriority;

	public Document() {
		this( null, null, null );
	}

	public Document( URI uri, String icon, String title ) {
		this.uri = uri;
		this.icon = icon;
		this.title = title;
		this.mediaType = MediaType.TEXT;
	}

	public Document( URI uri, String icon, String title, URL url ) {
		this( uri, icon, title );
		url( url );
	}

	public Document( URI uri, String icon, String title, String content ) {
		this( uri, icon, title );
		content( content );
	}

	public Set<String> tags() {
		return new HashSet<>( tags == null ? Set.of() : tags );
	}

	public Document tags( Collection<String> tags ) {
		if( this.tags == null ) this.tags = new CopyOnWriteArraySet<>();
		this.tags.addAll( tags );
		return this;
	}

	public Map<String, Object> properties() {
		if( properties == null ) properties = new ConcurrentHashMap<>();
		return properties;
	}

	public Reader reader() throws IOException {
		Reader reader;

		if( content != null ) {
			if( TextUtil.isEmpty( content ) ) log.atConfig().log( "Document reader has empty content: " + uri() );
			reader = new StringReader( content );
		} else if( url != null ) {
			reader = new InputStreamReader( url.openStream(), StandardCharsets.UTF_8 );
		} else {
			return null;
		}

		return new TokenReplacingReader( reader, values );
	}

}
