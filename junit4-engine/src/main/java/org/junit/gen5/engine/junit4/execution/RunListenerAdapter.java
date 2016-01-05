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
import static org.junit.gen5.engine.TestExecutionResult.*;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Ignore;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class RunListenerAdapter extends RunListener {

	private final RunnerTestDescriptor runnerTestDescriptor;
	private final EngineExecutionListener listener;

	private final Map<Description, JUnit4TestDescriptor> descriptionToDescriptor;
	private final Map<TestDescriptor, TestExecutionResult> executionResults = new LinkedHashMap<>();
	private final Set<TestDescriptor> startedDescriptors = new LinkedHashSet<>();
	private final Set<TestDescriptor> finishedDescriptors = new LinkedHashSet<>();

	public RunListenerAdapter(RunnerTestDescriptor runnerTestDescriptor, EngineExecutionListener listener) {
		this.runnerTestDescriptor = runnerTestDescriptor;
		this.listener = listener;
		// @formatter:off
		descriptionToDescriptor = runnerTestDescriptor.allDescendants().stream()
			.filter(JUnit4TestDescriptor.class::isInstance)
			.map(JUnit4TestDescriptor.class::cast)
			.collect(toMap(JUnit4TestDescriptor::getDescription, identity()));
		// @formatter:on
		descriptionToDescriptor.put(runnerTestDescriptor.getDescription(), runnerTestDescriptor);
	}

	@Override
	public void testRunStarted(Description description) {
		fireExecutionStarted(runnerTestDescriptor);
	}

	@Override
	public void testIgnored(Description description) {
		Ignore ignoreAnnotation = description.getAnnotation(Ignore.class);
		String reason = Optional.ofNullable(ignoreAnnotation).map(Ignore::value).orElse("<unknown>");
		listener.executionSkipped(lookupDescriptor(description), reason);
	}

	@Override
	public void testStarted(Description description) {
		TestDescriptor testDescriptor = lookupDescriptor(description);
		fireExecutionStartedIncludingUnstartedAncestors(testDescriptor.getParent());
		fireExecutionStarted(testDescriptor);
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		TestDescriptor testDescriptor = lookupDescriptor(failure.getDescription());
		executionResults.put(testDescriptor, aborted(failure.getException()));
	}

	@Override
	public void testFailure(Failure failure) {
		TestDescriptor testDescriptor = lookupDescriptor(failure.getDescription());
		executionResults.put(testDescriptor, failed(failure.getException()));
	}

	@Override
	public void testFinished(Description description) {
		TestDescriptor descriptor = lookupDescriptor(description);
		fireExecutionFinished(descriptor);
		fireExecutionFinishedIncludingAncestorsWithoutUnfinishedChildren(descriptor.getParent());
	}

	@Override
	public void testRunFinished(Result result) {
		fireExecutionFinished(runnerTestDescriptor);
	}

	private void fireExecutionStartedIncludingUnstartedAncestors(Optional<TestDescriptor> parent) {
		if (parent.isPresent() && !startedDescriptors.contains(parent.get())) {
			fireExecutionStartedIncludingUnstartedAncestors(parent.get().getParent());
			fireExecutionStarted(parent.get());
		}
	}

	private void fireExecutionStarted(TestDescriptor testDescriptor) {
		startedDescriptors.add(testDescriptor);
		listener.executionStarted(testDescriptor);
	}

	private void fireExecutionFinishedIncludingAncestorsWithoutUnfinishedChildren(Optional<TestDescriptor> parent) {
		if (parent.isPresent() && canFinish(parent.get())) {
			fireExecutionFinished(parent.get());
			fireExecutionFinishedIncludingAncestorsWithoutUnfinishedChildren(parent.get().getParent());
		}
	}

	private boolean canFinish(TestDescriptor testDescriptor) {
		return !finishedDescriptors.contains(testDescriptor) //
				&& !runnerTestDescriptor.equals(testDescriptor)
				&& finishedDescriptors.containsAll(testDescriptor.getChildren());
	}

	private void fireExecutionFinished(TestDescriptor testDescriptor) {
		finishedDescriptors.add(testDescriptor);
		listener.executionFinished(testDescriptor, executionResults.getOrDefault(testDescriptor, successful()));
	}

	private TestDescriptor lookupDescriptor(Description description) {
		return descriptionToDescriptor.get(description);
	}

}
