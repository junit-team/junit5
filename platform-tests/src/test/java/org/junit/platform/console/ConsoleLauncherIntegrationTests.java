/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;
import org.junit.platform.console.options.JOptSimpleCommandLineOptionsParser;
import org.junit.platform.console.tasks.ConsoleTaskExecutor;

/**
 * @since 1.0
 */
public class ConsoleLauncherIntegrationTests {

	private ByteArrayOutputStream out = new ByteArrayOutputStream();
	private ByteArrayOutputStream err = new ByteArrayOutputStream();

	private ConsoleLauncher consoleLauncher = new ConsoleLauncher(new JOptSimpleCommandLineOptionsParser(),
		new ConsoleTaskExecutor(new PrintStream(out), new PrintStream(err)));

	@Test
	public void runningConsoleLauncherWithoutExcludeClassnameOptionDoesNotExcludeClasses() {
		String[] args = { "-e", "junit-jupiter", "-p", "org.junit.platform.console.subpackage" };
		String standardOutText = this.executeLauncherAndFetchStandardOut(args);
		assertThat(standardOutText).contains("2 tests found ");
	}

	@Test
	public void runningConsoleLauncherWithExcludeClassnameOptionExcludesClasses() {
		String[] args = { "-e", "junit-jupiter", "-p", "org.junit.platform.console.subpackage", "--exclude-classname",
				"^org\\.junit\\.platform\\.console\\.subpackage\\..*" };
		String standardOutText = this.executeLauncherAndFetchStandardOut(args);
		assertThat(standardOutText).contains("0 tests found ");
	}

	private String executeLauncherAndFetchStandardOut(String[] args) {
		int exitCode = this.consoleLauncher.execute(args);
		assertEquals(0, exitCode);

		String standardErr = new String(this.err.toByteArray());
		assertTrue(standardErr.isEmpty());

		String standardOutText = new String(this.out.toByteArray());
		assertThat(standardOutText).isNotBlank();
		return standardOutText;
	}

}
