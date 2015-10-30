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

import java.util.*;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;
import org.junit.gen5.engine.junit5.descriptor.*;
import org.junit.gen5.engine.junit5.execution.TestExecutionNode;
import org.junit.gen5.engine.junit5.execution.TestExecutionNodeResolver;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

public class JUnit5TestEngine implements TestEngine {

	// TODO Consider using class names for engine IDs.
	private static final String ENGINE_ID = "junit5";

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	@Override
	public List<TestDescriptor> discoverTests(TestPlanSpecification specification) {
		// TODO lookup TestDescriptorResolverRegistry within the ApplicationExecutionContext
		TestDescriptorResolverRegistry testDescriptorResolverRegistry = new TestDescriptorResolverRegistry();
		testDescriptorResolverRegistry.addResolver(ClassNameSpecification.class, new ClassNameTestDescriptorResolver());
		testDescriptorResolverRegistry.addResolver(UniqueIdSpecification.class, new UniqueIdTestDescriptorResolver());

		// TODO Avoid redundant creation of TestDescriptors during this phase
		Set<TestDescriptor> testDescriptors = new LinkedHashSet<>();
		EngineDescriptor root = new EngineDescriptor(this);
		testDescriptors.add(root);

		for (TestPlanSpecificationElement element : specification) {
			TestDescriptorResolver testDescriptorResolver = testDescriptorResolverRegistry.forType(element.getClass());
			TestDescriptor descriptor = testDescriptorResolver.resolve(root, element);
			testDescriptors.add(descriptor);
			testDescriptors.addAll(testDescriptorResolver.resolveChildren(descriptor, element));
		}
		return new ArrayList<>(testDescriptors);
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		// TODO super class for Java test descriptors?
		return testDescriptor.getUniqueId().startsWith(getId());
	}

	@Override
	public void execute(EngineExecutionContext context) {

		// TODO Build a tree of TestDescriptors.
		//
		// Simply iterating over a collection is insufficient for our purposes. We need a
		// tree (or some form of hierarchical data structure) in order to be able to
		// execute each test within the correct scope.
		//
		// For example, we need to execute all test methods within a given test class as a
		// group in order to:
		//
		// 1) retain the instance across test method invocations (if desired).
		// 2) invoke class-level before & after methods _around_ the set of methods.

		Map<TestDescriptor, TestExecutionNode> nodes = new HashMap<>();
		for (TestDescriptor testDescriptor : context.getTestDescriptions()) {
			nodes.put(testDescriptor, TestExecutionNodeResolver.forDescriptor(testDescriptor));
		}

		List<TestExecutionNode> rootNodes = new LinkedList<>();
		for (TestExecutionNode node : nodes.values()) {
			TestDescriptor currentTestDescriptor = node.getTestDescriptor();
			if (currentTestDescriptor.getParent() == null) {
				rootNodes.add(node);
			}

			List<TestExecutionNode> childrenForCurrentNode = context.getTestDescriptions().stream().filter(
				testDescriptor -> currentTestDescriptor.equals(testDescriptor.getParent())).map(
					testDescriptor -> nodes.get(testDescriptor)).collect(toList());
			node.addChildren(childrenForCurrentNode);
		}

		for (TestExecutionNode rootNode : rootNodes) {
			rootNode.execute(context);
		}
	}
}
