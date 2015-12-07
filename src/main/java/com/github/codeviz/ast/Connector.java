package com.github.codeviz.ast;

import org.anarres.graphviz.builder.GraphVizGraph;

import javax.annotation.Nonnull;

public class Connector extends SingleExitNode {

  @Override
  public void toGraphViz(@Nonnull GraphVizGraph graph) {
    graph.node(super.scope, this)
      //.label(getDescription())
      .shape("circle");
  }
}
