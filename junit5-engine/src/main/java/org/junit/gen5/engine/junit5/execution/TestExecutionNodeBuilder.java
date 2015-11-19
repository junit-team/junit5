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

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NestedClassTestDescriptor;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class TestExecutionNodeBuilder {

	public EngineTestExecutionNode buildExecutionTree(EngineDescriptor engineDescriptor) {
		EngineTestExecutionNode root = new EngineTestExecutionNode(engineDescriptor);
		buildChildrenNodes(engineDescriptor, root);
		return root;
	}

	private void buildExecutionNode(TestDescriptor descriptor, TestExecutionNode parent) {
		TestExecutionNode newNode = createNode(descriptor);
		parent.addChild(newNode);
		buildChildrenNodes(newNode.getTestDescriptor(), newNode);
	}

	private void buildChildrenNodes(TestDescriptor parentDescriptor, TestExecutionNode parent) {
		parentDescriptor.getChildren().stream().forEach(testDescriptor -> buildExecutionNode(testDescriptor, parent));
	}

	private TestExecutionNode createNode(TestDescriptor testDescriptor) {
		Preconditions.notNull(testDescriptor, "testDescriptor must not be null");

		if (testDescriptor.getClass() == MethodTestDescriptor.class) {
			return new MethodExecutionNode((MethodTestDescriptor) testDescriptor);
		}
		else if (testDescriptor.getClass() == ClassTestDescriptor.class) {
			return new ClassExecutionNode((ClassTestDescriptor) testDescriptor);
		}
		else if (testDescriptor.getClass() == NestedClassTestDescriptor.class) {
			return new NestedClassExecutionNode((NestedClassTestDescriptor) testDescriptor);
		}
		else if (testDescriptor.getClass() == EngineDescriptor.class) {
			return new EngineTestExecutionNode((EngineDescriptor) testDescriptor);
		}

		// else
		throw new IllegalArgumentException("Unsupported TestDescriptor type: " + testDescriptor.getClass().getName());
	}

}
