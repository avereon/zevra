package com.avereon.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * This class represents both index and search hits since both of these hit
 * types are very similar and have very similar uses. Index hits have all
 * the information regarding hits while indexing. Search hits can augment index
 * hits with more information regarding the search match.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors( chain = true )
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

	// This is used for search hits
	private int points;

	private int priority;

	@Override
	public String toString() {
		return "{" + "title=" + document.title() + " length=" + length + " term=" + word + " context=" + context + " coords=" + coordinates + "}";
	}

}
