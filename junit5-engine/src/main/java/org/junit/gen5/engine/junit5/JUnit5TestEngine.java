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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;
import org.junit.gen5.engine.junit5.descriptor.ClassNameTestDescriptorResolver;
import org.junit.gen5.engine.junit5.descriptor.TestDescriptorResolver;
import org.junit.gen5.engine.junit5.descriptor.TestDescriptorResolverRegistry;
import org.junit.gen5.engine.junit5.descriptor.UniqueIdTestDescriptorResolver;
import org.junit.gen5.engine.junit5.execution.TestExecutionNode;
import org.junit.gen5.engine.junit5.execution.TestExecutionNodeResolver;

public class JUnit5TestEngine implements TestEngine {

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return "junit5";
	}

	@Override
	public List<TestDescriptor> discoverTests(TestPlanSpecification specification, TestDescriptor engineDescriptor) {
		// TODO Avoid redundant creation of TestDescriptors during this phase
		Set<TestDescriptor> testDescriptors = new LinkedHashSet<>();
		resolveSpecification(specification, engineDescriptor, testDescriptors);
		return new ArrayList<>(testDescriptors);
	}

	// Isolated this step to allow easier experimentation / branching / pull requesting
	private void resolveSpecification(TestPlanSpecification specification, TestDescriptor engineDescriptor,
			Set<TestDescriptor> testDescriptors) {
		TestDescriptorResolverRegistry testDescriptorResolverRegistry = createResolverRegistry();

		for (TestPlanSpecificationElement element : specification) {
			testDescriptors.addAll(resolveElement(testDescriptorResolverRegistry, engineDescriptor, element));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Set<TestDescriptor> resolveElement(TestDescriptorResolverRegistry testDescriptorResolverRegistry,
			TestDescriptor root, TestPlanSpecificationElement element) {
		Set<TestDescriptor> testDescriptors = new LinkedHashSet<>();
		TestDescriptorResolver testDescriptorResolver = testDescriptorResolverRegistry.forType(element.getClass());
		TestDescriptor descriptor = testDescriptorResolver.resolve(root, element);
		//Get rid of null check
		if (descriptor != null) {
			testDescriptors.add(descriptor);
			testDescriptors.addAll(testDescriptorResolver.resolveChildren(descriptor, element));
		}
		return testDescriptors;
	}

	private TestDescriptorResolverRegistry createResolverRegistry() {
		// TODO Look up TestDescriptorResolverRegistry within the
		// ApplicationExecutionContext
		TestDescriptorResolverRegistry testDescriptorResolverRegistry = new TestDescriptorResolverRegistry();
		testDescriptorResolverRegistry.addResolver(ClassNameSpecification.class, new ClassNameTestDescriptorResolver());
		testDescriptorResolverRegistry.addResolver(UniqueIdSpecification.class, new UniqueIdTestDescriptorResolver());
		return testDescriptorResolverRegistry;
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
