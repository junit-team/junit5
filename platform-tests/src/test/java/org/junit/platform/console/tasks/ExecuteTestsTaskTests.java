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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.commons.util.ReflectionUtils.getDefaultClassLoader;
import static org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly.createLauncher;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.engine.support.hierarchical.DummyTestEngine;

/**
 * @since 1.0
 */
public class ExecuteTestsTaskTests {

	private static final Runnable FAILING_BLOCK = () -> fail("should fail");
	private static final Runnable SUCCEEDING_TEST = () -> {
	};

	@Test
	public void printsSummary() throws Exception {
		StringWriter stringWriter = new StringWriter();

		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("succeedingTest", SUCCEEDING_TEST);
		dummyTestEngine.addTest("failingTest", FAILING_BLOCK);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		task.execute(new PrintWriter(stringWriter));

		assertThat(stringWriter.toString()).contains("Test run finished after", "2 tests found", "0 tests skipped",
			"2 tests started", "0 tests aborted", "1 tests successful", "1 tests failed");
	}

	@Test
	public void printsDetailsIfTheyAreNotHidden() throws Exception {
		StringWriter stringWriter = new StringWriter();

		CommandLineOptions options = new CommandLineOptions();
		options.setHideDetails(false);
		options.setRunAllTests(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("failingTest", FAILING_BLOCK);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		task.execute(new PrintWriter(stringWriter));

		assertThat(stringWriter.toString()).contains("Test execution started.");
	}

	@Test
	public void printsNoDetailsIfTheyAreHidden() throws Exception {
		StringWriter stringWriter = new StringWriter();

		CommandLineOptions options = new CommandLineOptions();
		options.setHideDetails(true);
		options.setRunAllTests(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("failingTest", FAILING_BLOCK);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		task.execute(new PrintWriter(stringWriter));

		assertThat(stringWriter.toString()).doesNotContain("Test execution started.");
	}

	@Test
	public void printsFailuresEvenIfDetailsAreHidden() throws Exception {
		StringWriter stringWriter = new StringWriter();

		CommandLineOptions options = new CommandLineOptions();
		options.setHideDetails(true);
		options.setRunAllTests(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("failingTest", FAILING_BLOCK);
		dummyTestEngine.addContainer("failingContainer", FAILING_BLOCK);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		task.execute(new PrintWriter(stringWriter));

		assertThat(stringWriter.toString()).contains("Failures (2)", "failingTest", "failingContainer");
	}

	@Test
	public void hasStatusCode0ForSucceedingTest() throws Exception {
		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("succeedingTest", SUCCEEDING_TEST);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		int exitCode = task.execute(dummyWriter());

		assertThat(exitCode).isEqualTo(0);
	}

	@Test
	public void hasStatusCode1ForFailingTest() throws Exception {
		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("failingTest", FAILING_BLOCK);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		int exitCode = task.execute(dummyWriter());

		assertThat(exitCode).isEqualTo(1);
	}

	@Test
	public void hasStatusCode1ForFailingContainer() throws Exception {
		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);

		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addContainer("failingContainer", FAILING_BLOCK);

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		int exitCode = task.execute(dummyWriter());

		assertThat(exitCode).isEqualTo(1);
	}

	@Test
	public void usesCustomClassLoaderIfAdditionalClassPathEntriesArePresent() throws Exception {
		StringWriter stringWriter = new StringWriter();
		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);
		options.setAdditionalClasspathEntries(singletonList("."));

		ClassLoader oldClassLoader = getDefaultClassLoader();
		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("failingTest",
			() -> assertSame(oldClassLoader, getDefaultClassLoader(), "should fail"));

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		task.execute(new PrintWriter(stringWriter));

		assertThat(stringWriter.toString()).contains("failingTest", "should fail", "1 tests failed");
	}

	@Test
	public void usesSameClassLoaderIfNoAdditionalClassPathEntriesArePresent() throws Exception {
		StringWriter stringWriter = new StringWriter();
		CommandLineOptions options = new CommandLineOptions();
		options.setRunAllTests(true);
		options.setAdditionalClasspathEntries(emptyList());

		ClassLoader oldClassLoader = getDefaultClassLoader();
		DummyTestEngine dummyTestEngine = new DummyTestEngine();
		dummyTestEngine.addTest("failingTest",
			() -> assertNotSame(oldClassLoader, getDefaultClassLoader(), "should fail"));

		ExecuteTestsTask task = new ExecuteTestsTask(options, () -> createLauncher(dummyTestEngine));
		task.execute(new PrintWriter(stringWriter));

		assertThat(stringWriter.toString()).contains("failingTest", "should fail", "1 tests failed");
	}

	private PrintWriter dummyWriter() {
		return new PrintWriter(new StringWriter());
	}
}
