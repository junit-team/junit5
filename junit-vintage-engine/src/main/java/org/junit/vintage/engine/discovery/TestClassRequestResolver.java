/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.SEGMENT_TYPE_RUNNER;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import java.util.logging.Logger;

import org.junit.platform.engine.UniqueId;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runners.model.RunnerBuilder;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageTestDescriptor;
import org.junit.vintage.engine.support.UniqueIdReader;
import org.junit.vintage.engine.support.UniqueIdStringifier;

/**
 * @since 4.12
 */
class TestClassRequestResolver {

	private static final RunnerBuilder RUNNER_BUILDER = new DefensiveAllDefaultPossibilitiesBuilder();
	private final Logger logger;

	private final UniqueIdReader uniqueIdReader;
	private final UniqueIdStringifier uniqueIdStringifier = new UniqueIdStringifier();

	TestClassRequestResolver(Logger logger) {
		this.logger = logger;
		this.uniqueIdReader = new UniqueIdReader(logger);
	}

	RunnerTestDescriptor createRunnerTestDescriptor(TestClassRequest request, UniqueId engineId) {
		Class<?> testClass = request.getTestClass();
		Runner runner = RUNNER_BUILDER.safeRunnerForClass(testClass);
		if (runner == null) {
			return null;
		}
		return determineRunnerTestDescriptor(testClass, runner, request.getFilters(), engineId);
	}

	private RunnerTestDescriptor determineRunnerTestDescriptor(Class<?> testClass, Runner runner,
			List<RunnerTestDescriptorAwareFilter> filters, UniqueId engineId) {
		RunnerTestDescriptor runnerTestDescriptor = createCompleteRunnerTestDescriptor(testClass, runner, engineId);
		if (!filters.isEmpty()) {
			if (runner instanceof Filterable) {
				Filter filter = createOrFilter(filters, runnerTestDescriptor);
				Runner filteredRunner = runnerTestDescriptor.toRequest().filterWith(filter).getRunner();
				runnerTestDescriptor = createCompleteRunnerTestDescriptor(testClass, filteredRunner, engineId);
			}
			else {
				logger.warning(() -> "Runner " + runner.getClass().getName() //
						+ " (used on " + testClass.getName() + ") does not support filtering" //
						+ " and will therefore be run completely.");
			}
		}
		return runnerTestDescriptor;
	}

	private Filter createOrFilter(List<RunnerTestDescriptorAwareFilter> filters,
			RunnerTestDescriptor runnerTestDescriptor) {
		filters.forEach(filter -> filter.initialize(runnerTestDescriptor));
		return new OrFilter(filters);
	}

	private RunnerTestDescriptor createCompleteRunnerTestDescriptor(Class<?> testClass, Runner runner,
			UniqueId engineId) {
		UniqueId id = engineId.append(SEGMENT_TYPE_RUNNER, testClass.getName());
		RunnerTestDescriptor runnerTestDescriptor = new RunnerTestDescriptor(id, testClass, runner);
		addChildrenRecursively(runnerTestDescriptor);
		return runnerTestDescriptor;
	}

	private void addChildrenRecursively(VintageTestDescriptor parent) {
		List<Description> children = parent.getDescription().getChildren();
		// Use LinkedHashMap to preserve order, ArrayList for fast access by index
		Map<String, List<Description>> childrenByUniqueId = children.stream().collect(
			groupingBy(uniqueIdReader.andThen(uniqueIdStringifier), LinkedHashMap::new, toCollection(ArrayList::new)));
		for (Entry<String, List<Description>> entry : childrenByUniqueId.entrySet()) {
			String uniqueId = entry.getKey();
			List<Description> childrenWithSameUniqueId = entry.getValue();
			IntFunction<String> uniqueIdGenerator = determineUniqueIdGenerator(uniqueId, childrenWithSameUniqueId);
			for (int index = 0; index < childrenWithSameUniqueId.size(); index++) {
				String reallyUniqueId = uniqueIdGenerator.apply(index);
				Description description = childrenWithSameUniqueId.get(index);
				UniqueId id = parent.getUniqueId().append(VintageTestDescriptor.SEGMENT_TYPE_TEST, reallyUniqueId);
				VintageTestDescriptor child = new VintageTestDescriptor(id, description);
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
