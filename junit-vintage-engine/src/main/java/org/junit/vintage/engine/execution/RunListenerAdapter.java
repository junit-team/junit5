/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.execution;

import java.util.Optional;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;

/**
 * @since 5.0
 */
class RunListenerAdapter extends RunListener {

	private final TestRun testRun;
	private final EngineExecutionListener listener;

	RunListenerAdapter(TestRun testRun, EngineExecutionListener listener) {
		this.testRun = testRun;
		this.listener = listener;
	}

	@Override
	public void testRunStarted(Description description) {
		// If it's not a suite it might be skipped entirely later on.
		if (description.isSuite()) {
			fireExecutionStarted(testRun.getRunnerTestDescriptor());
		}
	}

	@Override
	public void testIgnored(Description description) {
		testRun.lookupTestDescriptor(description).ifPresent(
			testDescriptor -> testIgnored(testDescriptor, determineReasonForIgnoredTest(description)));
	}

	@Override
	public void testStarted(Description description) {
		testRun.lookupTestDescriptor(description).ifPresent(this::testStarted);
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		handleFailure(failure, TestExecutionResult::aborted);
	}

	@Override
	public void testFailure(Failure failure) {
		handleFailure(failure, TestExecutionResult::failed);
	}

	@Override
	public void testFinished(Description description) {
		testRun.lookupTestDescriptor(description).ifPresent(this::testFinished);
	}

	@Override
	public void testRunFinished(Result result) {
		RunnerTestDescriptor runnerTestDescriptor = testRun.getRunnerTestDescriptor();
		if (testRun.isNotSkipped(runnerTestDescriptor)) {
			if (testRun.isNotStarted(runnerTestDescriptor)) {
				fireExecutionStarted(runnerTestDescriptor);
			}
			fireExecutionFinished(runnerTestDescriptor);
		}
	}

	private void handleFailure(Failure failure, Function<Throwable, TestExecutionResult> resultCreator) {
		testRun.lookupTestDescriptor(failure.getDescription()).ifPresent(
			testDescriptor -> handleFailure(failure, resultCreator, testDescriptor));
	}

	private void handleFailure(Failure failure, Function<Throwable, TestExecutionResult> resultCreator,
			TestDescriptor testDescriptor) {
		TestExecutionResult result = resultCreator.apply(failure.getException());
		testRun.storeResult(testDescriptor, result);
		if (testDescriptor.isContainer() && testRun.isDescendantOfRunnerTestDescriptor(testDescriptor)) {
			fireMissingContainerEvents(testDescriptor);
		}
	}

	private void fireMissingContainerEvents(TestDescriptor testDescriptor) {
		if (testRun.isNotStarted(testDescriptor)) {
			testStarted(testDescriptor);
		}
		if (testRun.isNotFinished(testDescriptor)) {
			testFinished(testDescriptor);
		}
	}

	private void testIgnored(TestDescriptor testDescriptor, String reason) {
		fireExecutionStartedIncludingUnstartedAncestors(testDescriptor.getParent());
		fireExecutionSkipped(testDescriptor, reason);
		fireExecutionFinishedIncludingAncestorsWithoutPendingChildren(testDescriptor.getParent());
	}

	private String determineReasonForIgnoredTest(Description description) {
		Ignore ignoreAnnotation = description.getAnnotation(Ignore.class);
		return Optional.ofNullable(ignoreAnnotation).map(Ignore::value).orElse("<unknown>");
	}

	private void testStarted(TestDescriptor testDescriptor) {
		fireExecutionStartedIncludingUnstartedAncestors(testDescriptor.getParent());
		fireExecutionStarted(testDescriptor);
	}

	private void testFinished(TestDescriptor descriptor) {
		fireExecutionFinished(descriptor);
		fireExecutionFinishedIncludingAncestorsWithoutPendingChildren(descriptor.getParent());
	}

	private void fireExecutionStartedIncludingUnstartedAncestors(Optional<TestDescriptor> parent) {
		if (parent.isPresent() && canStart(parent.get())) {
			fireExecutionStartedIncludingUnstartedAncestors(parent.get().getParent());
			fireExecutionStarted(parent.get());
		}
	}

	private void fireExecutionFinishedIncludingAncestorsWithoutPendingChildren(Optional<TestDescriptor> parent) {
		if (parent.isPresent() && canFinish(parent.get())) {
			fireExecutionFinished(parent.get());
			fireExecutionFinishedIncludingAncestorsWithoutPendingChildren(parent.get().getParent());
		}
	}

	private boolean canStart(TestDescriptor testDescriptor) {
		return testRun.isNotStarted(testDescriptor) //
				&& testRun.isDescendantOfRunnerTestDescriptor(testDescriptor);
	}

	private boolean canFinish(TestDescriptor testDescriptor) {
		return testRun.isNotFinished(testDescriptor) //
				&& testRun.isDescendantOfRunnerTestDescriptor(testDescriptor)
				&& testRun.areAllFinishedOrSkipped(testDescriptor.getChildren());
	}

	private void fireExecutionSkipped(TestDescriptor testDescriptor, String reason) {
		testRun.markSkipped(testDescriptor);
		listener.executionSkipped(testDescriptor, reason);
	}

	private void fireExecutionStarted(TestDescriptor testDescriptor) {
		testRun.markStarted(testDescriptor);
		listener.executionStarted(testDescriptor);
	}

	private void fireExecutionFinished(TestDescriptor testDescriptor) {
		testRun.markFinished(testDescriptor);
		listener.executionFinished(testDescriptor, testRun.getStoredResultOrSuccessful(testDescriptor));
	}

}
