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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.scanner.ScanResult;

import org.junit.platform.commons.meta.API;

class PrintUsageOfApiAnnotation {

	private static final String format = "  %-40s %-50s %-20s%n";

	private static void out(API.Usage usage, List<Class<?>> types) {
		System.out.printf("%n### @API(%s) (%d) ###%n", usage, types.size());
		System.out.printf("%n" + format + "%n", "TYPE", "PACKAGE", "KIND / MODIFIERS");
		types.forEach(PrintUsageOfApiAnnotation::out);
		System.out.printf("%n%d types annotated with @API(%s)%n", types.size(), usage);
	}

	private static void out(Class<?> type) {
		System.out.printf(format, //
			type.getSimpleName(), //
			type.getPackage().getName(), //
			getKind(type) + " / " + Modifier.toString(type.getModifiers()) //
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

	public static void main(String[] args) {
		// scan all types below "org.junit" package
		ScanResult scanResult = new FastClasspathScanner("org.junit").scan();

		List<String> names = new ArrayList<>();
		names.addAll(scanResult.getNamesOfClassesWithAnnotation(API.class));
		names.addAll(scanResult.getNamesOfAnnotationsWithMetaAnnotation(API.class));
		System.out.println("\n\n" + names.size() + " @API(.*) occurrences (including meta) found in class-path:\n");
		scanResult.getUniqueClasspathElements().forEach(System.out::println);

		// only retain directly annotated types
		List<Class<?>> types = scanResult.classNamesToClassRefs(names);
		types.removeIf(c -> !c.isAnnotationPresent(API.class));
		types.sort(Comparator.comparing(type -> type.getPackage().getName()));
		System.out.println("\n\nListing of all " + types.size() + " annotated types:\n");
		types.forEach(System.out::println);

		// prepare map report
		Map<API.Usage, List<Class<?>>> map = new EnumMap<>(API.Usage.class);
		for (API.Usage usage : API.Usage.values()) {
			map.put(usage, new ArrayList<>());
		}
		types.forEach(type -> map.get(type.getAnnotation(API.class).value()).add(type));

		map.forEach(PrintUsageOfApiAnnotation::out);
		System.out.println("\n\n" + types.size() + " types with @API(.*) annotation found.");
	}
}
