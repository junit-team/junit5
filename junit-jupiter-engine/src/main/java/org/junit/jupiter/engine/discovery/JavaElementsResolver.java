/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;
import static org.junit.platform.commons.util.ReflectionUtils.findNestedClasses;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsInnerClass;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;

/**
 * @since 5.0
 */
class JavaElementsResolver {

	private static final Logger logger = LoggerFactory.getLogger(JavaElementsResolver.class);

	private static final IsInnerClass isInnerClass = new IsInnerClass();

	private final TestDescriptor engineDescriptor;
	private final Set<ElementResolver> resolvers;

	JavaElementsResolver(TestDescriptor engineDescriptor, Set<ElementResolver> resolvers) {
		this.engineDescriptor = engineDescriptor;
		this.resolvers = resolvers;
	}

	void resolveClass(Class<?> testClass) {
		Set<TestDescriptor> resolvedDescriptors = resolveContainerWithParents(testClass);
		resolvedDescriptors.forEach(this::resolveChildren);

		if (resolvedDescriptors.isEmpty()) {
			logger.warn(() -> format("Class '%s' could not be resolved.", testClass.getName()));
		}
	}

	void resolveMethod(Class<?> testClass, Method testMethod) {
		Set<TestDescriptor> potentialParents = resolveContainerWithParents(testClass);
		Set<TestDescriptor> resolvedDescriptors = resolveForAllParents(testMethod, potentialParents);

		if (resolvedDescriptors.isEmpty()) {
			logger.warn(() -> format("Method '%s' could not be resolved.", testMethod.toGenericString()));
		}

		logMultipleTestDescriptorsForSingleElement(testMethod, resolvedDescriptors);
	}

	private Set<TestDescriptor> resolveContainerWithParents(Class<?> testClass) {
		if (isInnerClass.test(testClass)) {
			Set<TestDescriptor> potentialParents = resolveContainerWithParents(testClass.getDeclaringClass());
			return resolveForAllParents(testClass, potentialParents);
		}
		else {
			return resolveForAllParents(testClass, Collections.singleton(engineDescriptor));
		}
	}

	void resolveUniqueId(UniqueId uniqueId) {
		uniqueId.getEngineId().ifPresent(engineId -> {

			// Ignore Unique IDs from other test engines.
			if (JupiterTestEngine.ENGINE_ID.equals(engineId)) {
				List<Segment> remainingSegments = new ArrayList<>(uniqueId.getSegments());

				// Ignore engine ID
				remainingSegments.remove(0);

				int numSegmentsToResolve = remainingSegments.size();
				int numSegmentsResolved = resolveUniqueId(this.engineDescriptor, remainingSegments);

				if (numSegmentsResolved == 0) {
					logger.warn(() -> format("Unique ID '%s' could not be resolved.", uniqueId));
				}
				else if (numSegmentsResolved != numSegmentsToResolve) {
					logger.warn(() -> {
						List<Segment> segments = uniqueId.getSegments();
						List<Segment> unresolved = segments.subList(1, segments.size()); // Remove engine ID
						unresolved = unresolved.subList(numSegmentsResolved, unresolved.size()); // Remove resolved segments
						return format("Unique ID '%s' could only be partially resolved. "
								+ "All resolved segments will be executed; however, the "
								+ "following segments could not be resolved: %s",
							uniqueId, unresolved);
					});
				}
			}
		});
	}

	/**
	 * Attempt to resolve all segments for the supplied unique ID.
	 *
	 * @return the number of segments resolved
	 */
	private int resolveUniqueId(TestDescriptor parent, List<Segment> remainingSegments) {
		if (remainingSegments.isEmpty()) {
			resolveChildren(parent);
			return 0;
		}

		Segment head = remainingSegments.remove(0);
		for (ElementResolver resolver : this.resolvers) {
			Optional<TestDescriptor> resolvedDescriptor = resolver.resolveUniqueId(head, parent);
			if (!resolvedDescriptor.isPresent()) {
				continue;
			}

			Optional<TestDescriptor> foundTestDescriptor = findTestDescriptorByUniqueId(
				resolvedDescriptor.get().getUniqueId());
			TestDescriptor descriptor = foundTestDescriptor.orElseGet(() -> {
				TestDescriptor newDescriptor = resolvedDescriptor.get();
				parent.addChild(newDescriptor);
				return newDescriptor;
			});
			return 1 + resolveUniqueId(descriptor, remainingSegments);
		}

		return 0;
	}

	private Set<TestDescriptor> resolveContainerWithChildren(Class<?> containerClass,
			Set<TestDescriptor> potentialParents) {

		Set<TestDescriptor> resolvedDescriptors = resolveForAllParents(containerClass, potentialParents);
		resolvedDescriptors.forEach(this::resolveChildren);
		return resolvedDescriptors;
	}

	private Set<TestDescriptor> resolveForAllParents(AnnotatedElement element, Set<TestDescriptor> potentialParents) {
		Set<TestDescriptor> resolvedDescriptors = new HashSet<>();
		potentialParents.forEach(parent -> resolvedDescriptors.addAll(resolve(element, parent)));
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
		List<Class<?>> nestedClassesCandidates = findNestedClasses(clazz, isInnerClass);
		nestedClassesCandidates.forEach(
			nestedClass -> resolveContainerWithChildren(nestedClass, Collections.singleton(containerDescriptor)));
	}

	private void resolveContainedMethods(TestDescriptor containerDescriptor, Class<?> testClass) {
		List<Method> testMethodCandidates = findMethods(testClass, method -> !ReflectionUtils.isPrivate(method),
			ReflectionUtils.HierarchyTraversalMode.TOP_DOWN);
		testMethodCandidates.forEach(method -> resolve(method, containerDescriptor));
	}

	private Set<TestDescriptor> resolve(AnnotatedElement element, TestDescriptor parent) {
		Set<TestDescriptor> descriptors = this.resolvers.stream() //
				.map(resolver -> tryToResolveWithResolver(element, parent, resolver)) //
				.filter(testDescriptors -> !testDescriptors.isEmpty()) //
				.flatMap(Collection::stream) //
				.collect(toSet());

		logMultipleTestDescriptorsForSingleElement(element, descriptors);

		return descriptors;
	}

	private Set<TestDescriptor> tryToResolveWithResolver(AnnotatedElement element, TestDescriptor parent,
			ElementResolver resolver) {

		Set<TestDescriptor> resolvedDescriptors = resolver.resolveElement(element, parent);
		Set<TestDescriptor> result = new LinkedHashSet<>();

		resolvedDescriptors.forEach(testDescriptor -> {
			Optional<TestDescriptor> existingTestDescriptor = findTestDescriptorByUniqueId(
				testDescriptor.getUniqueId());
			if (existingTestDescriptor.isPresent()) {
				result.add(existingTestDescriptor.get());
			}
			else {
				parent.addChild(testDescriptor);
				result.add(testDescriptor);
			}
		});

		return result;
	}

	@SuppressWarnings("unchecked")
	private Optional<TestDescriptor> findTestDescriptorByUniqueId(UniqueId uniqueId) {
		return (Optional<TestDescriptor>) this.engineDescriptor.findByUniqueId(uniqueId);
	}

	private void logMultipleTestDescriptorsForSingleElement(AnnotatedElement element, Set<TestDescriptor> descriptors) {
		if (descriptors.size() > 1 && element instanceof Method) {
			Method method = (Method) element;
			logger.warn(() -> String.format(
				"Possible configuration error: method [%s] resulted in multiple TestDescriptors %s. "
						+ "This is typically the result of annotating a method with multiple competing annotations "
						+ "such as @Test, @RepeatedTest, @ParameterizedTest, @TestFactory, etc.",
				method.toGenericString(), descriptors.stream().map(d -> d.getClass().getName()).collect(toList())));
		}
	}

}
