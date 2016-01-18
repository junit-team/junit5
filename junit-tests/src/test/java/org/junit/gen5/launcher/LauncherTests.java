/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.DiscoveryRequestBuilder.request;
import static org.junit.gen5.launcher.EngineIdFilter.byEngineId;
import static org.junit.gen5.launcher.LauncherFactory.createLauncher;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;

public class LauncherTests {

	@Test
	public void discoverEmptyTestPlanWithoutAnyEngines() {
		Launcher launcher = createLauncher();

		TestPlan testPlan = launcher.discover(request().select(forUniqueId("foo")).build());

		assertThat(testPlan.getRoots()).isEmpty();
	}

	@Test
	public void discoverEmptyTestPlanWithEngineWithoutAnyTests() {
		Launcher launcher = createLauncher(new DummyTestEngine());

		TestPlan testPlan = launcher.discover(request().select(forUniqueId("foo")).build());

		assertThat(testPlan.getRoots()).isEmpty();
	}

	@Test
	public void discoverTestPlanForSingleEngineWithASingleTests() {
		DummyTestEngine engine = new DummyTestEngine("myEngine");
		TestDescriptor testDescriptor = engine.addTest("test", noOp());

		Launcher launcher = createLauncher(engine);

		TestPlan testPlan = launcher.discover(request().select(forUniqueId(testDescriptor.getUniqueId())).build());

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		assertThat(testPlan.getChildren(new TestId("myEngine"))).hasSize(1);
	}

	@Test
	public void discoverTestPlanForMultipleEngines() {
		DummyTestEngine firstEngine = new DummyTestEngine("engine1");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp());
		DummyTestEngine secondEngine = new DummyTestEngine("engine2");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp());

		Launcher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(
			request().select(forUniqueId(test1.getUniqueId()), forUniqueId(test2.getUniqueId())).build());

		assertThat(testPlan.getRoots()).hasSize(2);
		assertThat(testPlan.getChildren(new TestId("engine1"))).hasSize(1);
		assertThat(testPlan.getChildren(new TestId("engine2"))).hasSize(1);
	}

	@Test
	public void launcherWillNotCallEnginesThatAreFilterByAnEngineIdFilter() {
		DummyTestEngine firstEngine = new DummyTestEngine("first");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp());
		DummyTestEngine secondEngine = new DummyTestEngine("second");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp());

		Launcher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(
			request().select(forUniqueId(test1.getUniqueId()), forUniqueId(test2.getUniqueId())).filter(
				byEngineId("first")).build());

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		assertThat(testPlan.getChildren(new TestId("first"))).hasSize(1);
	}

	private static Runnable noOp() {
		return () -> {
		};
	}
}
