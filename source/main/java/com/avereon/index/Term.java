package com.avereon.index;

import java.util.List;

public record Term( String context, String value, int length, List<Integer> coordinates ){}
