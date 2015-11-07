package com.github.codeviz.ir;

/**
 * Represents an action, or something to be performed.
 */
public class Process extends Node {

	private String description;

	Process(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}
}
