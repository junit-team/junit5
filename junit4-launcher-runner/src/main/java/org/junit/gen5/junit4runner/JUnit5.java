/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4runner;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class JUnit5 extends Runner {

	private final Class<?> testClass;

	public JUnit5(Class<?> testClass) {

		this.testClass = testClass;
	}

	@Override
	public Description getDescription() {
		return Description.createSuiteDescription("JUnit 5 test suite: " + testClass.getSimpleName());
	}

	@Override
	public void run(RunNotifier notifier) {
		Result result = new Result();
		notifier.fireTestRunStarted(getDescription());
		notifier.fireTestRunFinished(result);
	}
}
