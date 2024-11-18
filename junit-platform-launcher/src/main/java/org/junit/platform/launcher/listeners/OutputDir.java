/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.StringUtils;

@API(status = INTERNAL, since = "1.9")
public class OutputDir {

	private final SecureRandom random = new SecureRandom();

	public static OutputDir create(Optional<String> customDir) {
		return createWithPath(customDir.filter(StringUtils::isNotBlank).map(Paths::get));
	}

	public static OutputDir createWithPath(Optional<Path> customDir) {
		try {
			return createSafely(customDir, () -> Paths.get(".").toAbsolutePath());
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to create output dir", e);
		}
	}

	/**
	 * Package private for testing purposes.
	 */
	static OutputDir createSafely(Optional<Path> customDir, Supplier<Path> currentWorkingDir) throws IOException {
		Path cwd = currentWorkingDir.get();
		Path outputDir;

		if (customDir.isPresent()) {
			outputDir = cwd.resolve(customDir.get());
		}
		else if (Files.exists(cwd.resolve("pom.xml"))) {
			outputDir = cwd.resolve("target");
		}
		else if (containsFilesWithExtensions(cwd, ".gradle", ".gradle.kts")) {
			outputDir = cwd.resolve("build");
		}
		else {
			outputDir = cwd;
		}

		if (!Files.exists(outputDir)) {
			Files.createDirectories(outputDir);
		}

		return new OutputDir(outputDir.normalize());
	}

	private final Path path;

	private OutputDir(Path path) {
		this.path = path;
	}

	public Path toPath() {
		return path;
	}

	public Path createDir(String prefix) throws UncheckedIOException {
		String filename = String.format("%s-%d", prefix, Math.abs(random.nextLong()));
		Path outputFile = path.resolve(filename);

		try {
			return Files.createDirectory(outputFile);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to create output directory: " + outputFile, e);
		}
	}

	public Path createFile(String prefix, String extension) throws UncheckedIOException {
		String filename = String.format("%s-%d.%s", prefix, Math.abs(random.nextLong()), extension);
		Path outputFile = path.resolve(filename);

		try {
			if (Files.exists(outputFile)) {
				Files.delete(outputFile);
			}
			return Files.createFile(outputFile);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to create output file: " + outputFile, e);
		}
	}

	/**
	 * Determine if the supplied directory contains files with any of the
	 * supplied extensions.
	 */
	private static boolean containsFilesWithExtensions(Path dir, String... extensions) throws IOException {
		BiPredicate<Path, BasicFileAttributes> matcher = (path, basicFileAttributes) -> {
			if (basicFileAttributes.isRegularFile()) {
				for (String extension : extensions) {
					if (path.getFileName().toString().endsWith(extension)) {
						return true;
					}
				}
			}
			return false;
		};
		try (Stream<Path> pathStream = Files.find(dir, 1, matcher)) {
			return pathStream.findFirst().isPresent();
		}
	}
}
