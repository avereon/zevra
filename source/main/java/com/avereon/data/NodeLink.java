package com.avereon.data;

import lombok.Getter;

@Getter
public class NodeLink<T extends Node> extends IdNode {

	private final T node;

	public NodeLink( T node ) {
		this.node = node;
	}

	@Override
	public String toString() {
		return "NodeLink@" + node;
	}

	public static <T extends Node> NodeLink<T> of( T node ) {
		return new NodeLink<>( node );
	}

}
