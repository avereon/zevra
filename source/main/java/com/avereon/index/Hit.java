package com.avereon.index;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * This class represents both index and search hits since both of these types
 * of hits are very similar and have very similar uses. Index hits have all
 * the information regarding hits while indexing. Search hits can augment index
 * hits with more information regarding the search match.
 */
@Data
@Builder( toBuilder = true )
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

	private int line;

	private int index;

	private int length;

	private int priority;

	// This is used for search hits
	private int points;

	@Override
	public String toString() {
		return "{" + "title=" + document.title() + " index=" + index + " length=" + length + " term=" + word + " context=" + context + " line=" + line + "}";
	}

}
