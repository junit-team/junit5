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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.Result;
import de.sormuras.bartholdy.Tool;
import de.sormuras.bartholdy.jdk.Jdeps;
import de.sormuras.bartholdy.util.CycleDetectedException;
import de.sormuras.bartholdy.util.DirectedAcyclicGraph;

/** Package cycles detector tool. */
public class CyclesDetector implements Tool {

	private final Path path;
	private final BiPredicate<String, String> exclude;

	public CyclesDetector(Path path) {
		this(path, String::equals);
	}

	public CyclesDetector(Path path, BiPredicate<String, String> exclude) {
		this.path = path;
		this.exclude = exclude;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public String getVersion() {
		return "1.2";
	}

	@Override
	public Result run(Configuration configuration) {
		var result = Result.builder();
		try {
			detectCycles(result, path);
			result.setExitCode(result.getOutputLines("cycles").isEmpty() ? 0 : 1);
		}
		catch (Exception e) {
			result.setExitCode(-1);
			result.setOutput("out", e.toString());
			result.setOutput("err",
				Arrays.stream(e.getStackTrace()).map(Object::toString).collect(Collectors.toList()));
		}
		return result.build();
	}

	private void detectCycles(Result.Builder result, Path path) {
		// prepare and run "jdeps" for the JAR...
		var dependenciesConfiguration = Configuration.builder();
		try (var jar = new JarFile(path.toFile())) {
			if (jar.isMultiRelease()) {
				var version = Runtime.version().feature();
				dependenciesConfiguration.addArgument("--multi-release").addArgument(version);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Opening jar failed: " + e);
		}
		dependenciesConfiguration.addArgument("-verbose:class");
		dependenciesConfiguration.addArgument(path);
		var dependencies = new Jdeps().run(dependenciesConfiguration.build());
		if (dependencies.getExitCode() != 0) {
			throw new RuntimeException("Running jdeps failed: " + dependencies);
		}

		// extract lines with class-level references...
		var lines = dependencies.getOutputLines("out").stream().filter(line -> line.startsWith("   ")).filter(
			line -> line.contains("->")).map(String::trim).collect(Collectors.toList());

		if (lines.isEmpty()) {
			return;
		}

		// parse each line and test against user-defined predicate...
		var items = new ArrayList<Item>();
		for (var line : lines) {
			var item = new Item(line);
			if (exclude.test(item.sourcePackage, item.targetPackage)) {
				continue;
			}
			items.add(item);
		}

		if (items.isEmpty()) {
			return;
		}

		var graph = new DirectedAcyclicGraph();
		var cycles = new ArrayList<String>();
		var edges = new ArrayList<String>();
		for (var item : items) {
			try {
				if (graph.addEdge(item.sourcePackage, item.targetPackage)) {
					edges.add(item.sourcePackage + " -> " + item.targetPackage);
				}
			}
			catch (CycleDetectedException exception) {
				cycles.add(String.format("Adding edge '%s' failed. %s", item, exception.getMessage()));
			}
		}
		result.setOutput("items", items.stream().map(Object::toString).collect(Collectors.toList()));
		result.setOutput("edges", edges);
		result.setOutput("cycles", cycles);
	}

	private static String classNameOf(String raw) {
		raw = raw.trim();
		// strip `"` from names
		raw = raw.replaceAll("\"", "");
		// remove leading artifacts, like "9/" from a multi-release jar
		var indexOfSlash = raw.indexOf('/');
		if (indexOfSlash >= 0) {
			raw = raw.substring(indexOfSlash + 1);
		}
		return raw;
	}

	private static String packageNameOf(String className) {
		var indexOfLastDot = className.lastIndexOf('.');
		if (indexOfLastDot < 0) {
			return "";
		}
		return className.substring(0, indexOfLastDot);
	}

	private static class Item {
		private final String sourceClass;
		private final String targetClass;
		private final String sourcePackage;
		private final String targetPackage;

		Item(String line) {
			var split = line.split("->");
			var target = split[1].trim();
			this.sourceClass = classNameOf(split[0].trim());
			this.targetClass = classNameOf(target.substring(0, target.indexOf(' ')));
			this.sourcePackage = packageNameOf(sourceClass);
			this.targetPackage = packageNameOf(targetClass);
		}

		@Override
		public String toString() {
			return sourceClass + " -> " + targetClass;
		}
	}
}
