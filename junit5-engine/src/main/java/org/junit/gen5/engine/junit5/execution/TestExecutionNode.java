/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public abstract class TestExecutionNode<T extends TestDescriptor> {

	private final T testDescriptor;
	private TestExecutionNode parent = null;
	private List<TestExecutionNode> children = new LinkedList<>();

	protected TestExecutionNode(T testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	public void addChild(TestExecutionNode childNode) {
		this.children.add(childNode);
		childNode.parent = this;
	}

	public void addChildren(List<TestExecutionNode> childNodes) {
		childNodes.forEach(childNode -> addChild(childNode));
	}

	public abstract void execute(EngineExecutionContext context);

	public Object createTestInstance() {
		return (getParent() != null) ? getParent().createTestInstance() : null;
	}

	public T getTestDescriptor() {
		return testDescriptor;
	}

	public TestExecutionNode getParent() {
		return parent;
	}

	public List<TestExecutionNode> getChildren() {
		return Collections.unmodifiableList(children);
	}
}
