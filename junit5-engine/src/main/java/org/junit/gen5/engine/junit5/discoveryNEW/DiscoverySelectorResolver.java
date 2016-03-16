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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.junit5.discovery.JUnit5EngineDescriptor;

public class DiscoverySelectorResolver {
	private final JUnit5EngineDescriptor engineDescriptor;
	private final Set<ElementResolver> resolvers = new HashSet<>();

	public DiscoverySelectorResolver(JUnit5EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
		resolvers.add(new TestContainerResolver());
		resolvers.add(new TestMethodResolver());
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
		Set<TestDescriptor> resolvedParentDescriptors = resolve(testClass, engineDescriptor);
		resolvedParentDescriptors.forEach(parent -> {
			resolve(testMethod, parent);
		});
	}

	private void resolveClass(Class<?> testClass) {
		TestDescriptor parent = engineDescriptor;
		Set<TestDescriptor> resolvedClassDescriptors = resolve(testClass, parent);
		if (resolvedClassDescriptors.isEmpty())
			return;
		resolvedClassDescriptors.forEach(classDescriptor -> {
			List<Method> testMethodCandidates = findMethods(testClass, method -> !ReflectionUtils.isPrivate(method),
				ReflectionUtils.MethodSortOrder.HierarchyDown);
			testMethodCandidates.forEach(method -> resolve(method, classDescriptor));
		});
	}

	private Set<TestDescriptor> resolve(AnnotatedElement element, TestDescriptor parent) {
		return resolvers.stream() //
				.map(resolver -> tryToResolveWithResolver(element, parent, resolver)) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.collect(Collectors.toSet());
	}

	private Optional<TestDescriptor> tryToResolveWithResolver(AnnotatedElement element, TestDescriptor parent,
			ElementResolver resolver) {
		if (!resolver.willResolve(element, parent))
			return Optional.empty();

		UniqueId uniqueId = resolver.createUniqueId(element, parent);

		Optional<TestDescriptor> optionalMethodTestDescriptor = findTestDescriptorByUniqueId(uniqueId);
		if (optionalMethodTestDescriptor.isPresent())
			return optionalMethodTestDescriptor;

		TestDescriptor newDescriptor = resolver.resolve(element, parent, uniqueId);
		parent.addChild(newDescriptor);
		return Optional.of(newDescriptor);
	}

	@SuppressWarnings("unchecked")
	private Optional<TestDescriptor> findTestDescriptorByUniqueId(UniqueId uniqueId) {
		return (Optional<TestDescriptor>) engineDescriptor.findByUniqueId(uniqueId);
	}

}
