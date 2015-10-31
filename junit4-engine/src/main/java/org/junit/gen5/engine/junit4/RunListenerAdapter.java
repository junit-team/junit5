/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

class RunListenerAdapter extends RunListener {

	private final Map<Description, DescriptionTestDescriptor> description2descriptor;
	private final TestExecutionListener testExecutionListener;

	private final Set<Description> completed = new LinkedHashSet<>();

	public RunListenerAdapter(Map<Description, DescriptionTestDescriptor> description2descriptor,
			TestExecutionListener testExecutionListener) {
		this.description2descriptor = description2descriptor;
		this.testExecutionListener = testExecutionListener;
	}

	@Override
	public void testIgnored(Description description) throws Exception {
		// TODO this looks weird
		notifyTestExecutionListener(description, testExecutionListener::testStarted);
		notifyTestExecutionListener(description, null, testExecutionListener::testSkipped);
	}

	@Override
	public void testStarted(Description description) throws Exception {
		notifyTestExecutionListener(description, testExecutionListener::testStarted);
	}

	@Override
	public void testAssumptionFailure(Failure failure) {
		completed.add(failure.getDescription());
		notifyTestExecutionListener(failure.getDescription(), failure.getException(),
			testExecutionListener::testAborted);
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		completed.add(failure.getDescription());
		notifyTestExecutionListener(failure.getDescription(), failure.getException(),
			testExecutionListener::testFailed);
	}

	@Override
	public void testFinished(Description description) {
		if (!completed.contains(description)) {
			completed.add(description);
			notifyTestExecutionListener(description, testExecutionListener::testSucceeded);
		}
	}

	private void notifyTestExecutionListener(Description description, Consumer<TestDescriptor> consumer) {
		DescriptionTestDescriptor testDescriptor = lookupTestDescriptor(description);
		consumer.accept(testDescriptor);
	}

	private void notifyTestExecutionListener(Description description, Throwable cause,
			BiConsumer<TestDescriptor, Throwable> consumer) {
		DescriptionTestDescriptor testDescriptor = lookupTestDescriptor(description);
		consumer.accept(testDescriptor, cause);
	}

	private DescriptionTestDescriptor lookupTestDescriptor(Description description) {
		DescriptionTestDescriptor testDescriptor = description2descriptor.get(description);
		Preconditions.notNull(testDescriptor, () -> "Could not find TestDescriptor for Description " + description);
		return testDescriptor;
	}
}