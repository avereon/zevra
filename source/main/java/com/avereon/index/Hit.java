package com.avereon.index;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@Builder
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

	@Override
	public String toString() {
		return "{" + "title=" + document.title() + " index=" + index + " length=" + length + " term=" + word + " context=" + context + " line=" + line + "}";
	}

}
