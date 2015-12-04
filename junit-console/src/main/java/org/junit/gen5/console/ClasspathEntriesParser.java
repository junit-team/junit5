/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

class ClasspathEntriesParser {

	URL[] toURLs(List<String> additionalClasspathEntries) {
		String pathSeparatorPattern = Pattern.quote(File.pathSeparator);
		// @formatter:off
		List<URL> urls = additionalClasspathEntries.stream()
				.map(entry -> entry.split(pathSeparatorPattern))
				.flatMap(Arrays::stream)
				.map(this::toURL)
				.collect(toList());
		// @formatter:on
		return urls.toArray(new URL[urls.size()]);
	}

	private URL toURL(String value) {
		try {
			return new File(value).toURI().toURL();
		}
		catch (MalformedURLException e) {
			throw new RuntimeException("Erroneous classpath entry: " + value, e);
		}
	}

}
