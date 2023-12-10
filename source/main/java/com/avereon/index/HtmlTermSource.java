package com.avereon.index;

import com.avereon.util.IoUtil;
import lombok.CustomLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@CustomLog
public class HtmlTermSource implements TermSource {

	private final Document document;

	public HtmlTermSource( Document document ) {
		this.document = document;
	}

	@Override
	public Stream<Term> index() throws IOException {
		document.title( getHtmlTitle( document ) );
		return getTextNodes( document ).stream().map( n -> new Term( null, n.text(), n.text().length(), getCoordinates( n ) ) );
	}

	private String getHtmlTitle( Document document ) {
		return getHtmlRoot( document ).select( "html > head > title" ).text();
	}

	private List<TextNode> getTextNodes( Document document ) {
		List<TextNode> elements = new ArrayList<>();

		for( Node child : getHtmlRoot( document ).getAllElements() ) {
			if( child instanceof TextNode text ) {
				elements.add( text );
			}
		}

		return elements;
	}

	private List<Integer> getCoordinates( Node node ) {
		List<Integer> coords = new ArrayList<>();
		while( node != null ) {
			coords.add( node.siblingIndex() );
			node = node.parentNode();
		}
		Collections.reverse( coords );
		return coords;
	}

	private org.jsoup.nodes.Document getHtmlRoot( Document document ) {
		org.jsoup.nodes.Document htmlRoot = (org.jsoup.nodes.Document)document.properties().get( "org.jsoup.nodes.Document" );
		if( htmlRoot == null ) {
			try( Reader reader = document.reader() ) {
				htmlRoot = Jsoup.parse( IoUtil.toString( reader ) );
				document.properties().put( "org.jsoup.nodes.Document", htmlRoot );
			} catch( IOException exception ) {
				log.atWarn( exception );
			}
		}
		return htmlRoot;
	}

}
