/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.tests.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.commons.io.output.TeeOutputStream;
import org.codehaus.groovy.runtime.ProcessGroovyMethods;

public class ProcessStarter {

	private Path executable;
	private Path workingDir;
	private final List<String> arguments = new ArrayList<>();
	private final Map<String, String> environment = new LinkedHashMap<>();

	public ProcessStarter executable(Path executable) {
		this.executable = executable;
		return this;
	}

	public ProcessStarter workingDir(Path workingDir) {
		this.workingDir = workingDir;
		return this;
	}

	public ProcessStarter addArguments(String... arguments) {
		this.arguments.addAll(List.of(arguments));
		return this;
	}

	public ProcessStarter putEnvironment(String key, Path value) {
		return putEnvironment(key, value.toAbsolutePath().toString());
	}

	public ProcessStarter putEnvironment(String key, String value) {
		environment.put(key, value);
		return this;
	}

	public ProcessStarter putEnvironment(Map<String, String> values) {
		environment.putAll(values);
		return this;
	}

	public ProcessResult startAndWait() throws InterruptedException {
		return start().waitFor();
	}

	public WatchedProcess start() {
		var command = Stream.concat(Stream.of(executable.toString()), arguments.stream()).toList();
		try {
			var builder = new ProcessBuilder().command(command);
			if (workingDir != null) {
				builder.directory(workingDir.toFile());
			}
			builder.environment().putAll(environment);
			var process = builder.start();
			var out = forwardAndCaptureOutput(process, System.out, ProcessGroovyMethods::consumeProcessOutputStream);
			var err = forwardAndCaptureOutput(process, System.err, ProcessGroovyMethods::consumeProcessErrorStream);
			return new WatchedProcess(process, out, err);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to start process: " + command, e);
		}
	}

	private static WatchedOutput forwardAndCaptureOutput(Process process, PrintStream delegate,
			BiFunction<Process, OutputStream, Thread> captureAction) {
		var capturingStream = new ByteArrayOutputStream();
		var thread = captureAction.apply(process, new TeeOutputStream(delegate, capturingStream));
		return new WatchedOutput(thread, capturingStream);
	}

}
