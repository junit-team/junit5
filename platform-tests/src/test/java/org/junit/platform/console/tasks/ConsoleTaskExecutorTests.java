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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0
 */
public class ConsoleTaskExecutorTests {

	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private ByteArrayOutputStream err = new ByteArrayOutputStream();
	private ConsoleTaskExecutor executor = new ConsoleTaskExecutor(new PrintStream(out), new PrintStream(err));

	@Test
	public void executeSuccessfulTask() {
		int exitCode = executor.executeTask(writer -> {
			writer.print("Test");
			return 42;
		}, writer -> {
			fail("should not be called");
		});

		assertEquals(42, exitCode);
		assertEquals("Test", stdOut());
		assertEquals("", stdErr());
	}

	@Test
	public void executeFailingTask() {
		int exitCode = executor.executeTask(writer -> {
			throw new RuntimeException("something went wrong");
		}, writer -> {
			writer.print("Help");
		});

		assertEquals(-1, exitCode);
		assertEquals("Help", stdOut());
		assertThat(stdErr()).startsWith("java.lang.RuntimeException: something went wrong");
	}

	@Test
	public void executeWithExceptionWhilePrintingHelp() {
		int exitCode = executor.executeTask(writer -> {
			throw new RuntimeException("something went wrong");
		}, writer -> {
			throw new RuntimeException("could not print help");
		});

		assertEquals(-1, exitCode);
		// @formatter:off
		assertThat(stdErr())
				.startsWith("java.lang.RuntimeException: something went wrong")
				.contains("Exception occurred while printing help: java.lang.RuntimeException: could not print help");
		// @formatter:on
	}

	private String stdOut() {
		return new String(out.toByteArray());
	}

	private String stdErr() {
		return new String(err.toByteArray());
	}
}
