package com.avereon.index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors( fluent = true )
public class Hit {

	public static final int TAG_PRIORITY = 2;

	public static final int NAME_PRIORITY = 1;

	public static final int CONTENT_PRIORITY = 0;

	private static final int HIGEST_PRIORITY = CONTENT_PRIORITY;

	private static final int LOWEST_PRIORITY = TAG_PRIORITY;

	private Document document;

	private String word;

	private String context;

	private int line;

	private int index;

	private int length;

	private int priority;

	@Override
	public String toString() {
		return document.name();
	}

}
