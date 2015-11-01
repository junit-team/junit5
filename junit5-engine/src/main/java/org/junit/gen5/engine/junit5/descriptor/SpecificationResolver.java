/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;

public class SpecificationResolver {

	private final Set testDescriptors;
	private final TestDescriptor root;
	private final TestDescriptorResolverRegistry resolverRegistry;

	public SpecificationResolver(Set testDescriptors, TestDescriptor root) {
		this.testDescriptors = testDescriptors;
		this.root = root;
		this.resolverRegistry = createResolverRegistry();
	}

	public void resolveElement(TestPlanSpecificationElement element) {
		testDescriptors.addAll(resolveElement(resolverRegistry, root, element));
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

}
