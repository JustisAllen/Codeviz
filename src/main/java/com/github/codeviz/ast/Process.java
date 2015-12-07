package com.github.codeviz.ast;

import org.anarres.graphviz.builder.GraphVizGraph;

import javax.annotation.Nonnull;

/**
 * Represents an action, or something to be performed.
 */
public class Process extends SingleExitNode {
  protected String description; 

  public Process(String description) {
    this.description = description;
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  public void toGraphViz(@Nonnull GraphVizGraph graph) {
    graph.node(super.scope, this)
      .label(getDescription())
      .shape("box");
    if (getNextNode().isPresent()) {
      FlowchartNode nextNode = getNextNode().get();
      nextNode.toGraphViz(graph);
      graph.edge(super.scope, this, nextNode);
    }
  }
}
