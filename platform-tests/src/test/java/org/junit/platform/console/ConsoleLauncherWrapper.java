/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import org.junit.platform.console.tasks.ConsoleTestExecutor;

/**
 * @since 1.0
 */
class ConsoleLauncherWrapper {

	private final StringWriter out = new StringWriter();
	private final StringWriter err = new StringWriter();
	private final ConsoleLauncher consoleLauncher;

	ConsoleLauncherWrapper() {
		this(ConsoleTestExecutor::new);
	}

	private ConsoleLauncherWrapper(ConsoleTestExecutor.Factory consoleTestExecutorFactory) {
		var outWriter = new PrintWriter(out, false);
		var errWriter = new PrintWriter(err, false);
		this.consoleLauncher = new ConsoleLauncher(consoleTestExecutorFactory, outWriter, errWriter);
	}

	public ConsoleLauncherWrapperResult execute(String... args) {
		return execute(0, args);
	}

	public ConsoleLauncherWrapperResult execute(int expectedExitCode, String... args) {
		return execute(Optional.of(expectedExitCode), args);
	}

	public ConsoleLauncherWrapperResult execute(Optional<Integer> expectedCode, String... args) {
		var result = consoleLauncher.run(args);
		var code = result.getExitCode();
		var outText = out.toString();
		var errText = err.toString();
		if (expectedCode.isPresent()) {
			int expectedValue = expectedCode.get();
			assertEquals(expectedValue, code, "ConsoleLauncher execute code mismatch!");
			if (expectedValue != 0) {
				assertThat(errText).isNotBlank();
			}
		}
		return new ConsoleLauncherWrapperResult(args, outText, errText, result);
	}

}
