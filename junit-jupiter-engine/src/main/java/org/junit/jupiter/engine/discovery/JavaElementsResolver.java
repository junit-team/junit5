/*
 * Copyright 2015-2018 the original author or authors.
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
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;
import static org.junit.platform.commons.util.ClassUtils.nullSafeToString;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInModule;
import static org.junit.platform.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;
import static org.junit.platform.commons.util.ReflectionUtils.findNestedClasses;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.Filterable;
import org.junit.jupiter.engine.discovery.predicates.IsInnerClass;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * <h3>NOTES TO DEVELOPERS</h3>
 *
 * <p>Any non-private method in this class is forbidden to throw any type of
 * exception other than a "blacklisted exception". All other exceptions must
 * be caught, logged, and swallowed in order to ensure the robustness of our
 * discovery mechanism.
 *
 * <p>Discovery errors related to Unique IDs that are particular to the Jupiter
 * test engine should be logged at WARNING level; whereas, all other discovery
 * errors should be logged at DEBUG level, since the Jupiter engine has no way
 * of knowing that it is supposed to actually handle those things (e.g.,
 * classpath roots, packages, etc. selected by a registered discovery selector).
 *
 * @since 5.0
 */
class JavaElementsResolver {

	private static final Logger logger = LoggerFactory.getLogger(JavaElementsResolver.class);

	private static final IsInnerClass isInnerClass = new IsInnerClass();

	private final TestDescriptor engineDescriptor;
	private final ClassFilter classFilter;
	private final Set<ElementResolver> resolvers;

	JavaElementsResolver(TestDescriptor engineDescriptor, ClassFilter classFilter, Set<ElementResolver> resolvers) {
		this.engineDescriptor = engineDescriptor;
		this.classFilter = classFilter;
		this.resolvers = resolvers;
	}

	void resolveClasspathRoot(ClasspathRootSelector selector) {
		try {
			findAllClassesInClasspathRoot(selector.getClasspathRoot(), this.classFilter).forEach(this::resolveClass);
		}
		catch (Throwable t) {
			rethrowIfBlacklisted(t);
			logger.debug(t,
				() -> format("Failed to resolve classes in classpath root '%s'.", selector.getClasspathRoot()));
		}
	}

	void resolveModule(ModuleSelector selector) {
		try {
			findAllClassesInModule(selector.getModuleName(), this.classFilter).forEach(this::resolveClass);
		}
		catch (Throwable t) {
			rethrowIfBlacklisted(t);
			logger.debug(t, () -> format("Failed to resolve classes in module '%s'.", selector.getModuleName()));
		}
	}

	void resolvePackage(PackageSelector selector) {
		try {
			findAllClassesInPackage(selector.getPackageName(), this.classFilter).forEach(this::resolveClass);
		}
		catch (Throwable t) {
			rethrowIfBlacklisted(t);
			logger.debug(t, () -> format("Failed to resolve classes in package '%s'.", selector.getPackageName()));
		}
	}

	void resolveClass(ClassSelector selector) {
		// Even though resolveClass(Class<?>) has its own similar try-catch block, the
		// try-catch block is necessary here as well since ClassSelector#getJavaClass()
		// may throw an exception.
		try {
			resolveClass(selector.getJavaClass());
		}
		catch (Throwable t) {
			rethrowIfBlacklisted(t);
			logger.debug(t, () -> format("Class '%s' could not be resolved.", selector.getClassName()));
		}
	}

	private void resolveClass(Class<?> testClass) {
		try {
			Set<TestDescriptor> resolvedDescriptors = resolveContainerWithParents(testClass);
			resolvedDescriptors.forEach(this::resolveChildren);

			if (resolvedDescriptors.isEmpty()) {
				logger.debug(() -> format("Class '%s' could not be resolved.", nullSafeToString(testClass)));
			}
		}
		catch (Throwable t) {
			rethrowIfBlacklisted(t);
			logger.debug(t, () -> format("Class '%s' could not be resolved.", nullSafeToString(testClass)));
		}
	}

	void resolveMethod(MethodSelector selector) {
		try {
			Class<?> testClass = selector.getJavaClass();
			Method testMethod = selector.getJavaMethod();

			Set<TestDescriptor> potentialParents = resolveContainerWithParents(testClass);
			Set<TestDescriptor> resolvedDescriptors = resolveForAllParents(testMethod, potentialParents);

			if (resolvedDescriptors.isEmpty()) {
				logger.debug(() -> format("Method '%s' could not be resolved.", testMethod.toGenericString()));
			}

			logMultipleTestDescriptorsForSingleElement(testMethod, resolvedDescriptors);
		}
		catch (Throwable t) {
			rethrowIfBlacklisted(t);
			logger.debug(t, () -> format("Method '%s' in class '%s' could not be resolved.", selector.getMethodName(),
				selector.getClassName()));
		}
	}

	void resolveUniqueId(UniqueIdSelector selector) {
		UniqueId uniqueId = selector.getUniqueId();

		// Ignore Unique IDs from other test engines.
		if (JupiterTestEngine.ENGINE_ID.equals(uniqueId.getEngineId().orElse(null))) {
			try {
				Deque<TestDescriptor> resolvedDescriptors = resolveAllSegments(uniqueId);
				handleResolvedDescriptorsForUniqueId(uniqueId, resolvedDescriptors);
			}
			catch (Throwable t) {
				rethrowIfBlacklisted(t);
				logger.warn(t, () -> format("Unique ID '%s' could not be resolved.", selector.getUniqueId()));
			}
		}
	}

	private Set<TestDescriptor> resolveContainerWithParents(Class<?> testClass) {
		if (isInnerClass.test(testClass)) {
			Set<TestDescriptor> potentialParents = resolveContainerWithParents(testClass.getDeclaringClass());
			return resolveForAllParents(testClass, potentialParents);
		}
		else {
			return resolveForAllParents(testClass, Collections.singleton(this.engineDescriptor));
		}
	}

	/**
	 * Attempt to resolve all segments for the supplied unique ID.
	 */
	private Deque<TestDescriptor> resolveAllSegments(UniqueId uniqueId) {
		List<Segment> segments = uniqueId.getSegments();
		Deque<TestDescriptor> resolvedDescriptors = new LinkedList<>();
		resolvedDescriptors.addFirst(this.engineDescriptor);

		for (int index = 1; index < segments.size() && resolvedDescriptors.size() == index; index++) {
			Segment segment = segments.get(index);
			TestDescriptor parent = resolvedDescriptors.getLast();
			UniqueId partialUniqueId = parent.getUniqueId().append(segment);

			Optional<TestDescriptor> resolvedDescriptor = findTestDescriptorByUniqueId(partialUniqueId);
			if (!resolvedDescriptor.isPresent()) {
				// @formatter:off
				resolvedDescriptor = this.resolvers.stream()
						.map(resolver -> resolver.resolveUniqueId(segment, parent))
						.filter(Optional::isPresent)
						.map(Optional::get)
						.findFirst();
				// @formatter:on
				resolvedDescriptor.ifPresent(parent::addChild);
			}
			resolvedDescriptor.ifPresent(resolvedDescriptors::addLast);
		}
		return resolvedDescriptors;
	}

	private void handleResolvedDescriptorsForUniqueId(UniqueId uniqueId, Deque<TestDescriptor> resolvedDescriptors) {
		List<Segment> segments = uniqueId.getSegments();
		int numSegmentsToResolve = segments.size() - 1;
		int numSegmentsResolved = resolvedDescriptors.size() - 1;

		if (numSegmentsResolved == 0) {
			logger.warn(() -> format("Unique ID '%s' could not be resolved.", uniqueId));
		}
		else if (numSegmentsResolved != numSegmentsToResolve) {
			if (resolvedDescriptors.getLast() instanceof Filterable) {
				((Filterable) resolvedDescriptors.getLast()).getDynamicDescendantFilter().allow(uniqueId);
			}
			else {
				logger.warn(() -> {
					List<Segment> unresolved = segments.subList(1, segments.size()); // Remove engine ID
					unresolved = unresolved.subList(numSegmentsResolved, unresolved.size()); // Remove resolved segments
					return format("Unique ID '%s' could only be partially resolved. "
							+ "All resolved segments will be executed; however, the "
							+ "following segments could not be resolved: %s",
						uniqueId, unresolved);
				});
			}
		}
		else {
			resolveChildren(resolvedDescriptors.getLast());
		}
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
		// @formatter:off
		resolvedDescriptors.stream()
				.filter(Filterable.class::isInstance)
				.map(Filterable.class::cast)
				.forEach(testDescriptor -> testDescriptor.getDynamicDescendantFilter().allowAll());
		// @formatter:on
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
		List<Method> testMethodCandidates = findMethods(testClass, ReflectionUtils::isNotPrivate);
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
