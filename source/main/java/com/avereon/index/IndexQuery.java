package com.avereon.index;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Builder
@Accessors( fluent = true )
public class IndexQuery {

	@Singular
	private List<String> terms;

}
