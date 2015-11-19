package com.github.codeviz.ir;

/**
 * Represents an action, or something to be performed.
 */
public class Process extends FlowchartNode {

	private String description;

	public Process(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return getDescription();
	}
}
