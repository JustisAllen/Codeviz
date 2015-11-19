package com.github.codeviz.ir;

import java.util.Optional;

/**
 * Abstract class for all nodes in a flowchart.
 *
 * All nodes optionally contain a reference to the next node
 * in the flowchart. An empty nextNode may have different meanings
 * depending on the context.
 */
public abstract class FlowchartNode {
	protected Optional<FlowchartNode> nextNode = Optional.empty();

	public Optional<FlowchartNode> getNextNode() {
		return this.nextNode;
	}

	public void setNextNode(Optional<FlowchartNode> nextNode) {
		this.nextNode = nextNode;
	}

	@Override
	abstract public String toString();
}
