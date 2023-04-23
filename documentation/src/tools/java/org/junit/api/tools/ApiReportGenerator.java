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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * @since 1.0
 */
class ApiReportGenerator {

	public static void main(String... args) {

		// CAUTION: The output produced by this method is used to
		//          generate a table in the User Guide.

		PrintWriter writer = new PrintWriter(System.out, true);
		ApiReportGenerator reportGenerator = new ApiReportGenerator();

		// scan all types below "org.junit" package
		ApiReport apiReport = reportGenerator.generateReport("org.junit");

		// ApiReportWriter reportWriter = new MarkdownApiReportWriter(apiReport);
		ApiReportWriter reportWriter = new AsciidocApiReportWriter(apiReport);
		// ApiReportWriter reportWriter = new HtmlApiReportWriter(apiReport);

		// reportWriter.printReportHeader(writer);

		// Print report for all Usage enum constants
		// reportWriter.printDeclarationInfo(writer, EnumSet.allOf(Usage.class));

		// Print report only for a specific Status constant, defaults to EXPERIMENTAL
		Status status = Status.EXPERIMENTAL;
		if (args.length == 1) {
			status = Status.valueOf(args[0]);
		}
		reportWriter.printDeclarationInfo(writer, EnumSet.of(status));
	}

	// -------------------------------------------------------------------------

	ApiReport generateReport(String... packages) {
		Logger logger = LoggerFactory.getLogger(ApiReportGenerator.class);
		String EOL = System.lineSeparator();
		ClassGraph classGraph = new ClassGraph() //
				.acceptPackages(packages) //
				.disableNestedJarScanning() //
				.enableAnnotationInfo(); //
		String apiClasspath = System.getProperty("api.classpath");
		if (apiClasspath != null) {
			classGraph = classGraph.overrideClasspath(apiClasspath);
		}

		// Scan packages
		try (ScanResult scanResult = classGraph.scan()) {

			// Collect names
			ClassInfoList classesWithApiAnnotation = scanResult.getClassesWithAnnotation(API.class.getCanonicalName());
			List<String> names = classesWithApiAnnotation.getNames();

			logger.debug(() -> {
				StringBuilder builder = new StringBuilder(
					names.size() + " @API declarations (including meta) found in class-path:");
				builder.append(EOL);
				scanResult.getClasspathURLs().forEach(e -> builder.append(e).append(EOL));
				return builder.toString();
			});

			// Collect types
			List<Class<?>> types = classesWithApiAnnotation.loadClasses();
			// only retain directly annotated types
			types.removeIf(c -> !c.isAnnotationPresent(API.class));
			types.sort(Comparator.comparing(Class::getName));

			logger.debug(() -> {
				StringBuilder builder = new StringBuilder("Listing of all " + types.size() + " annotated types:");
				builder.append(EOL);
				types.forEach(e -> builder.append(e).append(EOL));
				return builder.toString();
			});

			// Build map
			Map<Status, List<Class<?>>> declarationsMap = new EnumMap<>(Status.class);
			for (Status status : Status.values()) {
				declarationsMap.put(status, new ArrayList<>());
			}
			types.forEach(type -> declarationsMap.get(type.getAnnotation(API.class).status()).add(type));

			// Create report
			return new ApiReport(types, declarationsMap);
		}
	}

}
