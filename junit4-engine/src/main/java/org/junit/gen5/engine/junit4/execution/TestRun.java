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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static org.junit.gen5.engine.TestExecutionResult.successful;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.Description;

class TestRun {

	private final RunnerTestDescriptor runnerTestDescriptor;
	private final Set<? extends TestDescriptor> runnerDescendants;
	private final Map<Description, TestDescriptor> descriptionToDescriptor;
	private final Map<TestDescriptor, TestExecutionResult> executionResults = new LinkedHashMap<>();
	private final Set<TestDescriptor> skippedDescriptors = new LinkedHashSet<>();
	private final Set<TestDescriptor> startedDescriptors = new LinkedHashSet<>();
	private final Set<TestDescriptor> finishedDescriptors = new LinkedHashSet<>();

	TestRun(RunnerTestDescriptor runnerTestDescriptor) {
		this.runnerTestDescriptor = runnerTestDescriptor;
		runnerDescendants = runnerTestDescriptor.allDescendants();
		// @formatter:off
		descriptionToDescriptor = concat(Stream.of(runnerTestDescriptor), runnerDescendants.stream())
			.map(JUnit4TestDescriptor.class::cast)
			.collect(toMap(JUnit4TestDescriptor::getDescription, identity()));
		// @formatter:on
	}

	RunnerTestDescriptor getRunnerTestDescriptor() {
		return runnerTestDescriptor;
	}

	boolean isDescendantOfRunnerTestDescriptor(TestDescriptor testDescriptor) {
		return runnerDescendants.contains(testDescriptor);
	}

	TestDescriptor lookupDescriptor(Description description) {
		return descriptionToDescriptor.get(description);
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

	boolean isNotStarted(TestDescriptor testDescriptor) {
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
