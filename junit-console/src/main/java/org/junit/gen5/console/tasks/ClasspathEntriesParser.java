/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @since 5.0
 */
class ClasspathEntriesParser {

	private static final String PATH_SEPARATOR_PATTERN = Pattern.quote(File.pathSeparator);

	URL[] toURLs(List<String> additionalClasspathEntries) {
		// @formatter:off
		return additionalClasspathEntries.stream()
				.map(entry -> entry.split(PATH_SEPARATOR_PATTERN))
				.flatMap(Arrays::stream)
				.map(this::toURL)
				.toArray(URL[]::new);
		// @formatter:on
	}

	private URL toURL(String value) {
		try {
			return new File(value).toURI().toURL();
		}
		catch (Exception ex) {
			throw new IllegalStateException("Invalid classpath entry: " + value, ex);
		}
	}

}
