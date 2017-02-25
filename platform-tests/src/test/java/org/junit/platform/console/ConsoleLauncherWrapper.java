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

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.platform.commons.meta.API;
import org.junit.platform.console.options.CommandLineOptionsParser;
import org.junit.platform.console.options.JOptSimpleCommandLineOptionsParser;
import org.junit.platform.console.tasks.ConsoleTaskExecutor;

/**
 * @since 1.0
 */
@API(Internal)
public class ConsoleLauncherWrapper {

	private final Charset charset;
	private final ByteArrayOutputStream out = new ByteArrayOutputStream();
	private final ByteArrayOutputStream err = new ByteArrayOutputStream();
	private final ConsoleLauncher consoleLauncher;

	public ConsoleLauncherWrapper() {
		this(StandardCharsets.UTF_8);
	}

	public ConsoleLauncherWrapper(Charset charset) {
		this(charset, new JOptSimpleCommandLineOptionsParser());
	}

	public ConsoleLauncherWrapper(Charset charset, CommandLineOptionsParser parser) {
		this.charset = charset;
		try {
			PrintStream streamOut = new PrintStream(out, false, charset.name());
			PrintStream streamErr = new PrintStream(err, false, charset.name());
			this.consoleLauncher = new ConsoleLauncher(parser, new ConsoleTaskExecutor(streamOut, streamErr));
		}
		catch (UnsupportedEncodingException exception) {
			throw new AssertionError("Charset instance created but unsupported?!", exception);
		}
	}

	public ConsoleLauncherWrapperResult execute(String... args) {
		int code = consoleLauncher.execute(args);
		String outText = new String(out.toByteArray(), charset);
		String errText = new String(err.toByteArray(), charset);
		return new ConsoleLauncherWrapperResult(args, charset, code, outText, errText);
	}

}
