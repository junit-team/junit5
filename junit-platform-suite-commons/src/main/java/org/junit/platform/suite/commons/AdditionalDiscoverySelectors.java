/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.commons;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathResourceSelector;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.discovery.FilePosition;
import org.junit.platform.engine.discovery.FileSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UriSelector;

/**
 * @since 1.8
 */
class AdditionalDiscoverySelectors {

	static List<UriSelector> selectUris(String... uris) {
		Preconditions.notNull(uris, "URI list must not be null");
		Preconditions.containsNoNullElements(uris, "Individual URIs must not be null");

		// @formatter:off
		return uniqueStreamOf(uris)
				.filter(StringUtils::isNotBlank)
				.map(DiscoverySelectors::selectUri)
				.collect(Collectors.toList());
		// @formatter:on
	}

	static List<DirectorySelector> selectDirectories(String... paths) {
		Preconditions.notNull(paths, "Directory paths must not be null");
		Preconditions.containsNoNullElements(paths, "Individual directory paths must not be null");

		// @formatter:off
		return uniqueStreamOf(paths)
				.filter(StringUtils::isNotBlank)
				.map(DiscoverySelectors::selectDirectory)
				.collect(Collectors.toList());
		// @formatter:on
	}

	static List<PackageSelector> selectPackages(String... packageNames) {
		Preconditions.notNull(packageNames, "Package names must not be null");
		Preconditions.containsNoNullElements(packageNames, "Individual package names must not be null");

		// @formatter:off
		return uniqueStreamOf(packageNames)
				.map(DiscoverySelectors::selectPackage)
				.collect(Collectors.toList());
		// @formatter:on
	}

	static Stream<ClassSelector> selectClasses(Class<?>... classes) {
		Preconditions.notNull(classes, "classes must not be null");
		Preconditions.containsNoNullElements(classes, "Individual classes must not be null");

		return uniqueStreamOf(classes).map(DiscoverySelectors::selectClass);
	}

	static Stream<ClassSelector> selectClasses(String... classNames) {
		Preconditions.notNull(classNames, "classNames must not be null");
		Preconditions.containsNoNullElements(classNames, "Individual class names must not be null");

		return uniqueStreamOf(classNames).map(DiscoverySelectors::selectClass);
	}

	static List<ModuleSelector> selectModules(String... moduleNames) {
		Preconditions.notNull(moduleNames, "Module names must not be null");
		Preconditions.containsNoNullElements(moduleNames, "Individual module names must not be null");

		return DiscoverySelectors.selectModules(uniqueStreamOf(moduleNames).collect(Collectors.toSet()));
	}

	static FileSelector selectFile(String path, int line, int column) {
		Preconditions.notBlank(path, "File path must not be null or blank");

		if (line <= 0) {
			return DiscoverySelectors.selectFile(path);
		}
		if (column <= 0) {
			return DiscoverySelectors.selectFile(path, FilePosition.from(line));
		}
		return DiscoverySelectors.selectFile(path, FilePosition.from(line, column));
	}

	static ClasspathResourceSelector selectClasspathResource(String classpathResourceName, int line, int column) {
		Preconditions.notBlank(classpathResourceName, "Classpath resource name must not be null or blank");

		if (line <= 0) {
			return DiscoverySelectors.selectClasspathResource(classpathResourceName);
		}
		if (column <= 0) {
			return DiscoverySelectors.selectClasspathResource(classpathResourceName, FilePosition.from(line));
		}
		return DiscoverySelectors.selectClasspathResource(classpathResourceName, FilePosition.from(line, column));
	}

	private static <T> Stream<T> uniqueStreamOf(T[] elements) {
		return Arrays.stream(elements).distinct();
	}

}
