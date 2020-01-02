/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.StringUtils.isBlank;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import org.junit.platform.console.options.CommandLineOptionsParser;
import org.junit.platform.console.options.PicocliCommandLineOptionsParser;

/**
 * @since 1.0
 */
class ConsoleLauncherWrapper {

	private final StringWriter out = new StringWriter();
	private final StringWriter err = new StringWriter();
	private final ConsoleLauncher consoleLauncher;

	ConsoleLauncherWrapper() {
		this(new PicocliCommandLineOptionsParser());
	}

	private ConsoleLauncherWrapper(CommandLineOptionsParser parser) {
		PrintWriter outWriter = new PrintWriter(out, false);
		PrintWriter errWriter = new PrintWriter(err, false);
		this.consoleLauncher = new ConsoleLauncher(parser, outWriter, errWriter);

	}

	public ConsoleLauncherWrapperResult execute(String... args) {
		return execute(0, args);
	}

	public ConsoleLauncherWrapperResult execute(int expectedExitCode, String... args) {
		return execute(Optional.of(expectedExitCode), args);
	}

	public ConsoleLauncherWrapperResult execute(Optional<Integer> expectedCode, String... args) {
		ConsoleLauncherExecutionResult result = consoleLauncher.execute(args);
		int code = result.getExitCode();
		String outText = out.toString();
		String errText = err.toString();
		if (expectedCode.isPresent()) {
			int expectedValue = expectedCode.get();
			assertAll("wrapped execution failed:\n" + outText + "\n", //
				() -> assertEquals(expectedValue, code, "ConsoleLauncher execute code mismatch!"), //
				() -> assertTrue(expectedValue == 0 ? isBlank(errText) : isNotBlank(errText)) //
			);
		}
		return new ConsoleLauncherWrapperResult(args, outText, errText, result);
	}

}
