/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testdoubles;

import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;

public class EngineExecutionListenerSpy implements EngineExecutionListener {
	public List<TestDescriptor> foundStartedContainers = new LinkedList<>();
	public List<TestDescriptor> foundFinishedContainers = new LinkedList<>();
	public List<TestDescriptor> foundDynamicTests = new LinkedList<>();
	public List<TestDescriptor> foundStartedTests = new LinkedList<>();
	public List<TestDescriptor> foundSkippedTests = new LinkedList<>();
	public List<TestDescriptor> foundAbortedTests = new LinkedList<>();
	public List<TestDescriptor> foundFailedTests = new LinkedList<>();
	public List<TestDescriptor> foundSucceededTests = new LinkedList<>();

	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		foundDynamicTests.add(testDescriptor);
	}

	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		if (testDescriptor.isContainer()) {
      foundStartedContainers.add(testDescriptor);
    } else {
      foundStartedTests.add(testDescriptor);
    }
	}

	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		foundSkippedTests.add(testDescriptor);
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
    if (testDescriptor.isContainer()) {
      foundFinishedContainers.add(testDescriptor);
    } else {
      switch (testExecutionResult.getStatus()) {
        case SUCCESSFUL:
          foundSucceededTests.add(testDescriptor);
          break;
        case ABORTED:
          foundAbortedTests.add(testDescriptor);
          break;
        case FAILED:
          foundFailedTests.add(testDescriptor);
          break;
      }
    }
	}
}
