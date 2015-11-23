package com.github.codeviz.ir;

import org.anarres.graphviz.builder.GraphVizable;
import org.anarres.graphviz.builder.GraphVizScope;

import java.util.Optional;

/**
 * Abstract class for all nodes in a flowchart.
 *
 * All nodes optionally contain a reference to the next node
 * in the flowchart. An empty nextNode may have different meanings
 * depending on the context.
 */
public abstract class FlowchartNode implements GraphVizable {
	protected static final GraphVizScope scope = new Scope();
	protected Optional<FlowchartNode> nextNode = Optional.empty();

	public Optional<FlowchartNode> getNextNode() {
		return this.nextNode;
	}

	public void setNextNode(Optional<FlowchartNode> nextNode) {
		this.nextNode = nextNode;
	}

	@Override
	abstract public String toString();

	private static class Scope implements GraphVizScope {}
}
