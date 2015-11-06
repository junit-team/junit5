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

import java.lang.reflect.Method;
import java.util.*;

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.junit5.descriptor.*;
import org.junit.gen5.engine.junit5.execution.TestExecutionNode;
import org.junit.gen5.engine.junit5.execution.TestExecutionNodeResolver;

public class JUnit5TestEngine implements TestEngine {

	// TODO Consider using class names for engine IDs.
	public static final String JUNIT5_ENGINE_ID = "junit5";

	private final JUnit5TestableFactory testableFactory = new JUnit5TestableFactory(this);

	public JUnit5Testable fromUniqueId(String uniqueId) {
		return testableFactory.fromUniqueId(uniqueId);
	}

	public JUnit5Testable fromClassName(String className) {
		return testableFactory.fromClassName(className);
	}

	public JUnit5Testable fromClass(Class<?> clazz) {
		return testableFactory.fromClass(clazz);
	}

	public JUnit5Testable fromMethod(Method testMethod, Class<?> clazz) {
		return testableFactory.fromMethod(testMethod, clazz);
	}

	@Override
	public String getId() {
		return JUNIT5_ENGINE_ID;
	}

	@Override
	public List<TestDescriptor> discoverTests(TestPlanSpecification specification) {
		// TODO Avoid redundant creation of TestDescriptors during this phase
		Set<TestDescriptor> testDescriptors = new LinkedHashSet<>();

		EngineDescriptor engineDescriptor = new EngineDescriptor(this);
		testDescriptors.add(engineDescriptor);

		SpecificationResolver resolver = new SpecificationResolver(this, testDescriptors, engineDescriptor);
		for (TestPlanSpecificationElement element : specification) {
			resolver.resolveElement(element);
		}

		return new ArrayList<>(testDescriptors);
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		// TODO Consider creating a superclass or marker interface for JUnit 5 test descriptors.
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
		for (TestDescriptor testDescriptor : context.getTestDescriptors()) {
			nodes.put(testDescriptor, TestExecutionNodeResolver.forDescriptor(testDescriptor));
		}

		for (TestExecutionNode node : nodes.values()) {

			TestDescriptor currentTestDescriptor = node.getTestDescriptor();

			// @formatter:off
			List<TestExecutionNode> childrenForCurrentNode = context.getTestDescriptors().stream()
					.filter(testDescriptor -> currentTestDescriptor.equals(testDescriptor.getParent()))
					.map(testDescriptor -> nodes.get(testDescriptor))
					.collect(toList());
			// @formatter:on
			node.addChildren(childrenForCurrentNode);
		}
		return nodes;
	}

}
