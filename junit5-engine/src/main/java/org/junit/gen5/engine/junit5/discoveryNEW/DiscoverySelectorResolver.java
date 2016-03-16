/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discoveryNEW;

import static org.junit.gen5.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.junit.gen5.engine.junit5.discovery.IsPotentialTestContainer;
import org.junit.gen5.engine.junit5.discovery.IsTestMethod;
import org.junit.gen5.engine.junit5.discovery.JUnit5EngineDescriptor;

public class DiscoverySelectorResolver {
	private final JUnit5EngineDescriptor engineDescriptor;

	public DiscoverySelectorResolver(JUnit5EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolveSelectors(EngineDiscoveryRequest request) {
		request.getSelectorsByType(ClassSelector.class).forEach(selector -> {
			resolveClass(selector.getTestClass());
		});
		request.getSelectorsByType(MethodSelector.class).forEach(selector -> {
			resolveMethod(selector.getTestClass(), selector.getTestMethod());
		});
		pruneTree();
	}

	private void pruneTree() {
		TestDescriptor.Visitor removeDescriptorsWithoutTests = (descriptor, remove) -> {
			if (!descriptor.isRoot() && !descriptor.hasTests())
				remove.run();
		};
		engineDescriptor.accept(removeDescriptorsWithoutTests);
	}

	private void resolveMethod(Class<?> testClass, Method testMethod) {
		Optional<TestDescriptor> optionalParentDescriptor = resolve(testClass, engineDescriptor);
		optionalParentDescriptor.ifPresent(parent -> {
			Optional<TestDescriptor> optionalMethodDescriptor = resolve(testMethod, parent);
		});
	}

	private void resolveClass(Class<?> testClass) {
		TestDescriptor parent = engineDescriptor;
		Optional<TestDescriptor> optionalClassDescriptor = resolve(testClass, parent);
		optionalClassDescriptor.ifPresent(classDescriptor -> {
			List<Method> testMethodCandidates = findMethods(testClass, new IsTestMethod(),
				ReflectionUtils.MethodSortOrder.HierarchyDown);
			testMethodCandidates.forEach(method -> resolve(method, classDescriptor));
		});
	}

	private Optional<TestDescriptor> resolve(Class<?> testClass, TestDescriptor parent) {
		UniqueId uniqueId = parent.getUniqueId().append("class", testClass.getName());
		Optional<TestDescriptor> optionalMethodTestDescriptor = (Optional<TestDescriptor>) parent.findByUniqueId(
			uniqueId);
		if (optionalMethodTestDescriptor.isPresent())
			return optionalMethodTestDescriptor;

		if (!new IsPotentialTestContainer().test(testClass)) {
			return Optional.empty();
		}
		ClassTestDescriptor classTestDescriptor = new ClassTestDescriptor(uniqueId, testClass);
		parent.addChild(classTestDescriptor);

		return Optional.of(classTestDescriptor);
	}

	private Optional<TestDescriptor> resolve(Method testMethod, TestDescriptor parent) {
		ClassTestDescriptor parentClassDescriptor = (ClassTestDescriptor) parent;
		UniqueId uniqueId = parentClassDescriptor.getUniqueId().append("method", testMethod.getName() + "()");

		Optional<TestDescriptor> optionalMethodTestDescriptor = (Optional<TestDescriptor>) parent.findByUniqueId(
			uniqueId);
		if (optionalMethodTestDescriptor.isPresent())
			return optionalMethodTestDescriptor;

		if (!new IsTestMethod().test(testMethod))
			return Optional.empty();
		MethodTestDescriptor methodTestDescriptor = new MethodTestDescriptor(uniqueId,
			parentClassDescriptor.getTestClass(), testMethod);
		parentClassDescriptor.addChild(methodTestDescriptor);
		return Optional.of(methodTestDescriptor);
	}
}
