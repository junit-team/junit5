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

import static java.util.Collections.singleton;

import java.util.Collection;

import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;

public class JUnit4TestEngine implements TestEngine {

	@Override
	public String getId() {
		return "junit4";
	}

	@Override
	public Collection<TestDescriptor> discoverTests(TestPlanSpecification specification) {
		return singleton(new DummyTestDescriptor());
	}

	@Override
	public boolean supports(TestDescriptor testDescriptor) {
		return testDescriptor instanceof DummyTestDescriptor;
	}

	@Override
	public void execute(EngineExecutionContext context) {
		for (TestDescriptor testDescriptor : context.getTestDescriptions()) {
			context.getTestExecutionListener().testStarted(testDescriptor);
			context.getTestExecutionListener().testSucceeded(testDescriptor);
		}
	}

}
