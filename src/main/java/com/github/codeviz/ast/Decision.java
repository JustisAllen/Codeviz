package com.github.codeviz.ast;

import org.anarres.graphviz.builder.GraphVizGraph;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Represents conditional logic in which one of two branches are taken
 * based on some condition. The 'false' branch is not always required,
 * but the 'true' branch is.
 */
public class Decision extends FlowchartNode {
  protected String condition;
  protected FlowchartNode trueBranch;
  protected Optional<FlowchartNode> falseBranch = Optional.empty();

  public Decision(String condition) {
    this.condition = condition;
  }

  public String getCondition() {
    return this.condition;
  }

  public FlowchartNode getTrueBranch() {
    return this.trueBranch;
  }

  public Optional<FlowchartNode> getFalseBranch() {
    return this.falseBranch;
  }

  public void setTrueBranch(@Nonnull FlowchartNode trueBranch) {
    this.trueBranch = trueBranch;
  }

  public void setFalseBranch(@Nonnull FlowchartNode falseBranch) {
    this.falseBranch = Optional.of(falseBranch);
  }

  @Override
  public void toGraphViz(@Nonnull GraphVizGraph graph) {
    graph.node(super.scope, this)
      .label(getCondition())
      .shape("diamond");
    trueBranch.toGraphViz(graph);
    graph.edge(super.scope, this, trueBranch);
    if (falseBranch.isPresent()) {
      falseBranch.get().toGraphViz(graph);
      graph.edge(super.scope, this, falseBranch.get());
    }
  }
}
