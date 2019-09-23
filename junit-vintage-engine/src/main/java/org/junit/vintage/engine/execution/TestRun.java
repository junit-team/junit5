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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static org.junit.platform.engine.TestExecutionResult.failed;
import static org.junit.platform.engine.TestExecutionResult.successful;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
	private final Map<Description, VintageTestDescriptor> identityDescriptionToDescriptors;
	private final Map<Description, VintageTestDescriptor> descriptionToDescriptors;
	private final Map<TestDescriptor, List<TestExecutionResult>> executionResults = new LinkedHashMap<>();

	TestRun(RunnerTestDescriptor runnerTestDescriptor) {
		this.runnerTestDescriptor = runnerTestDescriptor;
		Set<? extends TestDescriptor> runnerDescendants = runnerTestDescriptor.getDescendants();
		// @formatter:off
		Map<Description, VintageTestDescriptor> m = new IdentityHashMap<>();
		Map<Description, VintageTestDescriptor> m2 = new HashMap<>();
		concat(Stream.of(runnerTestDescriptor), runnerDescendants.stream())
				.map(VintageTestDescriptor.class::cast)
				.forEach(v -> {
					m.put(v.getDescription(), v);
					m2.put(v.getDescription(), v);
				});
		identityDescriptionToDescriptors = m;
		descriptionToDescriptors = m2;
		// @formatter:on
	}

	RunnerTestDescriptor getRunnerTestDescriptor() {
		return runnerTestDescriptor;
	}

	VintageTestDescriptor lookupTestDescriptor(Description description) {
		VintageTestDescriptor result = identityDescriptionToDescriptors.get(description);
		if (result != null) {
			return result;
		}
		return descriptionToDescriptors.get(description);
	}

	void dynamicTestRegistered(VintageTestDescriptor testDescriptor) {
		identityDescriptionToDescriptors.put(testDescriptor.getDescription(), testDescriptor);
		descriptionToDescriptors.put(testDescriptor.getDescription(), testDescriptor);
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
