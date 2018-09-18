/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.paypal.digraph.parser.GraphEdge;
import com.paypal.digraph.parser.GraphNode;
import com.paypal.digraph.parser.GraphParser;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.jdk.Jdeps;

import org.apache.commons.io.FileUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedAcyclicGraph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import platform.tooling.support.Helper;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class JdepsTests {

	private static Path WORKSPACE = Request.WORKSPACE.resolve("jdeps-dot-files");

	private static Set<String> KNOWN_BAD_EDGES = Set.of(
		"org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils -> org.junit.jupiter.engine.Constants",
		"org.junit.jupiter.engine.discovery.JavaElementsResolver -> org.junit.jupiter.engine.JupiterTestEngine",
		"org.junit.jupiter.engine.execution.ConditionEvaluator -> org.junit.jupiter.engine.Constants",
		"org.junit.jupiter.engine.extension.ExtensionRegistry -> org.junit.jupiter.engine.Constants");

	private static Set<String> IGNORE_TARGET_STARTING_WITH = Set.of( //
		"java.", //
		"org.apiguardian.", //
		"org.junit.jupiter.params.shadow.com.univocity.parsers");

	@BeforeAll
	static void setup() {
		FileUtils.deleteQuietly(WORKSPACE.toFile());
		Helper.loadModuleDirectoryNames().stream().parallel().forEach(JdepsTests::createDotFiles);
	}

	@ParameterizedTest
	@MethodSource("platform.tooling.support.Helper#loadModuleDirectoryNames")
	void modules(String module, TestReporter reporter) {
		var parser = parseGraph(module);
		reporter.publishEntry(String.format("Parsed %d edges from modules: %s", parser.getEdges().size(), module));

		assertGraphIsAcyclic(Set.of(parser), "org." + module.replace('-', '.'));
	}

	@Test
	void all(TestReporter reporter) {
		var modules = Helper.loadModuleDirectoryNames();
		var parsers = new ArrayList<GraphParser>();
		for (var module : modules) {
			parsers.add(parseGraph(module));
		}
		var edges = parsers.stream().mapToInt(g -> g.getEdges().size()).sum();
		reporter.publishEntry(String.format("Parsed %d edges from %d modules.", edges, modules.size()));

		assertGraphIsAcyclic(parsers, "org.junit");
	}

	private static void createDotFiles(String module) {
		var jar = Helper.createJarFile(module);

		var builder = Configuration.builder();
		if (jar.isMultiRelease()) {
			builder.addArgument("--multi-release");
			builder.addArgument(Runtime.version().feature());
		}

		var destination = WORKSPACE.resolve(module);
		var configuration = builder //
				.addArgument("--dot-output").addArgument(destination) // Specifies the destination directory for DOT file output.
				.addArgument("-verbose:class") // Prints class-level dependencies.
				.addArgument("-filter:none") // No "-filter:package" and no "-filter:archive" filtering.
				.addArgument(jar.getName()) // JAR file to analyze.
				.build();

		var result = new Jdeps().run(configuration);

		assertEquals(0, result.getExitCode(), "result = " + result);
		assertEquals("", result.getOutput("out"), "output log isn't empty");
		assertEquals("", result.getOutput("err"), "error log isn't empty");

		var summary = destination.resolve("summary.dot");
		var archive = module + '-' + Helper.version(module) + ".jar";
		var dot = destination.resolve(archive + ".dot");
		var raw = destination.resolve(archive + ".raw.dot");

		assertTrue(Files.exists(summary), summary + " doesn't exist");
		assertTrue(Files.exists(dot), dot + " doesn't exist");
		assertTrue(Files.notExists(raw), raw + " does already exist");

		try {
			var lines = Files.readAllLines(dot) //
					.stream() //
					.map(line -> line.replaceAll(" \\(.+\\)", "")) //
					.collect(Collectors.toList());
			Files.write(raw, lines);
		}
		catch (Exception e) {
			fail("generating raw dot file failed", e);
		}
	}

	private static GraphParser parseGraph(String module) {
		var archive = module + '-' + Helper.version(module) + ".jar";
		var destination = WORKSPACE.resolve(module);
		var raw = destination.resolve(archive + ".raw.dot");

		try (var stream = new FileInputStream(raw.toFile())) {
			return new GraphParser(stream);
		}
		catch (Exception e) {
			throw new AssertionError("parsing graph failed", e);
		}
	}

	private static void assertGraphIsAcyclic(Collection<GraphParser> graphParsers, String expectedPackagePrefix) {
		var directedAcyclicGraph = new DirectedAcyclicGraph<String, DefaultEdge>(DefaultEdge.class);
		var parsedEdges = new ArrayList<GraphEdge>();
		for (var parser : graphParsers) {
			parsedEdges.addAll(parser.getEdges().values());
		}
		var badEdges = new ArrayList<String>();

		for (GraphEdge edge : parsedEdges) {

			// cleanup raw names
			var source = classNameOf(edge.getNode1());
			var target = classNameOf(edge.getNode2());

			// inspecting only "org.junit" artifacts
			assertTrue(source.startsWith(expectedPackagePrefix), source);

			// ignore some target packages
			if (ignorePackage(target)) {
				continue;
			}

			// extract package names
			var fromPackage = packageNameOf(source);
			var toPackage = packageNameOf(target);

			// refs to same package are always okay
			if (fromPackage.equals(toPackage)) {
				continue;
			}

			directedAcyclicGraph.addVertex(fromPackage);
			directedAcyclicGraph.addVertex(toPackage);

			try {
				directedAcyclicGraph.addEdge(fromPackage, toPackage);
			}
			catch (IllegalArgumentException e) {
				var badEdge = source + " -> " + target;
				// System.out.println("bad edge: " + badEdge);
				if (!KNOWN_BAD_EDGES.contains(badEdge)) {
					badEdges.add(badEdge);
				}
			}
		}

		assertTrue(badEdges.isEmpty(), badEdges.size() + " bad edge(s) found, expected 0.");
	}

	private static String classNameOf(GraphNode node) {
		var name = node.getId();
		// strip `"` from names
		name = name.replaceAll("\"", "");
		// remove leading artifacts, like "9/" from a multi-release jar
		var indexOfSlash = name.indexOf('/');
		if (indexOfSlash >= 0) {
			name = name.substring(indexOfSlash + 1);
		}
		return name;
	}

	private static String packageNameOf(String className) {
		var indexOfLastDot = className.lastIndexOf('.');
		if (indexOfLastDot < 0) {
			return "";
		}
		return className.substring(0, indexOfLastDot);
	}

	private static boolean ignorePackage(String className) {
		for (var prefix : IGNORE_TARGET_STARTING_WITH) {
			if (className.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
}
