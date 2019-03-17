/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.execution;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.TestExecutionResult.failed;
import static org.junit.platform.engine.TestExecutionResult.successful;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.runner.Description;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageTestDescriptor;
import org.opentest4j.MultipleFailuresError;

/**
 * @since 4.12
 */
class TestRun {

	private final RunnerTestDescriptor runnerTestDescriptor;
	private final Set<TestDescriptor> runnerDescendants;
	private final Map<Description, List<VintageTestDescriptor>> descriptionToDescriptors;
	private final Map<TestDescriptor, List<TestExecutionResult>> executionResults = new LinkedHashMap<>();
	private final Set<TestDescriptor> skippedDescriptors = new LinkedHashSet<>();
	private final Map<TestDescriptor, EventType> startedDescriptors = new LinkedHashMap<>();
	private final Set<TestDescriptor> finishedDescriptors = new LinkedHashSet<>();

	TestRun(RunnerTestDescriptor runnerTestDescriptor) {
		this.runnerTestDescriptor = runnerTestDescriptor;
		runnerDescendants = new LinkedHashSet<>(runnerTestDescriptor.getDescendants());
		// @formatter:off
		descriptionToDescriptors = concat(Stream.of(runnerTestDescriptor), runnerDescendants.stream())
				.map(VintageTestDescriptor.class::cast)
				.collect(groupingBy(
						VintageTestDescriptor::getDescription, HashMap::new, toCollection(ArrayList::new)));
		// @formatter:on
	}

	void registerDynamicTest(VintageTestDescriptor testDescriptor) {
		descriptionToDescriptors.computeIfAbsent(testDescriptor.getDescription(),
			key -> new CopyOnWriteArrayList<>()).add(testDescriptor);
		runnerDescendants.add(testDescriptor);
	}

	RunnerTestDescriptor getRunnerTestDescriptor() {
		return runnerTestDescriptor;
	}

	Collection<TestDescriptor> getInProgressTestDescriptorsWithSyntheticStartEvents() {
		List<TestDescriptor> result = startedDescriptors.entrySet().stream() //
				.filter(entry -> entry.getValue().equals(EventType.SYNTHETIC)) //
				.map(Entry::getKey) //
				.filter(descriptor -> !isFinished(descriptor)) //
				.collect(toCollection(ArrayList::new));
		Collections.reverse(result);
		return result;
	}

	boolean isDescendantOfRunnerTestDescriptor(TestDescriptor testDescriptor) {
		return runnerDescendants.contains(testDescriptor);
	}

	/**
	 * Returns the {@link TestDescriptor} that represents the specified
	 * {@link Description}.
	 *
	 * <p>There are edge cases where multiple {@link Description Descriptions}
	 * with the same {@code uniqueId} exist, e.g. when using overloaded methods
	 * to define {@linkplain org.junit.experimental.theories.Theory theories}.
	 * In this case, we try to find the correct {@link TestDescriptor} by
	 * checking for object identity on the {@link Description} it represents.
	 *
	 * @param description the {@code Description} to look up
	 */
	Optional<VintageTestDescriptor> lookupTestDescriptor(Description description) {
		List<VintageTestDescriptor> descriptors = descriptionToDescriptors.get(description);
		if (descriptors == null) {
			return Optional.empty();
		}
		if (descriptors.size() == 1) {
			return Optional.of(getOnlyElement(descriptors));
		}
		// @formatter:off
		return descriptors.stream()
				.filter(testDescriptor -> description == testDescriptor.getDescription())
				.findFirst();
		// @formatter:on
	}

	void markSkipped(TestDescriptor testDescriptor) {
		skippedDescriptors.add(testDescriptor);
	}

	boolean isNotSkipped(TestDescriptor testDescriptor) {
		return !isSkipped(testDescriptor);
	}

	boolean isSkipped(TestDescriptor testDescriptor) {
		return skippedDescriptors.contains(testDescriptor);
	}

	void markStarted(TestDescriptor testDescriptor, EventType eventType) {
		startedDescriptors.put(testDescriptor, eventType);
	}

	boolean isNotStarted(TestDescriptor testDescriptor) {
		return !startedDescriptors.containsKey(testDescriptor);
	}

	void markFinished(TestDescriptor testDescriptor) {
		finishedDescriptors.add(testDescriptor);
	}

	boolean isNotFinished(TestDescriptor testDescriptor) {
		return !isFinished(testDescriptor);
	}

	boolean isFinished(TestDescriptor testDescriptor) {
		return finishedDescriptors.contains(testDescriptor);
	}

	boolean areAllFinishedOrSkipped(Set<? extends TestDescriptor> testDescriptors) {
		return testDescriptors.stream().allMatch(this::isFinishedOrSkipped);
	}

	boolean isFinishedOrSkipped(TestDescriptor testDescriptor) {
		return isFinished(testDescriptor) || isSkipped(testDescriptor);
	}

	void storeResult(TestDescriptor testDescriptor, TestExecutionResult result) {
		List<TestExecutionResult> testExecutionResults = executionResults.computeIfAbsent(testDescriptor,
			key -> new ArrayList<>());
		testExecutionResults.add(result);
	}

	TestExecutionResult getStoredResultOrSuccessful(TestDescriptor testDescriptor) {
		List<TestExecutionResult> testExecutionResults = executionResults.get(testDescriptor);

		if (testExecutionResults == null) {
			return successful();
		}
		if (testExecutionResults.size() == 1) {
			return testExecutionResults.get(0);
		}
		// @formatter:off
		List<Throwable> failures = testExecutionResults
				.stream()
				.map(TestExecutionResult::getThrowable)
				.map(Optional::get)
				.collect(toList());
		// @formatter:on
		return failed(new MultipleFailuresError("", failures));
	}
}
