/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.api.tools;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.meta.API.Usage;

/**
 * @since 1.0
 */
class ApiReportGenerator {

	public static void main(String... args) {
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

		// Print report only for Experimental Usage constant
		reportWriter.printDeclarationInfo(writer, EnumSet.of(Usage.Experimental));
	}

	// -------------------------------------------------------------------------

	ApiReport generateReport(String... packages) {
		final Logger logger = Logger.getLogger(ApiReportGenerator.class.getName());
		final String EOL = System.lineSeparator();

		// Scan packages
		ScanResult scanResult = new FastClasspathScanner(packages).scan();

		// Collect names
		List<String> names = new ArrayList<>();
		names.addAll(scanResult.getNamesOfClassesWithAnnotation(API.class));
		names.addAll(scanResult.getNamesOfAnnotationsWithMetaAnnotation(API.class));

		logger.fine(() -> {
			StringBuilder builder = new StringBuilder(
				names.size() + " @API declarations (including meta) found in class-path:");
			builder.append(EOL);
			scanResult.getUniqueClasspathElements().forEach(e -> builder.append(e).append(EOL));
			return builder.toString();

		});

		// Collect types
		List<Class<?>> types = scanResult.classNamesToClassRefs(names);
		// only retain directly annotated types
		types.removeIf(c -> !c.isAnnotationPresent(API.class));
		types.sort(Comparator.comparing(type -> type.getName()));

		logger.fine(() -> {
			StringBuilder builder = new StringBuilder("Listing of all " + types.size() + " annotated types:");
			builder.append(EOL);
			types.forEach(e -> builder.append(e).append(EOL));
			return builder.toString();
		});

		// Build map
		Map<Usage, List<Class<?>>> declarationsMap = new EnumMap<>(Usage.class);
		for (Usage usage : Usage.values()) {
			declarationsMap.put(usage, new ArrayList<>());
		}
		types.forEach(type -> declarationsMap.get(type.getAnnotation(API.class).value()).add(type));

		// Create report
		return new ApiReport(types, declarationsMap);
	}

}
