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

import static java.lang.String.format;
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.discovery.IsNonStaticInnerClass;

public class JavaElementsResolver {

	private static final Logger LOG = Logger.getLogger(JavaElementsResolver.class.getName());

	private final TestDescriptor engineDescriptor;
	private final Set<ElementResolver> resolvers;

	public JavaElementsResolver(TestDescriptor engineDescriptor, Set<ElementResolver> resolvers) {
		this.engineDescriptor = engineDescriptor;
		this.resolvers = resolvers;
	}

	public void resolveClass(Class<?> testClass) {

		Set<TestDescriptor> resolvedDescriptors = resolveContainerWithParents(testClass);
		resolvedDescriptors.forEach(this::resolveChildren);

		if (resolvedDescriptors.isEmpty()) {
			LOG.warning(() -> {
				String classDescription = testClass.getName();
				return format("Class '%s' could not be resolved", classDescription);
			});
		}
	}

	public void resolveMethod(Class<?> testClass, Method testMethod) {
		Set<TestDescriptor> potentialParents = resolveContainerWithParents(testClass);
		Set<TestDescriptor> resolvedDescriptors = resolveForAllParents(testMethod, potentialParents);

		if (resolvedDescriptors.isEmpty()) {
			LOG.warning(() -> {
				String methodId = String.format("%s(%s)", testMethod.getName(),
					StringUtils.nullSafeToString(testMethod.getParameterTypes()));
				String methodDescription = testMethod.getDeclaringClass().getName() + "#" + methodId;
				return format("Method '%s' could not be resolved", methodDescription);
			});
		}
	}

	private Set<TestDescriptor> resolveContainerWithParents(Class<?> testClass) {
		if (new IsNonStaticInnerClass().test(testClass)) {
			Set<TestDescriptor> potentialParents = resolveContainerWithParents(testClass.getDeclaringClass());
			return resolveForAllParents(testClass, potentialParents);
		}
		else {
			return resolveForAllParents(testClass, Collections.singleton(engineDescriptor));
		}
	}

	public void resolveUniqueId(UniqueId uniqueId) {
		List<UniqueId.Segment> segments = uniqueId.getSegments();
		segments.remove(0); // Ignore engine unique ID
		if (!resolveUniqueId(engineDescriptor, segments))
			LOG.warning(() -> {
				return format("Unique ID '%s' could not be resolved", uniqueId.getUniqueString());
			});
	}

	/**
	 * Return true if all segments of unique ID could be resolved
	 */
	private boolean resolveUniqueId(TestDescriptor parent, List<UniqueId.Segment> remainingSegments) {
		if (remainingSegments.isEmpty()) {
			resolveChildren(parent);
			return true;
		}

		UniqueId.Segment head = remainingSegments.remove(0);
		for (ElementResolver resolver : resolvers) {
			Optional<TestDescriptor> resolvedDescriptor = resolver.resolveUniqueId(head, parent);
			if (!resolvedDescriptor.isPresent())
				continue;

			Optional<TestDescriptor> foundTestDescriptor = findTestDescriptorByUniqueId(
				resolvedDescriptor.get().getUniqueId());
			TestDescriptor descriptor = foundTestDescriptor.orElseGet(() -> {
				TestDescriptor newDescriptor = resolvedDescriptor.get();
				parent.addChild(newDescriptor);
				return newDescriptor;
			});
			return resolveUniqueId(descriptor, new ArrayList<>(remainingSegments));
		}
		return false;
	}

	private Set<TestDescriptor> resolveContainerWithChildren(Class<?> containerClass,
			Set<TestDescriptor> potentialParents) {
		Set<TestDescriptor> resolvedDescriptors = resolveForAllParents(containerClass, potentialParents);
		resolvedDescriptors.forEach(this::resolveChildren);
		return resolvedDescriptors;
	}

	private Set<TestDescriptor> resolveForAllParents(AnnotatedElement element, Set<TestDescriptor> potentialParents) {
		Set<TestDescriptor> resolvedDescriptors = new HashSet<>();
		potentialParents.forEach(parent -> {
			resolvedDescriptors.addAll(resolve(element, parent));
		});
		return resolvedDescriptors;
	}

	private void resolveChildren(TestDescriptor descriptor) {
		if (descriptor instanceof ClassTestDescriptor) {
			Class<?> testClass = ((ClassTestDescriptor) descriptor).getTestClass();
			resolveContainedMethods(descriptor, testClass);
			resolveContainedNestedClasses(descriptor, testClass);
		}
	}

	private void resolveContainedNestedClasses(TestDescriptor containerDescriptor, Class<?> clazz) {
		List<Class<?>> nestedClassesCandidates = findNestedClasses(clazz, new IsNonStaticInnerClass());
		nestedClassesCandidates.forEach(
			nestedClass -> resolveContainerWithChildren(nestedClass, Collections.singleton(containerDescriptor)));
	}

	private void resolveContainedMethods(TestDescriptor containerDescriptor, Class<?> testClass) {
		List<Method> testMethodCandidates = findMethods(testClass, method -> !ReflectionUtils.isPrivate(method),
			ReflectionUtils.MethodSortOrder.HierarchyDown);
		testMethodCandidates.forEach(method -> resolve(method, containerDescriptor));
	}

	private Set<TestDescriptor> resolve(AnnotatedElement element, TestDescriptor parent) {
		return resolvers.stream() //
				.map(resolver -> tryToResolveWithResolver(element, parent, resolver)) //
				.filter(testDescriptors -> !testDescriptors.isEmpty()) //
				.flatMap(Collection::stream) //
				.collect(Collectors.toSet());
	}

	private Set<TestDescriptor> tryToResolveWithResolver(AnnotatedElement element, TestDescriptor parent,
			ElementResolver resolver) {

		Set<TestDescriptor> resolvedDescriptors = resolver.resolveElement(element, parent);

		resolvedDescriptors.forEach(testDescriptor -> {
			Optional<TestDescriptor> existingTestDescriptor = findTestDescriptorByUniqueId(
				testDescriptor.getUniqueId());
			if (!existingTestDescriptor.isPresent()) {
				parent.addChild(testDescriptor);
			}
		});

		return resolvedDescriptors;
	}

	@SuppressWarnings("unchecked")
	private Optional<TestDescriptor> findTestDescriptorByUniqueId(UniqueId uniqueId) {
		return (Optional<TestDescriptor>) engineDescriptor.findByUniqueId(uniqueId);
	}

}
