package com.github.codeviz.ir;

import javax.annotation.Nullable;

/**
 * Abstract class for all nodes in a flowchart.
 *
 * All nodes contain a (potenially null) reference to the next node
 * in the flowchart. A null nextNode may have different meanings
 * depending on the context.
 */
public abstract class Node {
	@Nullable protected Node nextNode;

	public Node getNextNode() {
		return this.nextNode;
	}

	public void setNextNode(Node nextNode) {
		this.nextNode = nextNode;
	}

	@Override
	abstract public String toString();
}
