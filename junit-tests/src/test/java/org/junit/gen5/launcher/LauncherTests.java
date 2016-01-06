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
import static org.junit.gen5.engine.TestPlanSpecification.*;
import static org.junit.gen5.launcher.LauncherFactory.createLauncher;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.DummyTestEngine;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.testdoubles.TestEngineStub;

public class LauncherTests {

	@Test
	public void discoverEmptyTestPlanWithoutAnyEngines() {
		Launcher launcher = createLauncher();

		TestPlan testPlan = launcher.discover(TestPlanSpecification.build(forUniqueId("foo")));

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).isEmpty();
	}

	@Test
	public void discoverEmptyTestPlanWithEngineWithoutAnyTests() {
		Launcher launcher = createLauncher(new DummyTestEngine());

		TestPlan testPlan = launcher.discover(TestPlanSpecification.build(forUniqueId("foo")));

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).isEmpty();
	}

	@Test
	public void discoverTestPlanForSingleEngineWithASingleTests() {
		DummyTestEngine engine = new DummyTestEngine();
		engine.addTest("test", noOp());

		Launcher launcher = createLauncher(engine);

		TestPlan testPlan = launcher.discover(TestPlanSpecification.build(forUniqueId("test")));

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(1);
		assertThat(testPlan.getChildren(new TestId("myEngine"))).hasSize(1);
	}

	@Test
	public void discoverTestPlanForMultipleEngines() {
		DummyTestEngine firstEngine = new DummyTestEngine();
		firstEngine.addTest("test1", noOp());
		DummyTestEngine secondEngine = new DummyTestEngine();
		secondEngine.addTest("test2", noOp());

		Launcher launcher = createLauncher(firstEngine, secondEngine);

		TestPlan testPlan = launcher.discover(TestPlanSpecification.build(forUniqueId("test1"), forUniqueId("test2")));

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).hasSize(2);
		assertThat(testPlan.getChildren(new TestId("first"))).hasSize(1);
		assertThat(testPlan.getChildren(new TestId("second"))).hasSize(1);
	}

	@Test
	public void discoverPrunesEnginesWithoutTestsFromTestPlan() {
		DummyTestEngine engine = new DummyTestEngine();
		engine.addTest("test", noOp());

		Launcher launcher = createLauncher(engine);

		TestPlanSpecification specification = TestPlanSpecification.build(forUniqueId("test"));
		specification.filterWith(byEngine("doesNotExist"));

		TestPlan testPlan = launcher.discover(specification);

		assertThat(testPlan.getRoots()).hasSize(1);
		TestIdentifier rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueId())).isEmpty();
	}

	private static Runnable noOp() {
		return () -> {
		};
	}
}
