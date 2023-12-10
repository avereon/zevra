package com.avereon.index;

import com.avereon.util.TextUtil;
import com.avereon.util.TokenReplacingReader;
import lombok.CustomLog;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Data
@Accessors( fluent = true )
@CustomLog
public class Document {

	public enum SupportedMediaType {
		TEXT,
		HTML
	}

	private URI uri;

	private String icon;

	private String title;

	private SupportedMediaType mediaType;

	private Set<String> tags;

	private boolean store;

	private URL url;

	private String content;

	private Map<String, String> values;

	private Map<String, Object> properties;

	public Document() {
		this( null, null, null );
	}

	public Document( URI uri, String icon, String title ) {
		this.uri = uri;
		this.icon = icon;
		this.title = title;
		this.mediaType = SupportedMediaType.TEXT;
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
			if( TextUtil.isEmpty( content ) ) log.atWarn().log( "Document reader has empty content: " + uri() );
			reader = new StringReader( content );
		} else if( url != null ) {
			//log.atConfig().log( "Reader from url: " + url);
			reader = new InputStreamReader( url.openStream(), StandardCharsets.UTF_8 );
		} else {
			//log.atConfig().log( "No content" );
			return null;
		}

		return new TokenReplacingReader( reader, values );
	}

}
