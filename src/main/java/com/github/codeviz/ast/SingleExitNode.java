package com.github.codeviz.ast;

import java.util.Optional;

import javax.annotation.Nonnull;

public abstract class SingleExitNode extends FlowchartNode {
  protected Optional<FlowchartNode> nextNode = Optional.empty();

  public Optional<FlowchartNode> getNextNode() {
    return this.nextNode;
  }

  public void setNextNode(@Nonnull FlowchartNode nextNode) {
    this.nextNode = Optional.of(nextNode);
  }
}
