package com.avereon.data;

public class NodeLink<T extends Node> extends IdNode {

	private final T node;

	public NodeLink( T node ) {
		this.node = node;
	}

	public T getNode() {
		return node;
	}

	@Override
	public String toString() {
		return "NodeLink@" + node;
	}
}
