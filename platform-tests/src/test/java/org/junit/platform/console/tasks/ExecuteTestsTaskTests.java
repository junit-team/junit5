/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly.createLauncher;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.engine.support.hierarchical.DummyTestEngine;

/**
 * @since 1.0
 */
public class ExecuteTestsTaskTests {
	private static final Runnable FAILING_TEST = () -> fail("should fail");
	private static final Runnable SUCCEEDING_TEST = () -> {
	};

	@Test
	public void executeWithoutExitCode() throws Exception {
		StringWriter stringWriter = new StringWriter();

		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("succeedingTest", SUCCEEDING_TEST);
		dummyTestEngine.addTest("failingTest", FAILING_TEST);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		int exitCode = task.execute(new PrintWriter(stringWriter));

		assertEquals(0, exitCode);
		assertThat(stringWriter.toString()).contains("1 tests successful", "1 tests failed");
	}

	@Test
	public void executeWithExitCode() throws Exception {
		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);
		options.setExitCodeEnabled(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("succeedingTest", SUCCEEDING_TEST);
		dummyTestEngine.addTest("failingTest", FAILING_TEST);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		int exitCode = task.execute(dummyWriter());

		assertEquals(1, exitCode);
	}

	@Test
	public void executeWithCustomClassLoader() throws Exception {
		StringWriter stringWriter = new StringWriter();
		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);
		options.setAdditionalClasspathEntries(singletonList("."));

		ClassLoader oldClassLoader = ReflectionUtils.getDefaultClassLoader();
		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("failingTest",
			() -> assertSame(oldClassLoader, ReflectionUtils.getDefaultClassLoader(), "should fail"));

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		task.execute(new PrintWriter(stringWriter));

		assertThat(stringWriter.toString()).contains("failingTest", "should fail", "1 tests failed");
	}

	@Test
	public void executeWithHiddenDetails() throws Exception {
		StringWriter stringWriter = new StringWriter();

		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);
		options.setHideDetails(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("failingTest", FAILING_TEST);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		task.execute(new PrintWriter(stringWriter));

		// @formatter:off
		assertThat(stringWriter.toString())
				.doesNotContain("Test started")
				.contains("Test failures (1)", "failingTest");
		// @formatter:on
	}

	private PrintWriter dummyWriter() {
		return new PrintWriter(new StringWriter());
	}
}
