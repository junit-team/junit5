/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;

/**
 * Hierarchical {@link OutputDirectoryProvider} that creates directories based on
 * the unique ID segments of a {@link TestDescriptor}.
 *
 * @since 1.12
 */
class HierarchicalOutputDirectoryProvider implements OutputDirectoryProvider {

	private static final Pattern FORBIDDEN_CHARS = Pattern.compile("[^a-z0-9.,_\\-() ]", Pattern.CASE_INSENSITIVE);
	private static final String REPLACEMENT = "_";

	private final Supplier<Path> rootDirSupplier;
	private volatile Path rootDir;

	HierarchicalOutputDirectoryProvider(Supplier<Path> rootDirSupplier) {
		this.rootDirSupplier = rootDirSupplier;
	}

	@Override
	public Path createOutputDirectory(TestDescriptor testDescriptor) throws IOException {
		Preconditions.notNull(testDescriptor, "testDescriptor must not be null");

		List<Segment> segments = testDescriptor.getUniqueId().getSegments();
		Path relativePath = segments.stream() //
				.skip(1) //
				.map(HierarchicalOutputDirectoryProvider::toSanitizedPath) //
				.reduce(toSanitizedPath(segments.get(0)), Path::resolve);
		return Files.createDirectories(getRootDirectory().resolve(relativePath));
	}

	@Override
	public synchronized Path getRootDirectory() {
		if (rootDir == null) {
			rootDir = rootDirSupplier.get();
		}
		return rootDir;
	}

	private static Path toSanitizedPath(Segment segment) {
		return Paths.get(sanitizeName(segment.getValue()));
	}

	private static String sanitizeName(String value) {
		return FORBIDDEN_CHARS.matcher(value).replaceAll(REPLACEMENT);
	}
}
