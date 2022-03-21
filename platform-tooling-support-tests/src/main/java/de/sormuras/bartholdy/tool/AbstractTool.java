/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package de.sormuras.bartholdy.tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.sormuras.bartholdy.Bartholdy;
import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.Tool;

public abstract class AbstractTool implements Tool {

	@Override
	public Result run(Configuration configuration) {
		var timeout = configuration.getTimeout().toMillis();
		var command = createCommand(configuration);
		var builder = new ProcessBuilder(command);
		var working = configuration.getWorkingDirectory();
		builder.directory(working.toFile());
		builder.environment().put("JAVA_HOME", Bartholdy.currentJdkHome().toString());
		builder.environment().put(getNameOfEnvironmentHomeVariable(), getHome().toString());
		builder.environment().putAll(configuration.getEnvironment());
		try {
			var start = Instant.now();
			var timestamp = start.toString().replace(':', '-');
			var errfile = working.resolve(".bartholdy-err-" + timestamp + ".txt");
			var outfile = working.resolve(".bartholdy-out-" + timestamp + ".txt");
			builder.redirectError(errfile.toFile());
			builder.redirectOutput(outfile.toFile());
			var process = builder.start();
			try {
				var timedOut = false;
				if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
					timedOut = true;
					process.destroy();
					// give process a second to terminate normally
					for (int i = 10; i > 0 && process.isAlive(); i--) {
						Thread.sleep(123);
					}
					// if the process is still alive, kill it
					if (process.isAlive()) {
						process.destroyForcibly();
						for (int i = 10; i > 0 && process.isAlive(); i--) {
							Thread.sleep(1234);
						}
					}
				}
				if (process.isAlive()) {
					throw new RuntimeException("process is still alive: " + process.info());
				}
				var duration = Duration.between(start, Instant.now());
				return Result.builder().setTimedOut(timedOut).setExitCode(process.exitValue()).setDuration(
					duration).setOutput("err", readAllLines(errfile)).setOutput("out", readAllLines(outfile)).build();
			}
			catch (InterruptedException e) {
				throw new RuntimeException("run failed", e);
			}
			finally {
				Files.deleteIfExists(errfile);
				Files.deleteIfExists(outfile);
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("starting process failed", e);
		}
	}

	private List<String> createCommand(Configuration configuration) {
		var program = createProgram(createPathToProgram());
		var command = new ArrayList<String>();
		command.add(program);
		command.addAll(getToolArguments());
		command.addAll(configuration.getArguments());
		var commandLineLength = String.join(" ", command).length();
		if (commandLineLength < 32000) {
			return command;
		}
		//      if (!tool.isArgumentsFileSupported) {
		//        info("large command line (%s) detected, but %s does not support @argument file",
		//            commandLineLength, this);
		//           return strings;
		//      }
		var timestamp = Instant.now().toString().replace("-", "").replace(":", "");
		var prefix = "bartholdy-" + getName() + "-arguments-" + timestamp + "-";
		try {
			var temporaryDirectory = configuration.getTemporaryDirectory();
			var temporaryFile = Files.createTempFile(temporaryDirectory, prefix, ".txt");
			return List.of(program, "@" + Files.write(temporaryFile, configuration.getArguments()));
		}
		catch (IOException e) {
			throw new UncheckedIOException("creating temporary arguments file failed", e);
		}
	}

	public Path getHome() {
		return Path.of(".");
	}

	public String getNameOfEnvironmentHomeVariable() {
		return getClass().getSimpleName().toUpperCase() + "_HOME";
	}

	protected Path createPathToProgram() {
		return getHome().resolve("bin").resolve(getProgram());
	}

	protected String createProgram(Path pathToProgram) {
		return pathToProgram.normalize().toAbsolutePath().toString();
	}

	protected List<String> getToolArguments() {
		return List.of();
	}

	private static List<String> readAllLines(Path path) {
		try {
			return Files.readAllLines(path);
		}
		catch (IOException e) {
			// ignore
		}
		var lines = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
			for (String line; (line = br.readLine()) != null;) {
				lines.add(line);
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException("reading lines failed: " + path, e);
		}
		return lines;
	}
}
