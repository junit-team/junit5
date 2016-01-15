/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.junit.gen5.commons.util.ReflectionUtils.findAllClassesInClasspathRoot;
import static org.junit.gen5.commons.util.ReflectionUtils.findAllClassesInPackage;
import static org.junit.gen5.engine.junit4.discovery.RunnerTestDescriptorAwareFilter.adapter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.gen5.engine.specification.AllClassFilters;
import org.junit.gen5.engine.specification.ClassSelector;
import org.junit.gen5.engine.specification.ClasspathSelector;
import org.junit.gen5.engine.specification.MethodSelector;
import org.junit.gen5.engine.specification.PackageNameSelector;
import org.junit.gen5.engine.specification.UniqueIdSelector;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.model.RunnerBuilder;

public class JUnit4TestPlanSpecificationResolver {

	private static final char DEFAULT_SEPARATOR = '/';

	private final EngineDescriptor engineDescriptor;

	public JUnit4TestPlanSpecificationResolver(EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolve(DiscoveryRequest request) {

		IsPotentialJUnit4TestClass classTester = new IsPotentialJUnit4TestClass();
		Set<Class<?>> unfilteredTestClasses = new LinkedHashSet<>();
		Map<Class<?>, List<RunnerTestDescriptorAwareFilter>> filteredTestClasses = new LinkedHashMap<>();

		request.getElementsByType(ClasspathSelector.class).forEach(selector -> {
			findAllClassesInClasspathRoot(selector.getClasspathRoot(), classTester).forEach(unfilteredTestClasses::add);
		});

		request.getElementsByType(PackageNameSelector.class).forEach(selector -> {
			findAllClassesInPackage(selector.getPackageName(), classTester).forEach(unfilteredTestClasses::add);
		});

		request.getElementsByType(ClassSelector.class).forEach(selector -> {
			unfilteredTestClasses.add(selector.getTestClass());
		});

		request.getElementsByType(MethodSelector.class).forEach(selector -> {
			Class<?> testClass = selector.getTestClass();
			Method testMethod = selector.getTestMethod();
			Description methodDescription = Description.createTestDescription(testClass, testMethod.getName());
			RunnerTestDescriptorAwareFilter filter = adapter(Filter.matchMethodDescription(methodDescription));
			filteredTestClasses.computeIfAbsent(testClass, key -> new LinkedList<>()).add(filter);
		});

		request.getElementsByType(UniqueIdSelector.class).forEach(selector -> {
			String uniqueId = selector.getUniqueId();
			String enginePrefix = engineDescriptor.getEngine().getId() + RunnerTestDescriptor.SEPARATOR;
			if (uniqueId.startsWith(enginePrefix)) {
				String testClassName = determineTestClassName(uniqueId, enginePrefix);
				Optional<Class<?>> testClass = ReflectionUtils.loadClass(testClassName);
				if (testClass.isPresent()) {
					RunnerTestDescriptorAwareFilter filter = new UniqueIdFilter(uniqueId);
					filteredTestClasses.computeIfAbsent(testClass.get(), key -> new LinkedList<>()).add(filter);
				}
				else {
					// TODO Log warning
				}
			}
		});

		ClassFilter classFilter = new AllClassFilters(request.getEngineFiltersByType(ClassFilter.class));

		convertToTestDescriptors(unfilteredTestClasses, filteredTestClasses, classFilter);
	}

	private void convertToTestDescriptors(Set<Class<?>> unfilteredTestClasses,
			Map<Class<?>, List<RunnerTestDescriptorAwareFilter>> filteredTestClasses, ClassFilter classFilter) {
		RunnerBuilder runnerBuilder = new DefensiveAllDefaultPossibilitiesBuilder();

		// TODO #40 @marcphilipp Clean this up when uniqueIds are resolved
		for (Class<?> testClass : unfilteredTestClasses) {
			if (classFilter.acceptClass(testClass)) {
				Runner runner = runnerBuilder.safeRunnerForClass(testClass);
				if (runner != null) {
					engineDescriptor.addChild(createCompleteRunnerTestDescriptor(testClass, runner));
				}
			}
		}
		for (Entry<Class<?>, List<RunnerTestDescriptorAwareFilter>> entry : filteredTestClasses.entrySet()) {
			Class<?> testClass = entry.getKey();
			List<RunnerTestDescriptorAwareFilter> filters = entry.getValue();
			// TODO check ClassFilter
			Runner runner = runnerBuilder.safeRunnerForClass(testClass);
			if (runner != null) {
				RunnerTestDescriptor originalDescriptor = createCompleteRunnerTestDescriptor(testClass, runner);
				Filter filter = createOrFilter(filters, originalDescriptor);
				Runner filteredRunner = originalDescriptor.toRequest().filterWith(filter).getRunner();
				// TODO Log warning if runner does not implement Filterable
				engineDescriptor.addChild(createCompleteRunnerTestDescriptor(testClass, filteredRunner));
			}
		}
	}

	private String determineTestClassName(String uniqueId, String enginePrefix) {
		int endIndex = uniqueId.indexOf(DEFAULT_SEPARATOR);
		if (endIndex >= 0) {
			return uniqueId.substring(enginePrefix.length(), endIndex);
		}
		return uniqueId.substring(enginePrefix.length());
	}

	private Filter createOrFilter(List<RunnerTestDescriptorAwareFilter> filters,
			RunnerTestDescriptor runnerTestDescriptor) {
		filters.stream().forEach(filter -> filter.initialize(runnerTestDescriptor));
		return new OrFilter(filters);
	}

	private RunnerTestDescriptor createCompleteRunnerTestDescriptor(Class<?> testClass, Runner runner) {
		RunnerTestDescriptor runnerTestDescriptor = new RunnerTestDescriptor(engineDescriptor, testClass, runner);
		addChildrenRecursively(runnerTestDescriptor);
		return runnerTestDescriptor;
	}

	private void addChildrenRecursively(JUnit4TestDescriptor parent) {
		List<Description> children = parent.getDescription().getChildren();
		// Use LinkedHashMap to preserve order, ArrayList for fast access by index
		Map<String, List<Description>> childrenByUniqueId = children.stream().collect(
			groupingBy(UniqueIdExtractor::toUniqueId, LinkedHashMap::new, toCollection(ArrayList::new)));
		for (Entry<String, List<Description>> entry : childrenByUniqueId.entrySet()) {
			String uniqueId = entry.getKey();
			List<Description> childrenWithSameUniqueId = entry.getValue();
			IntFunction<String> uniqueIdGenerator = determineUniqueIdGenerator(uniqueId, childrenWithSameUniqueId);
			for (int index = 0; index < childrenWithSameUniqueId.size(); index++) {
				String reallyUniqueId = uniqueIdGenerator.apply(index);
				Description description = childrenWithSameUniqueId.get(index);
				JUnit4TestDescriptor child = new JUnit4TestDescriptor(parent, DEFAULT_SEPARATOR, reallyUniqueId,
					description);
				parent.addChild(child);
				addChildrenRecursively(child);
			}
		}
	}

	private IntFunction<String> determineUniqueIdGenerator(String uniqueId,
			List<Description> childrenWithSameUniqueId) {
		if (childrenWithSameUniqueId.size() == 1) {
			return index -> uniqueId;
		}
		return index -> uniqueId + "[" + index + "]";
	}
}
