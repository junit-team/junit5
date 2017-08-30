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
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
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

	private static final Logger logger = Logger.getLogger(ApiReportGenerator.class.getName());

	private static final String MARKDOWN_FORMAT = "%-52s | %-42s | %-12s | %-27s%n";

	private static final String EOL = System.lineSeparator();

	public static void main(String... args) {
		ApiReportGenerator reportGenerator = new ApiReportGenerator();

		// scan all types below "org.junit" package
		reportGenerator.scanPackages("org.junit");

		reportGenerator.printMarkdownReport(System.out);
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
		this.types.sort(Comparator.comparing(type -> type.getName()));

		logger.fine(() -> {
			StringBuilder builder = new StringBuilder("Listing of all " + this.types.size() + " annotated types:");
			builder.append(EOL);
			this.types.forEach(e -> builder.append(e).append(EOL));
			return builder.toString();
		});

		// ---------------------------------------------------------------------

		// Build usage map
		for (Usage usage : Usage.values()) {
			this.usageMap.put(usage, new ArrayList<>());
		}
		this.types.forEach(type -> this.usageMap.get(type.getAnnotation(API.class).value()).add(type));
	}

	public void printMarkdownReport(PrintStream out) {
		out.println("# `@API` Declarations");
		out.println();
		out.println("Discovered " + this.types.size() + " types with `@API` declarations.");
		this.usageMap.forEach((k, v) -> this.printMarkdown(k, v, out));
	}

	private void printMarkdown(Usage usage, List<Class<?>> types, PrintStream out) {
		out.printf("%n## `@API(%s)`%n", usage);
		out.printf("%nDiscovered %d `@API(%s)` declarations.%n", types.size(), usage);
		if (types.size() > 0) {
			out.printf("%n");
			out.printf(MARKDOWN_FORMAT, "PACKAGE NAME", "CLASS NAME", "TYPE", "MODIFIERS");
			out.printf(MARKDOWN_FORMAT, dashes(52), dashes(42), dashes(12), dashes(27));
			types.forEach(type -> this.printMarkdown(type, out));
		}
	}

	private void printMarkdown(Class<?> type, PrintStream out) {
		out.printf(MARKDOWN_FORMAT, //
			code(type.getPackage().getName()), //
			code(type.getSimpleName()), //
			code(getKind(type)), //
			code(Modifier.toString(type.getModifiers())) //
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

	private static String code(String element) {
		return "`" + element + "`";
	}

	private static String dashes(int length) {
		return CharBuffer.allocate(length).toString().replace('\0', '-');
	}

}
