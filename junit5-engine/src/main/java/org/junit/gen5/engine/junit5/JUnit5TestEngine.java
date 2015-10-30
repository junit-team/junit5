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

import java.util.*;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.*;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class JUnit5TestEngine implements TestEngine {

	// TODO Consider using class names for engine IDs.
	private static final String ENGINE_ID = "junit5";


	@Override
	public String getId() {
		return ENGINE_ID;
	}

	@Override
	public List<TestDescriptor> discoverTests(TestPlanSpecification specification) {
		Set<TestDescriptor> testDescriptors = new LinkedHashSet<>();
		EngineTestGroup root = new EngineTestGroup(this);
		testDescriptors.add(root);

		for (TestPlanSpecificationElement element : specification) {
			SpecificationResolver specificationResolver = SpecificationResolverRegistry.forType(element.getClass());
			TestDescriptor descriptor = specificationResolver.resolve(root, element);
			testDescriptors.add(descriptor);
			testDescriptors.addAll(specificationResolver.resolveChildren(descriptor, element));
		}

		return new ArrayList<>(testDescriptors);
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		return testDescriptor.getUniqueId().startsWith(getId());
	}

	@Override
	public void execute(TestExecutionContext context) {
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

		context.getTestDescriptors().stream().peek(testDescriptor -> {
			Preconditions.condition(supports(testDescriptor),
				String.format("%s does not support test descriptors of type %s!", getId(),
					(testDescriptor != null ? testDescriptor.getClass().getName() : "null")));
		}).map(TestExecutionResolver::forDescriptor).filter(TestExecutor::isRoot).forEach(
			testExecutor -> testExecutor.execute(context));
	}
}
