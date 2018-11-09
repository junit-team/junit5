/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

	public static void deletePath(Path path) throws IOException {
		Map<Path, IOException> failures = deleteAllFilesAndDirectories(path);
		if (failures.isEmpty()) {
			return;
		}
		throw createIOExceptionWithAttachedFailures(path, failures);
	}

	private static Map<Path, IOException> deleteAllFilesAndDirectories(Path path) {
		// trivial case: delete empty directory right away
		try {
			if (Files.deleteIfExists(path)) {
				return Collections.emptyMap();
			}
		}
		catch (IOException ignored) {
			// fall-through
		}

		// default case: walk the tree...
		Map<Path, IOException> failures = new TreeMap<>();
		try (Stream<Path> stream = Files.walk(path)) {
			Stream<Path> selected = stream.sorted((p, q) -> -p.compareTo(q));
			for (Path item : selected.collect(Collectors.toList())) {
				Files.deleteIfExists(item);
			}
		}
		catch (IOException e) {
			failures.put(path, e);
		}

		return failures;
	}

	private static IOException createIOExceptionWithAttachedFailures(Path path, Map<Path, IOException> failures) {

		String joinedPaths = failures.keySet().stream() //
				.peek(FileUtils::tryToDeleteOnExit) //
				.map(other -> FileUtils.relativizeSafely(path, other)) //
				.map(String::valueOf) //
				.collect(joining(", "));

		IOException exception = new IOException("Failed to delete temp directory " + path.toAbsolutePath()
				+ ". The following paths could not be deleted (see suppressed exceptions for details): " + joinedPaths);
		failures.values().forEach(exception::addSuppressed);
		return exception;
	}

	private static void tryToDeleteOnExit(Path path) {
		try {
			path.toFile().deleteOnExit();
		}
		catch (UnsupportedOperationException ignore) {
		}
	}

	private static Path relativizeSafely(Path path, Path other) {
		try {
			return path.relativize(other);
		}
		catch (IllegalArgumentException e) {
			return other;
		}
	}
}
