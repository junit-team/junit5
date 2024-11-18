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

import static java.util.Collections.unmodifiableSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId.Segment;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;

class HierarchicalOutputDirectoryProvider implements OutputDirectoryProvider {

	private static final Set<Character> FORBIDDEN_CHARS = unmodifiableSet(
		new HashSet<>(Arrays.asList('\0', '/', '\\', ':', '*', '?', '"', '<', '>', '|')));
	private static final char REPLACEMENT = '_';

	private final Supplier<Path> rootDirSupplier;
	private volatile Path rootDir;

	HierarchicalOutputDirectoryProvider(Supplier<Path> rootDirSupplier) {
		this.rootDirSupplier = rootDirSupplier;
	}

	@Override
	public Path createOutputDirectory(TestDescriptor testDescriptor) throws IOException {
		List<Segment> segments = testDescriptor.getUniqueId().getSegments();
		Segment firstSegment = segments.get(0);
		Path relativePath = segments.stream() //
				.skip(1) //
				.map(Segment::getValue) //
				.map(HierarchicalOutputDirectoryProvider::sanitizeName).map(Paths::get) //
				.reduce(Paths.get(firstSegment.getValue()), Path::resolve);
		return Files.createDirectories(getRootDirectory().resolve(relativePath));
	}

	@Override
	public synchronized Path getRootDirectory() {
		if (rootDir == null) {
			rootDir = rootDirSupplier.get();
		}
		return rootDir;
	}

	private static String sanitizeName(String value) {
		StringBuilder result = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			result.append(isForbiddenCharacter(c) ? REPLACEMENT : c);
		}
		return result.toString();
	}

	private static boolean isForbiddenCharacter(char c) {
		return FORBIDDEN_CHARS.contains(c) || Character.isISOControl(c);
	}
}
