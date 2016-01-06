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

import java.util.Optional;
import java.util.function.Function;

import org.junit.Ignore;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class RunListenerAdapter extends RunListener {

	private final TestRun testRun;
	private final EngineExecutionListener listener;

	public RunListenerAdapter(RunnerTestDescriptor runnerTestDescriptor, EngineExecutionListener listener) {
		this.testRun = new TestRun(runnerTestDescriptor);
		this.listener = listener;
	}

	@Override
	public void testRunStarted(Description description) {
		fireExecutionStarted(testRun.getRunnerTestDescriptor());
	}

	@Override
	public void testIgnored(Description description) {
		Ignore ignoreAnnotation = description.getAnnotation(Ignore.class);
		String reason = Optional.ofNullable(ignoreAnnotation).map(Ignore::value).orElse("<unknown>");
		listener.executionSkipped(testRun.lookupDescriptor(description), reason);
	}

	@Override
	public void testStarted(Description description) {
		TestDescriptor testDescriptor = testRun.lookupDescriptor(description);
		fireExecutionStartedIncludingUnstartedAncestors(testDescriptor.getParent());
		fireExecutionStarted(testDescriptor);
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		handleFailure(failure, TestExecutionResult::aborted);
	}

	@Override
	public void testFailure(Failure failure) {
		handleFailure(failure, TestExecutionResult::failed);
	}

	private void handleFailure(Failure failure, Function<Throwable, TestExecutionResult> resultCreator) {
		Description description = failure.getDescription();
		TestDescriptor testDescriptor = testRun.lookupDescriptor(description);
		TestExecutionResult result = resultCreator.apply(failure.getException());
		testRun.storeResult(testDescriptor, result);
		if (testDescriptor.isContainer() && testRun.isNotRunnerTestDescriptor(testDescriptor)) {
			fireMissingContainerEvents(description, testDescriptor);
		}
	}

	private void fireMissingContainerEvents(Description description, TestDescriptor testDescriptor) {
		if (testRun.isNotStarted(testDescriptor)) {
			testStarted(description);
		}
		if (testRun.isNotFinished(testDescriptor)) {
			testFinished(description);
		}
	}

	@Override
	public void testFinished(Description description) {
		TestDescriptor descriptor = testRun.lookupDescriptor(description);
		fireExecutionFinished(descriptor);
		fireExecutionFinishedIncludingAncestorsWithoutUnfinishedChildren(descriptor.getParent());
	}

	@Override
	public void testRunFinished(Result result) {
		fireExecutionFinished(testRun.getRunnerTestDescriptor());
	}

	private void fireExecutionStartedIncludingUnstartedAncestors(Optional<TestDescriptor> parent) {
		if (parent.isPresent() && testRun.isNotStarted(parent.get())) {
			fireExecutionStartedIncludingUnstartedAncestors(parent.get().getParent());
			fireExecutionStarted(parent.get());
		}
	}

	private void fireExecutionStarted(TestDescriptor testDescriptor) {
		testRun.markStarted(testDescriptor);
		listener.executionStarted(testDescriptor);
	}

	private void fireExecutionFinishedIncludingAncestorsWithoutUnfinishedChildren(Optional<TestDescriptor> parent) {
		if (parent.isPresent() && canFinish(parent.get())) {
			fireExecutionFinished(parent.get());
			fireExecutionFinishedIncludingAncestorsWithoutUnfinishedChildren(parent.get().getParent());
		}
	}

	private boolean canFinish(TestDescriptor testDescriptor) {
		return testRun.isNotFinished(testDescriptor) //
				&& testRun.isNotRunnerTestDescriptor(testDescriptor)
				&& testRun.areAllFinished(testDescriptor.getChildren());
	}

	private void fireExecutionFinished(TestDescriptor testDescriptor) {
		testRun.markFinished(testDescriptor);
		listener.executionFinished(testDescriptor, testRun.getStoredResultOrSuccessful(testDescriptor));
	}

}
