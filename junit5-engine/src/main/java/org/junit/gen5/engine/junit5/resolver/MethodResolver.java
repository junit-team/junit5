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

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.gen5.commons.util.ReflectionUtils.*;
import static org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

/**
 * @since 5.0
 */
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
	public void bindTestResolveryRegistry(TestResolverRegistry testResolverRegistry) {
		super.bindTestResolveryRegistry(testResolverRegistry);
	}

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		if (parent.isRoot()) {
			resolveMethodsFromSelectors(parent, discoveryRequest);
		}
		else if (parent instanceof ClassTestDescriptor) {
			Class<?> testClass = ((ClassTestDescriptor) parent).getTestClass();
			resolveTestMethodsInClasses(testClass, parent, discoveryRequest);
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

	private void resolveTestMethodsInClasses(Class<?> testClass, TestDescriptor parent,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        List<MethodTestDescriptor> testMethods = findAllMethodsInHierarchy(testClass, HierarchyDown).stream()
                .filter(this::isTestMethod)
                .map(testMethod -> descriptorForParentAndMethod(parent, testClass, testMethod))
                .collect(toList());
        // @formatter:on

		for (MethodTestDescriptor child : testMethods) {
			parent.addChild(child);
			getTestResolverRegistry().notifyResolvers(child, discoveryRequest);
		}
	}

	@Override
	public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
		return Optional.empty();
	}

	private boolean isTestMethod(Method candidate) {
		//please do not collapse into single return
		if (isStatic(candidate))
			return false;
		if (isPrivate(candidate))
			return false;
		if (isAbstract(candidate))
			return false;
		return isAnnotated(candidate, Test.class);
	}
}
