package com.avereon.index;

import com.avereon.util.TokenReplacingReader;
import lombok.Data;
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
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@Accessors( fluent = true )
public class Document {

	private final URI uri;

	private final String icon;

	private final String title;

	private Set<String> tags;

	private boolean store;

	private URL url;

	private String content;

	private Map<String, String> values;

	public Document( URI uri, String icon, String title ) {
		this.uri = uri;
		this.icon = icon;
		this.title = title;
	}

	public Document( URI uri, String icon, String title, URL url ) {
		this( uri, icon, title );
		url( url );
	}

	public Document( URI uri, String icon, String title, String content ) {
		this( uri, icon, title );
		content( content );
	}

	private boolean store;

	public Set<String> tags() {
		return new HashSet<>( tags == null ? Set.of() : tags );
	}

	public Document tags( Collection<String> tags ) {
		if( this.tags == null ) this.tags = new CopyOnWriteArraySet<>();
		this.tags.addAll( tags );
		return this;
	}

	public Reader reader() throws IOException {
		Reader reader = null;
		if( url != null ) reader = new InputStreamReader( url.openStream(), StandardCharsets.UTF_8 );
		if( content != null ) reader = new StringReader( content );
		if( reader == null ) return null;
		return new TokenReplacingReader( reader, values );
	}

}
