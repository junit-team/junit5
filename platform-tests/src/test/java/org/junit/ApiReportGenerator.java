/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.meta.API.Usage;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

/**
 * @since 1.0
 */
class ApiReportGenerator {

	private static final String EOL = System.lineSeparator();

	private static final String FORMAT = "  %-50s %-40s %-10s %s%n";

	private static final Logger logger = Logger.getLogger(ApiReportGenerator.class.getName());

	public static void main(String... args) {
		ApiReportGenerator reportGenerator = new ApiReportGenerator();

		// scan all types below "org.junit" package
		reportGenerator.scanPackages("org.junit");

		reportGenerator.printReport(System.out);
	}

	private List<Class<?>> types;

	private final Map<Usage, List<Class<?>>> usageMap = new EnumMap<>(Usage.class);

	public void scanPackages(String... packages) {
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

		this.types = scanResult.classNamesToClassRefs(names);
		// only retain directly annotated types
		this.types.removeIf(c -> !c.isAnnotationPresent(API.class));
		this.types.sort(Comparator.comparing(type -> type.getPackage().getName()));

		logger.fine(() -> {
			StringBuilder builder = new StringBuilder("Listing of all " + this.types.size() + " annotated types:");
			builder.append(EOL);
			this.types.forEach(e -> builder.append(e).append(EOL));
			return builder.toString();
		});

		// ---------------------------------------------------------------------

		// Build usage map
		for (Usage usage : Usage.values()) {
			usageMap.put(usage, new ArrayList<>());
		}
		this.types.forEach(type -> usageMap.get(type.getAnnotation(API.class).value()).add(type));
	}

	public void printReport(PrintStream out) {
		out.println("Discovered " + this.types.size() + " types with @API declarations.");
		usageMap.forEach((k, v) -> this.print(k, v, out));
	}

	private void print(Usage usage, List<Class<?>> types, PrintStream out) {
		out.printf("%n## @API(%s) (%d)%n", usage, types.size());
		if (types.size() > 0) {
			out.printf("%n");
			out.printf(FORMAT, "PACKAGE NAME", "CLASS NAME", "TYPE", " MODIFIERS");
			// TODO Make dashed line lengths dynamic
			out.printf(FORMAT, "--------------------------------------------------",
				"----------------------------------------", "----------", "--------------------------");
			types.forEach(type -> this.print(type, out));
		}
	}

	private void print(Class<?> type, PrintStream out) {
		out.printf(FORMAT, //
			type.getPackage().getName(), //
			type.getSimpleName(), //
			getKind(type), //
			Modifier.toString(type.getModifiers()) //
		);
	}

	private static String getKind(Class<?> type) {
		if (type.isAnnotation()) {
			return "annotation";
		}
		if (type.isEnum()) {
			return "enum";
		}
		if (type.isInterface()) {
			return "interface";
		}
		return "class";
	}

}
