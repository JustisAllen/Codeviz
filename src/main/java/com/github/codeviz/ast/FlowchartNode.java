package com.github.codeviz.ast;

import org.anarres.graphviz.builder.GraphVizable;
import org.anarres.graphviz.builder.GraphVizScope;

/**
 * Abstract class for all nodes in a flowchart.
 *
 * All nodes optionally contain a reference to the next node
 * in the flowchart. An empty nextNode may have different meanings
 * depending on the context.
 */
public abstract class FlowchartNode implements GraphVizable {
  protected static final GraphVizScope scope = new Scope();

  private static class Scope implements GraphVizScope {}
}
