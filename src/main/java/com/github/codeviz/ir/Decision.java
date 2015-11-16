package com.github.codeviz.ir;

import javax.annotation.Nullable;

/**
 * Represents conditional logic in which one of two branches are taken
 * based on some condition. The 'false' branch is not always required,
 * but the 'true' branch is.
 */
public class Decision extends Node {

	private String condition;
	private Node trueBranch;
	@Nullable private Node falseBranch;

	public Decision(String condition) {
		this.condition = condition;
	}

	public String getCondition() {
		return this.condition;
	}

	public Node getTrueBranch() {
		return this.trueBranch;
	}

	@Nullable public Node getFalseBranch() {
		return this.falseBranch;
	}

	public void setTrueBranch(Node trueBranch) {
		this.trueBranch = trueBranch;
	}

	public void setFalseBranch(Node falseBranch) {
		this.falseBranch = falseBranch;
	}

	@Override
	public String toString() {
		return getCondition();
	}
}
