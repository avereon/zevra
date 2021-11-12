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
