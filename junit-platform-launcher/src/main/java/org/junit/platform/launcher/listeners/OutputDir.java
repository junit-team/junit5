/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.launcher.LauncherConstants.OUTPUT_DIR_UNIQUE_NUMBER_PLACEHOLDER;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.StringUtils;

@API(status = INTERNAL, since = "1.9")
public class OutputDir {

	private static final Pattern OUTPUT_DIR_UNIQUE_NUMBER_PLACEHOLDER_PATTERN = Pattern.compile(
		Pattern.quote(OUTPUT_DIR_UNIQUE_NUMBER_PLACEHOLDER));

	public static OutputDir create(Optional<String> customDir) {
		return create(customDir, () -> Path.of("."));
	}

	static OutputDir create(Optional<String> customDir, Supplier<Path> currentWorkingDir) {
		try {
			return createSafely(customDir, currentWorkingDir);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to create output dir", e);
		}
	}

	/**
	 * Package private for testing purposes.
	 */
	static OutputDir createSafely(Optional<String> customDir, Supplier<Path> currentWorkingDir) throws IOException {
		return createSafely(customDir, currentWorkingDir, new SecureRandom());
	}

	private static OutputDir createSafely(Optional<String> customDir, Supplier<Path> currentWorkingDir,
			SecureRandom random) throws IOException {
		Path cwd = currentWorkingDir.get().toAbsolutePath();
		Path outputDir;

		if (customDir.isPresent() && StringUtils.isNotBlank(customDir.get())) {
			String customPath = customDir.get();
			while (customPath.contains(OUTPUT_DIR_UNIQUE_NUMBER_PLACEHOLDER)) {
				customPath = OUTPUT_DIR_UNIQUE_NUMBER_PLACEHOLDER_PATTERN.matcher(customPath) //
						.replaceFirst(String.valueOf(Math.abs(random.nextLong())));
			}
			outputDir = cwd.resolve(customPath);
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

		return new OutputDir(outputDir.normalize(), random);
	}

	private final Path path;
	private final SecureRandom random;

	private OutputDir(Path path, SecureRandom random) {
		this.path = path;
		this.random = random;
	}

	public Path toPath() {
		return path;
	}

	public Path createFile(String prefix, String extension) throws UncheckedIOException {
		String filename = "%s-%d.%s".formatted(prefix, Math.abs(random.nextLong()), extension);
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
