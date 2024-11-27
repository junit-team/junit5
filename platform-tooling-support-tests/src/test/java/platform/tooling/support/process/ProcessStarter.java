/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.process;

import static org.codehaus.groovy.runtime.ProcessGroovyMethods.consumeProcessErrorStream;
import static org.codehaus.groovy.runtime.ProcessGroovyMethods.consumeProcessOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.output.TeeOutputStream;

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
		var command = Stream.concat(Stream.of(executable.toAbsolutePath().toString()), arguments.stream()).toList();
		try {
			var builder = new ProcessBuilder().command(command);
			if (workingDir != null) {
				builder.directory(workingDir.toFile());
			}
			builder.environment().putAll(environment);
			var start = Instant.now();
			var out = new ByteArrayOutputStream();
			var err = new ByteArrayOutputStream();
			var process = builder.start();
			var outThread = consumeProcessOutputStream(process, new TeeOutputStream(System.out, out));
			var errThread = consumeProcessErrorStream(process, new TeeOutputStream(System.err, err));
			return new WatchedProcess(start, process, new WatchedOutput(outThread, out),
				new WatchedOutput(errThread, err));
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to start process: " + command, e);
		}
	}
}
