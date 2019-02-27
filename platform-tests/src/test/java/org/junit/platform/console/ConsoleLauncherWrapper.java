/*
 * Copyright 2015-2019 the original author or authors.
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.platform.console.options.CommandLineOptionsParser;
import org.junit.platform.console.options.PicocliCommandLineOptionsParser;

/**
 * @since 1.0
 */
class ConsoleLauncherWrapper {

	private final Charset charset;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final ByteArrayOutputStream err = new ByteArrayOutputStream();
	private final ConsoleLauncher consoleLauncher;

	ConsoleLauncherWrapper() {
		this(StandardCharsets.UTF_8);
	}

	private ConsoleLauncherWrapper(Charset charset) {
		this(charset, new PicocliCommandLineOptionsParser());
	}

	private ConsoleLauncherWrapper(Charset charset, CommandLineOptionsParser parser) {
		this.charset = charset;
		try {
			PrintStream streamOut = new PrintStream(out, false, charset.name());
			PrintStream streamErr = new PrintStream(err, false, charset.name());
			this.consoleLauncher = new ConsoleLauncher(parser, streamOut, streamErr, charset);
		}
		catch (UnsupportedEncodingException exception) {
			throw new AssertionError("Charset instance created but unsupported?!", exception);
		}
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
		String outText = new String(out.toByteArray(), charset);
		String errText = new String(err.toByteArray(), charset);
		if (expectedCode.isPresent()) {
			int expectedValue = expectedCode.get();
			assertAll("wrapped execution failed:\n" + outText + "\n", //
				() -> assertEquals(expectedValue, code, "ConsoleLauncher execute code mismatch!"), //
				() -> assertTrue(expectedValue == 0 ? isBlank(errText) : isNotBlank(errText)) //
			);
		}
		return new ConsoleLauncherWrapperResult(args, charset, outText, errText, result);
	}

}
