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
  private String condition;
  private FlowchartNode trueBranch;
  private Optional<FlowchartNode> falseBranch = Optional.empty();

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

  public void setTrueBranch(FlowchartNode trueBranch) {
    this.trueBranch = trueBranch;
  }

  public void setFalseBranch(Optional<FlowchartNode> falseBranch) {
    this.falseBranch = falseBranch;
  }

  @Override
  public void toGraphViz(@Nonnull GraphVizGraph graph) {
    // TODO: implement
  }

  @Override
  public String toString() {
    return getCondition();
  }
}