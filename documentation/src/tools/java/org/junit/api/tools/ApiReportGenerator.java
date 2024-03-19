/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.api.tools;

import static java.util.stream.Collectors.toCollection;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.ScanResult;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * @since 1.0
 */
class ApiReportGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApiReportGenerator.class);
	private static final String EOL = System.lineSeparator();

	public static void main(String... args) {

		// CAUTION: The output produced by this method is used to
		//          generate a table in the User Guide.

		var writer = new PrintWriter(System.out, true);
		var reportGenerator = new ApiReportGenerator();

		// scan all types below "org.junit" package
		var apiReport = reportGenerator.generateReport("org.junit");

		// ApiReportWriter reportWriter = new MarkdownApiReportWriter(apiReport);
		ApiReportWriter reportWriter = new AsciidocApiReportWriter(apiReport);
		// ApiReportWriter reportWriter = new HtmlApiReportWriter(apiReport);

		// reportWriter.printReportHeader(writer);

		// Print report for all Usage enum constants
		// reportWriter.printDeclarationInfo(writer, EnumSet.allOf(Status.class));

		// Print report only for a specific Status constant, defaults to EXPERIMENTAL
		var status = Status.EXPERIMENTAL;
		if (args.length == 1) {
			status = Status.valueOf(args[0]);
		}
		reportWriter.printDeclarationInfo(writer, EnumSet.of(status));
	}

	// -------------------------------------------------------------------------

	ApiReport generateReport(String... packages) {
		Map<Status, List<Declaration>> declarations = new EnumMap<>(Status.class);
		for (var status : Status.values()) {
			declarations.put(status, new ArrayList<>());
		}

		try (var scanResult = scanClasspath(packages)) {

			var types = collectTypes(scanResult);
			types.stream() //
					.map(Declaration.Type::new) //
					.forEach(type -> declarations.get(type.status()).add(type));

			collectMethods(scanResult) //
					.map(Declaration.Method::new) //
					.filter(method -> !declarations.get(method.status()) //
							.contains(new Declaration.Type(method.classInfo()))) //
					.forEach(method -> {
						types.add(method.classInfo());
						declarations.get(method.status()).add(method);
					});

			declarations.values().forEach(list -> list.sort(null));

			return new ApiReport(types, declarations);
		}
	}

	private static ScanResult scanClasspath(String[] packages) {
		var classGraph = new ClassGraph() //
				.acceptPackages(packages) //
				.disableNestedJarScanning() //
				.enableClassInfo() //
				.enableMethodInfo() //
				.enableAnnotationInfo(); //
		var apiClasspath = System.getProperty("api.classpath");
		if (apiClasspath != null) {
			classGraph = classGraph.overrideClasspath(apiClasspath);
		}
		return classGraph.scan();
	}

	private static SortedSet<ClassInfo> collectTypes(ScanResult scanResult) {
		var types = scanResult.getClassesWithAnnotation(API.class).stream() //
				.filter(it -> !it.getAnnotationInfo(API.class).isInherited()) //
				.collect(toCollection(TreeSet::new));

		LOGGER.debug(() -> {
			var builder = new StringBuilder("Listing of all " + types.size() + " annotated types:");
			builder.append(EOL);
			types.forEach(e -> builder.append(e.getName()).append(EOL));
			return builder.toString();
		});

		return types;
	}

	private static Stream<MethodInfo> collectMethods(ScanResult scanResult) {
		return scanResult.getClassesWithMethodAnnotation(API.class).stream() //
				.flatMap(type -> type.getDeclaredMethodAndConstructorInfo().stream()) //
				.filter(m -> m.getAnnotationInfo(API.class) != null);
	}

}
