/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.execution;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Stream.concat;
import static org.junit.gen5.engine.TestExecutionResult.successful;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.Description;

public class TestRun {

	private final RunnerTestDescriptor runnerTestDescriptor;
	private final Set<? extends TestDescriptor> runnerDescendants;
	private final Map<Description, List<JUnit4TestDescriptor>> descriptionToDescriptors;
	private final Map<TestDescriptor, TestExecutionResult> executionResults = new LinkedHashMap<>();
	private final Set<TestDescriptor> skippedDescriptors = new LinkedHashSet<>();
	private final Set<TestDescriptor> startedDescriptors = new LinkedHashSet<>();
	private final Set<TestDescriptor> finishedDescriptors = new LinkedHashSet<>();

	public TestRun(RunnerTestDescriptor runnerTestDescriptor) {
		this.runnerTestDescriptor = runnerTestDescriptor;
		runnerDescendants = runnerTestDescriptor.allDescendants();
		// @formatter:off
		descriptionToDescriptors = concat(Stream.of(runnerTestDescriptor), runnerDescendants.stream())
			.map(JUnit4TestDescriptor.class::cast)
			.collect(groupingBy(JUnit4TestDescriptor::getDescription));
		// @formatter:on
	}

	RunnerTestDescriptor getRunnerTestDescriptor() {
		return runnerTestDescriptor;
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
	TestDescriptor lookupTestDescriptor(Description description) {
		List<JUnit4TestDescriptor> descriptors = descriptionToDescriptors.get(description);
		if (descriptors == null) {
			// TODO #40 Handle unknown description
			return null;
		}
		if (descriptors.size() == 1) {
			return descriptors.get(0);
		}
		// @formatter:off
		return descriptors.stream()
				.filter(testDescriptor -> description == testDescriptor.getDescription())
				.findFirst()
				.orElseGet(() -> descriptors.get(0));
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

	void markStarted(TestDescriptor testDescriptor) {
		startedDescriptors.add(testDescriptor);
	}

	public boolean isNotStarted(TestDescriptor testDescriptor) {
		return !startedDescriptors.contains(testDescriptor);
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
		executionResults.put(testDescriptor, result);
	}

	TestExecutionResult getStoredResultOrSuccessful(TestDescriptor testDescriptor) {
		return executionResults.getOrDefault(testDescriptor, successful());
	}

}
