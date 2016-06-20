/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static java.util.stream.Collectors.toCollection;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.platform.commons.JUnitException;

/**
 * @since 1.0
 */
class ClasspathEntriesParser {

	private static final String PATH_SEPARATOR_PATTERN = Pattern.quote(File.pathSeparator);

	URL[] toURLs(List<String> additionalClasspathEntries) {
		return split(additionalClasspathEntries).map(this::toURL).toArray(URL[]::new);
	}

	Set<File> toDirectories(List<String> additionalClasspathEntries) {
		// @formatter:off
		return split(additionalClasspathEntries)
				.filter(File::isDirectory)
				.collect(toCollection(LinkedHashSet::new));
		// @formatter:on
	}

	private Stream<File> split(List<String> additionalClasspathEntries) {
		// @formatter:off
		return additionalClasspathEntries.stream()
				.map(entry -> entry.split(PATH_SEPARATOR_PATTERN))
				.flatMap(Arrays::stream)
				.map(File::new);
		// @formatter:on
	}

	private URL toURL(File file) {
		try {
			return file.toURI().toURL();
		}
		catch (Exception ex) {
			throw new JUnitException("Invalid classpath entry: " + file, ex);
		}
	}

}
