/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.descriptor;

import java.util.Optional;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.support.descriptor.JavaSource;
import org.junit.runner.Request;
import org.junit.runner.Runner;

/**
 * @since 5.0
 */
public class RunnerTestDescriptor extends JUnit4TestDescriptor {

	public static final char SEPARATOR = ':';

	private final Runner runner;
	private final Class<?> testClass;

	public RunnerTestDescriptor(TestDescriptor parent, Class<?> testClass, Runner runner) {
		super(parent, SEPARATOR, testClass.getName(), runner.getDescription(), Optional.of(new JavaSource(testClass)));
		this.testClass = testClass;
		this.runner = runner;
	}

	@Override
	public String getName() {
		return testClass.getName();
	}

	@Override
	public String getDisplayName() {
		return testClass.getName();
	}

	public Request toRequest() {
		return new RunnerRequest(runner);
	}

}
