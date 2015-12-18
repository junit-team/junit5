/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.logging.Logger;

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * @since 5.0
 */
public class Launcher {

	private static final Logger LOG = Logger.getLogger(Launcher.class.getName());

	private final TestExecutionListenerRegistry listenerRegistry = new TestExecutionListenerRegistry();
	private final TestEngineRegistry testEngineRegistry;

	public Launcher() {
		this(new ServiceLoaderTestEngineRegistry());
	}

	// for tests only
	Launcher(TestEngineRegistry testEngineRegistry) {
		this.testEngineRegistry = testEngineRegistry;
	}

	public void registerTestExecutionListeners(TestExecutionListener... testListeners) {
		listenerRegistry.registerListener(testListeners);
	}

	public TestPlan discover(TestPlanSpecification specification) {
		return TestPlan.from(discoverRootDescriptor(specification));
	}

	private RootTestDescriptor discoverRootDescriptor(TestPlanSpecification specification) {
		RootTestDescriptor root = new RootTestDescriptor();
		for (TestEngine testEngine : testEngineRegistry.lookupAllTestEngines()) {
			LOG.info("Discovering tests in engine " + testEngine.getId());
			TestDescriptor engineRoot = testEngine.discoverTests(specification);
			root.addTestDescriptorForEngine(testEngine, engineRoot);
		}
		root.applyFilters(specification);
		root.prune();
		return root;
	}

	public void execute(TestPlanSpecification specification) {
		execute(discoverRootDescriptor(specification));
	}

	private void execute(RootTestDescriptor root) {
		TestPlan testPlan = TestPlan.from(root);
		TestExecutionListener testExecutionListener = listenerRegistry.getCompositeTestExecutionListener();
		testExecutionListener.testPlanExecutionStarted(testPlan);
		ExecutionListenerAdapter engineExecutionListener = new ExecutionListenerAdapter(testPlan,
			testExecutionListener);
		for (TestEngine testEngine : root.getTestEngines()) {
			TestDescriptor testDescriptor = root.getTestDescriptorFor(testEngine);
			testEngine.execute(new ExecutionRequest(testDescriptor, engineExecutionListener));
		}
		testExecutionListener.testPlanExecutionFinished(testPlan);
	}

	static class ExecutionListenerAdapter implements EngineExecutionListener {

		private final TestPlan testPlan;
		private final TestExecutionListener testExecutionListener;

		public ExecutionListenerAdapter(TestPlan testPlan, TestExecutionListener testExecutionListener) {
			this.testPlan = testPlan;
			this.testExecutionListener = testExecutionListener;
		}

		@Override
		public void dynamicTestRegistered(TestDescriptor testDescriptor) {
			TestIdentifier testIdentifier = TestIdentifier.from(testDescriptor);
			testPlan.add(testIdentifier);
			testExecutionListener.dynamicTestRegistered(testIdentifier);
		}

		@Override
		public void executionStarted(TestDescriptor testDescriptor) {
			testExecutionListener.executionStarted(getTestIdentifier(testDescriptor));
		}

		@Override
		public void executionSkipped(TestDescriptor testDescriptor, String reason) {
			testExecutionListener.executionSkipped(getTestIdentifier(testDescriptor), reason);
		}

		@Override
		public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
			testExecutionListener.executionFinished(getTestIdentifier(testDescriptor), testExecutionResult);
		}

		private TestIdentifier getTestIdentifier(TestDescriptor testDescriptor) {
			return testPlan.getTestIdentifier(new TestId(testDescriptor.getUniqueId()));
		}
	}

}
