/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.junit5.descriptor.SpecificationResolver;
import org.junit.gen5.engine.junit5.execution.TestExecutionNode;
import org.junit.gen5.engine.junit5.execution.TestExecutionNodeResolver;

public class JUnit5TestEngine implements TestEngine {

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return "junit5";
	}

	@Override
	public void discoverTests(TestPlanSpecification specification, EngineDescriptor engineDescriptor) {
		Preconditions.notNull(specification, "specification must not be null");
		Preconditions.notNull(engineDescriptor, "engineDescriptor must not be null");

		// TODO Avoid redundant creation of TestDescriptors during this phase
		Set<TestDescriptor> testDescriptors = new LinkedHashSet<>();
		// Todo: testDescriptors are no longer needed
		resolveSpecification(specification, engineDescriptor, testDescriptors);
	}

	// Isolated this step to allow easier experimentation / branching / pull requesting
	private void resolveSpecification(TestPlanSpecification specification, EngineDescriptor engineDescriptor,
			Set<TestDescriptor> testDescriptors) {
		SpecificationResolver resolver = new SpecificationResolver(testDescriptors, engineDescriptor);
		for (TestPlanSpecificationElement element : specification) {
			resolver.resolveElement(element);
		}
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		// TODO Consider creating a superclass or marker interface for JUnit 5 test
		// descriptors.
		return testDescriptor.getUniqueId().startsWith(getId());
	}

	@Override
	public void execute(EngineExecutionContext context) {

		Map<TestDescriptor, TestExecutionNode> nodes = buildTestExecutionNodesTree(context);

		List<TestExecutionNode> rootNodes = findRootNodes(nodes);

		startRootNodesExecution(context, rootNodes);
	}

	private void startRootNodesExecution(EngineExecutionContext context, List<TestExecutionNode> rootNodes) {
		for (TestExecutionNode rootNode : rootNodes) {
			rootNode.execute(context);
		}
	}

	private List<TestExecutionNode> findRootNodes(Map<TestDescriptor, TestExecutionNode> nodes) {
		List<TestExecutionNode> rootNodes = new LinkedList<>();
		for (TestExecutionNode node : nodes.values()) {

			TestDescriptor currentTestDescriptor = node.getTestDescriptor();
			if (currentTestDescriptor.getParent() == null) {
				rootNodes.add(node);
			}
		}
		return rootNodes;
	}

	private Map<TestDescriptor, TestExecutionNode> buildTestExecutionNodesTree(EngineExecutionContext context) {
		Map<TestDescriptor, TestExecutionNode> nodes = new HashMap<>();
		for (TestDescriptor testDescriptor : context.getEngineDescriptor().allChildren()) {
			nodes.put(testDescriptor, TestExecutionNodeResolver.forDescriptor(testDescriptor));
		}

		for (TestExecutionNode node : nodes.values()) {

			TestDescriptor currentTestDescriptor = node.getTestDescriptor();

			// @formatter:off
			List<TestExecutionNode> childrenForCurrentNode = context.getEngineDescriptor().allChildren().stream()
					.filter(testDescriptor -> currentTestDescriptor.equals(testDescriptor.getParent()))
					.map(testDescriptor -> nodes.get(testDescriptor))
					.collect(toList());
			// @formatter:on
			node.addChildren(childrenForCurrentNode);
		}
		return nodes;
	}

}
