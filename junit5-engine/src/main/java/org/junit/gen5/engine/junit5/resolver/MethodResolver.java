/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

public class MethodResolver extends JUnit5TestResolver {
	public static MethodTestDescriptor descriptorForParentAndMethod(TestDescriptor parent, Class<?> testClass,
			Method testMethod) {
		String uniqueId = parent.getUniqueId() + "/[method:" + testMethod.getName() + "]";

		if (parent.findByUniqueId(uniqueId).isPresent()) {
			return (MethodTestDescriptor) parent.findByUniqueId(uniqueId).get();
		}
		else {
			return new MethodTestDescriptor(uniqueId, testClass, testMethod);
		}
	}

	@Override
	public void initialize(TestEngine testEngine, TestResolverRegistry testResolverRegistry) {
		super.initialize(testEngine, testResolverRegistry);
	}

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		if (parent.isRoot()) {
			resolveMethodsFromSelectors(parent, discoveryRequest);
		}
	}

	private void resolveMethodsFromSelectors(TestDescriptor root, EngineDiscoveryRequest discoveryRequest) {
		List<MethodSelector> methodSelectors = discoveryRequest.getSelectorsByType(MethodSelector.class);

		for (MethodSelector methodSelector : methodSelectors) {
			DiscoverySelector selector = forClass(methodSelector.getTestClass());
			TestDescriptor parent = getTestResolverRegistry().fetchParent(selector, root);
			MethodTestDescriptor child = descriptorForParentAndMethod(parent, methodSelector.getTestClass(),
				methodSelector.getTestMethod());
			parent.addChild(child);
			getTestResolverRegistry().notifyResolvers(child, discoveryRequest);
		}
	}

	@Override
	public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
		return Optional.empty();
	}
}
