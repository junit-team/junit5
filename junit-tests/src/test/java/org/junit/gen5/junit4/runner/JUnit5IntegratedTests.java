/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assumptions.assumeFalse;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.gen5.launcher.main.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.junit.runner.Description.createSuiteDescription;
import static org.junit.runner.Description.createTestDescription;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;
import org.junit.gen5.launcher.TestId;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.mockito.InOrder;

class JUnit5IntegratedTests {

	@Test
	void convertsTestIdentifiersIntoDescriptions() throws Exception {
		DummyTestEngine engine = new DummyTestEngine("dummy");
		engine.addTest("firstTest", () -> fail("expected to fail"));
		engine.addTest("secondTest", () -> assumeFalse(true));

		JUnit5 runner = new JUnit5(TestClass.class, createLauncher(engine));

		Description runnerDescription = runner.getDescription();
		assertEquals(createSuiteDescription(TestClass.class), runnerDescription);

		Description engineDescription = getOnlyElement(runnerDescription.getChildren());
		assertEquals(createSuiteDescription("dummy", new TestId("dummy")), engineDescription);

		List<Description> testDescriptions = engineDescription.getChildren();
		assertThat(testDescriptions).hasSize(2);
		assertEquals(testDescription("dummy", "firstTest"), testDescriptions.get(0));
		assertEquals(testDescription("dummy", "secondTest"), testDescriptions.get(1));
	}

	@Test
	void notifiesRunListenerOfTestExecution() throws Exception {
		DummyTestEngine engine = new DummyTestEngine("dummy");
		engine.addTest("failingTest", () -> fail("expected to fail"));
		engine.addTest("succeedingTest", () -> {
		});
		engine.addTest("skippedTest", () -> assumeFalse(true));
		engine.addTest("ignoredTest", () -> fail("never called")).markSkipped("should be skipped");

		RunListener runListener = mock(RunListener.class);

		RunNotifier notifier = new RunNotifier();
		notifier.addListener(runListener);
		new JUnit5(TestClass.class, createLauncher(engine)).run(notifier);

		InOrder inOrder = inOrder(runListener);

		inOrder.verify(runListener).testStarted(testDescription("dummy", "failingTest"));
		inOrder.verify(runListener).testFailure(any());
		inOrder.verify(runListener).testFinished(testDescription("dummy", "failingTest"));

		inOrder.verify(runListener).testStarted(testDescription("dummy", "succeedingTest"));
		inOrder.verify(runListener).testFinished(testDescription("dummy", "succeedingTest"));

		inOrder.verify(runListener).testStarted(testDescription("dummy", "skippedTest"));
		inOrder.verify(runListener).testAssumptionFailure(any());
		inOrder.verify(runListener).testFinished(testDescription("dummy", "skippedTest"));

		inOrder.verify(runListener).testIgnored(testDescription("dummy", "ignoredTest"));

		inOrder.verifyNoMoreInteractions();
	}

	private static Description testDescription(String engineId, String testName) {
		return createTestDescription(engineId, testName, new TestId(engineId + ":" + testName));
	}

	private static class TestClass {
	}

}
