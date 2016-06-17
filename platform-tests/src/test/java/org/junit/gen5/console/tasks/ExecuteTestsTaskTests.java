/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertSame;
import static org.junit.gen5.api.Assertions.fail;
import static org.junit.gen5.launcher.main.LauncherFactoryForTestingPurposesOnly.createLauncher;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;

/**
 * @since 5.0
 */
public class ExecuteTestsTaskTests {

	@Test
	public void executeWithoutExitCode() throws Exception {
		StringWriter stringWriter = new StringWriter();

		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("succeedingTest", success());
		dummyTestEngine.addTest("failingTest", () -> fail("should fail"));

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
		dummyTestEngine.addTest("succeedingTest", success());
		dummyTestEngine.addTest("failingTest", () -> fail("should fail"));

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		int exitCode = task.execute(new PrintWriter(new StringWriter()));

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
		dummyTestEngine.addTest("failingTest", () -> {
			assertSame(oldClassLoader, ReflectionUtils.getDefaultClassLoader(), "should fail");
		});

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
		dummyTestEngine.addTest("failingTest", () -> fail("should fail"));

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		task.execute(new PrintWriter(stringWriter));

		// @formatter:off
		assertThat(stringWriter.toString())
				.doesNotContain("Test started")
				.contains("Test failures (1)", "failingTest");
		// @formatter:on
	}

	private static Runnable success() {
		return () -> {
		};
	}

}
