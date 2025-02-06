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

import static java.util.Collections.emptyList;
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
import org.junit.jupiter.api.extension.ContainerTemplateInvocationContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.descriptor.ClassBasedTestDescriptor;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.ContainerTemplateTestDescriptor;
import org.junit.jupiter.engine.descriptor.Filterable;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestClassAware;
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
		if (ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE.equals(lastSegment.getType())) {
			String className = lastSegment.getValue();
			return ReflectionSupport.tryToLoadClass(className).toOptional() //
					.filter(isTestClassWithTests) //
					.filter(testClass -> isAnnotated(testClass, ContainerTemplate.class)) //
					.map(testClass -> toResolution(context.addToParent(
						parent -> Optional.of(newStaticContainerTemplateTestDescriptor(parent, testClass))))) //
					.orElse(unresolved());
		}
		if (NestedClassTestDescriptor.SEGMENT_TYPE.equals(lastSegment.getType())) {
			String simpleClassName = lastSegment.getValue();
			return toResolution(context.addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
				if (parent instanceof TestClassAware) {
					Class<?> parentTestClass = ((TestClassAware) parent).getTestClass();
					return ReflectionSupport.findNestedClasses(parentTestClass, isNestedTestClass.and(
						where(Class::getSimpleName, isEqual(simpleClassName)))).stream().findFirst() //
							.flatMap(testClass -> Optional.of(newNestedClassTestDescriptor(parent, testClass)));
				}
				return Optional.empty();
			}));
		}
		if (ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE.equals(lastSegment.getType())) {
			String simpleClassName = lastSegment.getValue();
			return toResolution(context.addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
				if (parent instanceof TestClassAware) {
					Class<?> parentTestClass = ((TestClassAware) parent).getTestClass();
					return ReflectionSupport.findNestedClasses(parentTestClass, isNestedTestClass.and(
						where(Class::getSimpleName, isEqual(simpleClassName)))).stream().findFirst() //
							.filter(testClass -> isAnnotated(testClass, ContainerTemplate.class)) //
							.flatMap(
								testClass -> Optional.of(newNestedContainerTemplateTestDescriptor(parent, testClass)));
				}
				return Optional.empty();
			}));
		}
		if (ContainerTemplateInvocationTestDescriptor.SEGMENT_TYPE.equals(lastSegment.getType())) {
			return toInvocationResolution(
				context.addToParent(() -> selectUniqueId(uniqueId.removeLastSegment()), parent -> {
					int index = Integer.parseInt(lastSegment.getValue().substring(1));
					return Optional.of(newDummyContainerTemplateInvocationTestDescriptor(parent, lastSegment, index));
				}));
		}
		return unresolved();
	}

	private ContainerTemplateInvocationTestDescriptor newDummyContainerTemplateInvocationTestDescriptor(
			TestDescriptor parent, UniqueId.Segment lastSegment, int index) {
		return new ContainerTemplateInvocationTestDescriptor(parent.getUniqueId().append(lastSegment),
			(TestClassAware) parent, DummyContainerTemplateInvocationContext.INSTANCE, index,
			parent.getSource().orElse(null), configuration);
	}

	private ClassBasedTestDescriptor newStaticClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		return isAnnotated(testClass, ContainerTemplate.class) //
				? newStaticContainerTemplateTestDescriptor(parent, testClass) //
				: newClassTestDescriptor(parent, testClass);
	}

	private ContainerTemplateTestDescriptor newStaticContainerTemplateTestDescriptor(TestDescriptor parent,
			Class<?> testClass) {
		return newContainerTemplateTestDescriptor(parent, ContainerTemplateTestDescriptor.STATIC_CLASS_SEGMENT_TYPE,
			newClassTestDescriptor(parent, testClass));
	}

	private ClassTestDescriptor newClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		return new ClassTestDescriptor(
			parent.getUniqueId().append(ClassTestDescriptor.SEGMENT_TYPE, testClass.getName()), testClass,
			configuration);
	}

	private ClassBasedTestDescriptor newMemberClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		return isAnnotated(testClass, ContainerTemplate.class) //
				? newNestedContainerTemplateTestDescriptor(parent, testClass) //
				: newNestedClassTestDescriptor(parent, testClass);
	}

	private ContainerTemplateTestDescriptor newNestedContainerTemplateTestDescriptor(TestDescriptor parent,
			Class<?> testClass) {
		return newContainerTemplateTestDescriptor(parent, ContainerTemplateTestDescriptor.NESTED_CLASS_SEGMENT_TYPE,
			newNestedClassTestDescriptor(parent, testClass));
	}

	private NestedClassTestDescriptor newNestedClassTestDescriptor(TestDescriptor parent, Class<?> testClass) {
		UniqueId uniqueId = parent.getUniqueId().append(NestedClassTestDescriptor.SEGMENT_TYPE,
			testClass.getSimpleName());
		return new NestedClassTestDescriptor(uniqueId, testClass, () -> getEnclosingTestClasses(parent), configuration);
	}

	private ContainerTemplateTestDescriptor newContainerTemplateTestDescriptor(TestDescriptor parent,
			String segmentType, ClassBasedTestDescriptor delegate) {
		String segmentValue = delegate.getUniqueId().getLastSegment().getValue();
		UniqueId uniqueId = parent.getUniqueId().append(segmentType, segmentValue);
		return new ContainerTemplateTestDescriptor(uniqueId, delegate);
	}

	private Resolution toInvocationResolution(Optional<ContainerTemplateInvocationTestDescriptor> testDescriptor) {
		return testDescriptor //
				.map(it -> Resolution.match(Match.exact(it,
					expansionCallback(it,
						() -> it.getParent().map(parent -> getTestClasses((ClassBasedTestDescriptor) parent)).orElse(
							emptyList()))))) //
				.orElse(unresolved());
	}

	private Resolution toResolution(Optional<? extends ClassBasedTestDescriptor> testDescriptor) {
		return testDescriptor //
				.map(it -> Resolution.match(Match.exact(it, expansionCallback(it)))) //
				.orElse(unresolved());
	}

	private Supplier<Set<? extends DiscoverySelector>> expansionCallback(ClassBasedTestDescriptor testDescriptor) {
		return expansionCallback(testDescriptor, () -> getTestClasses(testDescriptor));
	}

	private static List<Class<?>> getTestClasses(ClassBasedTestDescriptor testDescriptor) {
		List<Class<?>> testClasses = new ArrayList<>(testDescriptor.getEnclosingTestClasses());
		testClasses.add(testDescriptor.getTestClass());
		return testClasses;
	}

	private Supplier<Set<? extends DiscoverySelector>> expansionCallback(TestDescriptor testDescriptor,
			Supplier<List<Class<?>>> testClassesSupplier) {
		return () -> {
			if (testDescriptor instanceof Filterable) {
				Filterable filterable = (Filterable) testDescriptor;
				filterable.getDynamicDescendantFilter().allowAll();
			}
			List<Class<?>> testClasses = testClassesSupplier.get();
			Class<?> testClass = testClasses.get(testClasses.size() - 1);
			Stream<DiscoverySelector> methods = findMethods(testClass, isTestOrTestFactoryOrTestTemplateMethod,
				TOP_DOWN).stream().map(method -> selectMethod(testClasses, method));
			Stream<NestedClassSelector> nestedClasses = streamNestedClasses(testClass, isNestedTestClass).map(
				nestedClass -> DiscoverySelectors.selectNestedClass(testClasses, nestedClass));
			return Stream.concat(methods, nestedClasses).collect(
				toCollection((Supplier<Set<DiscoverySelector>>) LinkedHashSet::new));
		};
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

	static class DummyContainerTemplateInvocationContext implements ContainerTemplateInvocationContext {
		private static final DummyContainerTemplateInvocationContext INSTANCE = new DummyContainerTemplateInvocationContext();
	}
}
