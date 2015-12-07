package com.github.codeviz.ast;

import org.anarres.graphviz.builder.GraphVizGraph;

import javax.annotation.Nonnull;

/**
 * Represents an action, or something to be performed;
 * then exits the program.
 */
public class Terminal extends FlowchartNode {
  protected String description; 

  public Terminal(String description) {
    this.description = description;
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  public void toGraphViz(@Nonnull GraphVizGraph graph) {
    graph.node(super.scope, this)
      .label(getDescription())
      .shape("oval");
  }
}
