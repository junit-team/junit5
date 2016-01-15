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

import static java.util.stream.Collectors.*;
import static org.junit.gen5.commons.util.ReflectionUtils.*;
import static org.junit.gen5.engine.junit4.discovery.RunnerTestDescriptorAwareFilter.adapter;

import java.io.File;
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
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElementVisitor;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.gen5.engine.specification.AllClassFilters;
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

	public void resolve(TestPlanSpecification specification) {
		ClassFilter classFilter = new AllClassFilters(specification.getEngineFiltersByType(ClassFilter.class));
		RunnerBuilder runnerBuilder = new DefensiveAllDefaultPossibilitiesBuilder();
		Set<Class<?>> unfilteredTestClasses = new LinkedHashSet<>();
		Map<Class<?>, List<RunnerTestDescriptorAwareFilter>> filteredTestClasses = new LinkedHashMap<>();
		specification.accept(new TestPlanSpecificationElementVisitor() {

			private final IsPotentialJUnit4TestClass classTester = new IsPotentialJUnit4TestClass();

			@Override
			public void visitClass(Class<?> testClass) {
				if (classFilter.acceptClass(testClass)) {
					unfilteredTestClasses.add(testClass);
				}
			}

			@Override
			public void visitAllTests(File rootDirectory) {
				findAllClassesInClasspathRoot(rootDirectory, classTester).stream().forEach(this::visitClass);
			}

			@Override
			public void visitPackage(String packageName) {
				findAllClassesInPackage(packageName, classTester).stream().forEach(this::visitClass);
			}

			@Override
			public void visitMethod(Class<?> testClass, Method testMethod) {
				Description methodDescription = Description.createTestDescription(testClass, testMethod.getName());
				RunnerTestDescriptorAwareFilter filter = adapter(Filter.matchMethodDescription(methodDescription));
				filteredTestClasses.computeIfAbsent(testClass, key -> new LinkedList<>()).add(filter);
			}

			@Override
			public void visitUniqueId(String uniqueId) {
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
			}

			private String determineTestClassName(String uniqueId, String enginePrefix) {
				int endIndex = uniqueId.indexOf(DEFAULT_SEPARATOR);
				if (endIndex >= 0) {
					return uniqueId.substring(enginePrefix.length(), endIndex);
				}
				return uniqueId.substring(enginePrefix.length());
			}
		});

		// TODO #40 @marcphilipp Clean this up when uniqueIds are resolved
		for (Class<?> testClass : unfilteredTestClasses) {
			Runner runner = runnerBuilder.safeRunnerForClass(testClass);
			if (runner != null) {
				engineDescriptor.addChild(createCompleteRunnerTestDescriptor(testClass, runner));
			}
		}
		for (Entry<Class<?>, List<RunnerTestDescriptorAwareFilter>> entry : filteredTestClasses.entrySet()) {
			Class<?> testClass = entry.getKey();
			List<RunnerTestDescriptorAwareFilter> filters = entry.getValue();
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
