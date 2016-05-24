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

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.util.Optional;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.support.descriptor.JavaClassSource;
import org.junit.runner.Request;
import org.junit.runner.Runner;

/**
 * @since 5.0
 */
@API(Internal)
public class RunnerTestDescriptor extends JUnit4TestDescriptor {

	private final Runner runner;
	private final Class<?> testClass;

	public RunnerTestDescriptor(TestDescriptor parent, Class<?> testClass, Runner runner) {
		super(parent, SEGMENT_TYPE_RUNNER, testClass.getName(), runner.getDescription(),
			Optional.of(new JavaClassSource(testClass)));
		this.testClass = testClass;
		this.runner = runner;
	}

	public Runner getRunner() {
		return this.runner;
	}

	public Class<?> getTestClass() {
		return this.testClass;
	}

	@Override
	public String getDisplayName() {
		return this.testClass.getName();
	}

	public Request toRequest() {
		return new RunnerRequest(this.runner);
	}

}
