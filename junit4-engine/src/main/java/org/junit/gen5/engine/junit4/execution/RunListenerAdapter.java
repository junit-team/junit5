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
import static org.junit.gen5.engine.TestExecutionResult.failed;

import java.util.Map;

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class RunListenerAdapter extends RunListener {

	private final EngineExecutionListener listener;
	private final Map<Description, JUnit4TestDescriptor> descriptionToDescriptor;

	public RunListenerAdapter(RunnerTestDescriptor runnerTestDescriptor, EngineExecutionListener listener) {
		this.listener = listener;
		// @formatter:off
		descriptionToDescriptor = runnerTestDescriptor.allDescendants().stream()
			.filter(JUnit4TestDescriptor.class::isInstance)
			.map(JUnit4TestDescriptor.class::cast)
			.collect(toMap(JUnit4TestDescriptor::getDescription, identity()));
		// @formatter:on
	}

	@Override
	public void testStarted(Description description) throws Exception {
		listener.executionStarted(lookupDescriptor(description));
	}

	@Override
	public void testFailure(Failure failure) throws Exception {
		TestDescriptor testDescriptor = lookupDescriptor(failure.getDescription());
		TestExecutionResult result = failed(failure.getException());
		listener.executionFinished(testDescriptor, result);
	}

	private TestDescriptor lookupDescriptor(Description description) {
		return descriptionToDescriptor.get(description);
	}

}
