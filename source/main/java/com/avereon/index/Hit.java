package com.avereon.index;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * This class represents both index and search hits since both of these types
 * of hits are very similar and have very similar uses. Index hits have all
 * the information regarding hits while indexing. Search hits can augment index
 * hits with more information regarding the search match.
 */
@Data
//@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
@Accessors( fluent = true )
public class Hit {

	public static final int TAG_PRIORITY = 2;

	public static final int TITLE_PRIORITY = 1;

	public static final int CONTENT_PRIORITY = 0;

	private static final int HIGHEST_PRIORITY = CONTENT_PRIORITY;

	private static final int LOWEST_PRIORITY = TAG_PRIORITY;

	private Document document;

	private String context;

	private String word;

	private int length;

	private List<Integer> coordinates;

	@Deprecated
	//@EqualsAndHashCode.Exclude
	private int line;

	@Deprecated
	//@EqualsAndHashCode.Exclude
	private int index;

	private int priority;

	// This is used for search hits
	private int points;

	public Hit coordinates( List<Integer> coordinates ) {
		this.coordinates = coordinates;
		int count = coordinates.size();
		if( count > 0 ) line = coordinates.get( 0 );
		if( count > 1 ) index = coordinates.get( 1 );
		return this;
	}

	@Override
	public String toString() {
		return "{" + "title=" + document.title() + " length=" + length + " term=" + word + " context=" + context+ " line=" + line+ " index=" + index + " coords=" + coordinates + "}";
	}

}
