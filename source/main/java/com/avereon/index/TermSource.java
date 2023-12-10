package com.avereon.index;

import java.io.IOException;
import java.util.stream.Stream;

public interface TermSource {

	Stream<Term> index() throws IOException;

}
