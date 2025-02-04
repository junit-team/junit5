/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toCollection;
import static org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor.getEnclosingTestClasses;
import static org.junit.jupiter.engine.discovery.predicates.IsTestClassWithTests.isTestOrTestFactoryOrTestTemplateMethod;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.support.ReflectionSupport.findMethods;
import static org.junit.platform.commons.support.ReflectionSupport.streamNestedClasses;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.engine.support.discovery.SelectorResolver.Resolution.unresolved;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.ContainerTemplate;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsNestedTestClass;
import org.junit.jupiter.engine.discovery.predicates.IsTestClassWithTests;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.NestedClassSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

/**
 * @since 5.5
 */
class ClassSelectorResolver implements SelectorResolver {

	private static final IsTestClassWithTests isTestClassWithTests = new IsTestClassWithTests();
	private static final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();

	private final Predicate<String> classNameFilter;
	private final JupiterConfiguration configuration;

	ClassSelectorResolver(Predicate<String> classNameFilter, JupiterConfiguration configuration) {
		this.classNameFilter = classNameFilter;
		this.configuration = configuration;
	}

	@Override
	public Resolution resolve(ClassSelector selector, Context context) {
		Class<?> testClass = selector.getJavaClass();
		if (isTestClassWithTests.test(testClass)) {
			// Nested tests are never filtered out
			if (classNameFilter.test(testClass.getName())) {
				return toResolution(
					context.addToParent(parent -> Optional.of(newStaticClassTestDescriptor(parent, testClass))));
			}
		}
		else if (isNestedTestClass.test(testClass)) {
			return toResolution(context.addToParent(() -> DiscoverySelectors.selectClass(testClass.getEnclosingClass()),
				parent -> Optional.of(newMemberClassTestDescriptor(parent, testClass))));
		}
		return unresolved();
	}

	@Override
	public Resolution resolve(NestedClassSelector selector, Context context) {
		if (isNestedTestClass.test(selector.getNestedClass())) {
			return toResolution(context.addToParent(() -> selectClass(selector.getEnclosingClasses()),
				parent -> Optional.of(newMemberClassTestDescriptor(parent, selector.getNestedClass()))));
		}
		return unresolved();
	}

	@Override
	public Resolution resolve(UniqueIdSelector selector, Context context) {
		UniqueId uniqueId = selector.getUniqueId();
		UniqueId.Segment lastSegment = uniqueId.getLastSegment();
		if (ClassTestDescriptor.SEGMENT_TYPE.equals(lastSegment.getType())) {
			String className = lastSegment.getValue();
			return ReflectionSupport.tryToLoadClass(className).toOptional() //
					.filter(isTestClassWithTests) //
					.map(testClass -> toResolution(
						context.addToParent(parent -> Optional.of(newClassTestDescriptor(parent, testClass))))).orElse(
							unresolved());
		}
		if (NestedClassTestDescriptor.SEGMENT_TYPE.equals(lastSegment.getType())) {
			String simpleClassName = lastSegment.getValue();
			return toResolution(context.addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
				if (parent instanceof ClassBasedTestDescriptor) {
					Class<?> parentTestClass = ((ClassBasedTestDescriptor) parent).getTestClass();
					return ReflectionSupport.findNestedClasses(parentTestClass, isNestedTestClass.and(
						where(Class::getSimpleName, isEqual(simpleClassName)))).stream().findFirst() //
							.flatMap(testClass -> Optional.of(newNestedClassTestDescriptor(parent, testClass)));
				}
				return Optional.empty();
			}));
		}
		if (ContainerTemplateTestDescriptor.SEGMENT_TYPE.equals(lastSegment.getType())) {
			String className = lastSegment.getValue();
			return ReflectionSupport.tryToLoadClass(className).toOptional() //
					.filter(isTestClassWithTests) //
					.filter(testClass -> isAnnotated(testClass, ContainerTemplate.class)) //
					.map(testClass -> toResolution(context.addToParent(
						parent -> Optional.of(newStaticClassTestDescriptor(parent, testClass))))).orElse(unresolved());
		}
		return unresolved();
	}

	private ClassBasedTestDescriptor newStaticClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		ClassTestDescriptor classTestDescriptor = newClassTestDescriptor(parent, testClass);
		return isAnnotated(testClass, ContainerTemplate.class) //
				? newContainerTemplateTestDescriptor(parent, classTestDescriptor) //
				: classTestDescriptor;
	}

	private ClassTestDescriptor newClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		return new ClassTestDescriptor(
			parent.getUniqueId().append(ClassTestDescriptor.SEGMENT_TYPE, testClass.getName()), testClass,
			configuration);
	}

	private ClassBasedTestDescriptor newMemberClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		NestedClassTestDescriptor classTestDescriptor = newNestedClassTestDescriptor(parent, testClass);
		return isAnnotated(testClass, ContainerTemplate.class) //
				? newContainerTemplateTestDescriptor(parent, classTestDescriptor) //
				: classTestDescriptor;
	}

	private NestedClassTestDescriptor newNestedClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		UniqueId uniqueId = parent.getUniqueId().append(NestedClassTestDescriptor.SEGMENT_TYPE,
			testClass.getSimpleName());
		return new NestedClassTestDescriptor(uniqueId, testClass, () -> getEnclosingTestClasses(parent), configuration);
	}

	private ContainerTemplateTestDescriptor newContainerTemplateTestDescriptor(TestDescriptor parent,
			ClassBasedTestDescriptor delegate) {
		String segmentValue = delegate.getUniqueId().getLastSegment().getValue();
		UniqueId uniqueId = parent.getUniqueId().append(ContainerTemplateTestDescriptor.SEGMENT_TYPE, segmentValue);
		return new ContainerTemplateTestDescriptor(uniqueId, delegate);
	}

	private Resolution toResolution(Optional<? extends ClassBasedTestDescriptor> testDescriptor) {
		return testDescriptor.map(it -> {
			Class<?> testClass = it.getTestClass();
			List<Class<?>> testClasses = new ArrayList<>(it.getEnclosingTestClasses());
			testClasses.add(testClass);
			// @formatter:off
			return Resolution.match(Match.exact(it, () -> {
				Stream<DiscoverySelector> methods = findMethods(testClass, isTestOrTestFactoryOrTestTemplateMethod, TOP_DOWN).stream()
						.map(method -> selectMethod(testClasses, method));
				Stream<NestedClassSelector> nestedClasses = streamNestedClasses(testClass, isNestedTestClass)
						.map(nestedClass -> DiscoverySelectors.selectNestedClass(testClasses, nestedClass));
				return Stream.concat(methods, nestedClasses).collect(toCollection((Supplier<Set<DiscoverySelector>>) LinkedHashSet::new));
			}));
			// @formatter:on
		}).orElse(unresolved());
	}

	private DiscoverySelector selectClass(List<Class<?>> classes) {
		if (classes.size() == 1) {
			return DiscoverySelectors.selectClass(classes.get(0));
		}
		int lastIndex = classes.size() - 1;
		return DiscoverySelectors.selectNestedClass(classes.subList(0, lastIndex), classes.get(lastIndex));
	}

	private DiscoverySelector selectMethod(List<Class<?>> classes, Method method) {
		if (classes.size() == 1) {
			return DiscoverySelectors.selectMethod(classes.get(0), method);
		}
		int lastIndex = classes.size() - 1;
		return DiscoverySelectors.selectNestedMethod(classes.subList(0, lastIndex), classes.get(lastIndex), method);
	}

}
