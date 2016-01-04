/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
public final class TestPlanSpecification implements Iterable<TestPlanSpecificationElement> {

	public static TestPlanSpecificationElement forPackage(String packageName) {
		return new PackageSpecification(packageName);
	}

	public static List<TestPlanSpecificationElement> forPackages(Collection<String> packageNames) {
		return packageNames.stream().map(TestPlanSpecification::forPackage).collect(toList());
	}

	public static TestPlanSpecificationElement forMethod(Class<?> testClass, Method testMethod) {
		return new MethodSpecification(testClass, testMethod);
	}

	public static TestPlanSpecificationElement forName(String anyName) {

		Optional<Class<?>> testClassOptional = ReflectionUtils.loadClass(anyName);
		if (testClassOptional.isPresent()) {
			return forClass(testClassOptional.get());
		}

		Optional<Method> testMethodOptional = ReflectionUtils.loadMethod(anyName);
		if (testMethodOptional.isPresent()) {
			Method testMethod = testMethodOptional.get();
			return forMethod(testMethod.getDeclaringClass(), testMethod);
		}

		if (ReflectionUtils.isPackage(anyName)) {
			return forPackage(anyName);
		}

		throw new IllegalArgumentException(
			String.format("'%s' specifies neither a class, method, nor package.", anyName));
	}

	public static List<TestPlanSpecificationElement> allTests(Set<File> rootDirectories) {
		// @formatter:off
		return rootDirectories.stream()
				.filter(File::exists)
				.map(AllTestsSpecification::new)
				.collect(Collectors.toList());
		// @formatter:on
	}

	public static List<TestPlanSpecificationElement> forNames(Collection<String> classNames) {
		return forNames(classNames.stream());
	}

	private static List<TestPlanSpecificationElement> forNames(Stream<String> classNames) {
		return classNames.map(TestPlanSpecification::forName).collect(toList());
	}

	public static TestPlanSpecificationElement forClass(Class<?> testClass) {
		return new ClassSpecification(testClass);
	}

	public static TestPlanSpecificationElement forUniqueId(String uniqueId) {
		return new UniqueIdSpecification(uniqueId);
	}

	public static Predicate<TestDescriptor> byTags(String... tagNames) {
		return byTags(Arrays.asList(tagNames));
	}

	public static Predicate<TestDescriptor> byTags(List<String> includeTags) {
		// @formatter:off
		return descriptor -> descriptor.getTags().stream()
				.map(TestTag::getName)
				.anyMatch(includeTags::contains);
		// @formatter:on
	}

	public static Predicate<TestDescriptor> excludeTags(String... tagNames) {
		return excludeTags(Arrays.asList(tagNames));
	}

	public static Predicate<TestDescriptor> excludeTags(List<String> includeTags) {
		// @formatter:off
		return descriptor -> descriptor.getTags().stream()
				.map(TestTag::getName)
				.allMatch(tagName -> !includeTags.contains(tagName));
		// @formatter:on
	}

	public static Predicate<TestDescriptor> byEngine(String engineId) {
		return descriptor -> descriptor.getUniqueId().startsWith(engineId);
	}

	public static EngineFilter classNameMatches(String regex) {
		return new ClassNameFilter(regex);
	}

	public static TestPlanSpecification build(TestPlanSpecificationElement... elements) {
		return build(Arrays.asList(elements));
	}

	public static TestPlanSpecification build(List<TestPlanSpecificationElement> elements) {
		return new TestPlanSpecification(elements);
	}

	private final List<TestPlanSpecificationElement> elements;

	// Descriptor Filters are evaluated by the launcher itself after engines have done their discovery.
	// Begin predicate chain with a predicate that always evaluates to true.
	private Predicate<TestDescriptor> descriptorFilter = descriptor -> true;

	// Engine filters are handed through to all test engines to be applied during discovery
	private List<EngineFilter> engineFilters = new ArrayList<>();

	public TestPlanSpecification(List<TestPlanSpecificationElement> elements) {
		this.elements = elements;
	}

	@Override
	public Iterator<TestPlanSpecificationElement> iterator() {
		return getElements().iterator();
	}

	List<TestPlanSpecificationElement> getElements() {
		return unmodifiableList(this.elements);
	}

	public void accept(TestPlanSpecificationElementVisitor visitor) {
		elements.forEach(element -> element.accept(visitor));
	}

	public void filterWith(Predicate<TestDescriptor> filter) {
		Preconditions.notNull(filter, "filter must not be null");
		this.descriptorFilter = this.descriptorFilter.and(filter);
	}

	public void filterWith(EngineFilter filter) {
		Preconditions.notNull(filter, "filter must not be null");
		this.engineFilters.add(filter);
	}

	public List<EngineFilter> getEngineFilters() {
		return Collections.unmodifiableList(engineFilters);
	}

	public boolean acceptDescriptor(TestDescriptor testDescriptor) {
		Preconditions.notNull(testDescriptor, "testDescriptor must not be null");
		return this.descriptorFilter.test(testDescriptor);
	}

}