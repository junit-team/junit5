/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.EngineIdFilter.byEngineId;
import static org.junit.gen5.launcher.main.LauncherFactory.createLauncher;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import org.assertj.core.api.Assertions;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;
import org.junit.gen5.launcher.TestId;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

public class JUnit5LauncherTests {

	@Test
	public void discoverEmptyTestPlanWithoutAnyEngines() {
		JUnit5Launcher launcher = createLauncher();

		TestPlan testPlan = launcher.discover(request().select(forUniqueId("foo")).build());

		Assertions.assertThat(testPlan.getRoots()).isEmpty();
	}

	@Test
	public void discoverEmptyTestPlanWithEngineWithoutAnyTests() {
		JUnit5Launcher launcher = createLauncher(new DummyTestEngine());

		TestPlan testPlan = launcher.discover(request().select(forUniqueId("foo")).build());

		Assertions.assertThat(testPlan.getRoots()).isEmpty();
	}

	@Test
	public void discoverTestPlanForSingleEngineWithASingleTests() {
		DummyTestEngine engine = new DummyTestEngine("myEngine");
		TestDescriptor testDescriptor = engine.addTest("test", noOp());

		JUnit5Launcher launcher = createLauncher(engine);

		TestPlan testPlan = launcher.discover(request().select(forUniqueId(testDescriptor.getUniqueId())).build());

		Assertions.assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		Assertions.assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		Assertions.assertThat(testPlan.getChildren(new TestId("myEngine"))).hasSize(1);
	}

	@Test
	public void discoverTestPlanForMultipleEngines() {
		DummyTestEngine firstEngine = new DummyTestEngine("engine1");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp());
		DummyTestEngine secondEngine = new DummyTestEngine("engine2");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp());

		JUnit5Launcher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(
			request().select(forUniqueId(test1.getUniqueId()), forUniqueId(test2.getUniqueId())).build());

		Assertions.assertThat(testPlan.getRoots()).hasSize(2);
		Assertions.assertThat(testPlan.getChildren(new TestId("engine1"))).hasSize(1);
		Assertions.assertThat(testPlan.getChildren(new TestId("engine2"))).hasSize(1);
	}

	@Test
	public void launcherWillNotCallEnginesThatAreFilterByAnEngineIdFilter() {
		DummyTestEngine firstEngine = new DummyTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp());
		DummyTestEngine secondEngine = new DummyTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp());

		JUnit5Launcher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(
			request().select(forUniqueId(test1.getUniqueId()), forUniqueId(test2.getUniqueId())).filter(
				byEngineId("first")).build());

		Assertions.assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		Assertions.assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		Assertions.assertThat(testPlan.getChildren(new TestId("first"))).hasSize(1);
	}

	private static Runnable noOp() {
		return () -> {
		};
	}
}
