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

	private static final Logger logger = Logger.getLogger(ApiReportGenerator.class.getName());

	private static final String EOL = System.lineSeparator();

	public static void main(String... args) {
		ApiReportGenerator reportGenerator = new ApiReportGenerator();

		// scan all types below "org.junit" package
		// ApiReport apiReport = reportGenerator.generateMarkdownReport("org.junit");
		ApiReport apiReport = reportGenerator.generateAsciidocReport("org.junit");

		apiReport.printReportHeader(System.out);

		// Print report for all Usage enum constants
		// apiReport.printDeclarationInfo(System.out, EnumSet.allOf(Usage.class));

		// Print report only for Experimental Usage constant
		apiReport.printDeclarationInfo(System.out, EnumSet.of(Usage.Experimental));
	}

	private List<Class<?>> types;

	private Map<Usage, List<Class<?>>> declarationsMap;

	ApiReport generateMarkdownReport(String... packages) {
		generateReportArtifacts(packages);
		return new MarkdownApiReport(this.types, this.declarationsMap);
	}

	ApiReport generateAsciidocReport(String... packages) {
		generateReportArtifacts(packages);
		return new AsciidocApiReport(this.types, this.declarationsMap);
	}

	private void generateReportArtifacts(String... packages) {
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

		// Build map
		this.declarationsMap = new EnumMap<>(Usage.class);
		for (Usage usage : Usage.values()) {
			this.declarationsMap.put(usage, new ArrayList<>());
		}
		this.types.forEach(type -> this.declarationsMap.get(type.getAnnotation(API.class).value()).add(type));
	}

}
