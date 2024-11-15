/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

public class LocalMavenRepo implements AutoCloseable {

	private final Path tempDir;

	public LocalMavenRepo() {
		try {
			tempDir = Files.createTempDirectory("local-maven-repo-");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String toCliArgument() {
		return "-Dmaven.repo.local=" + tempDir;
	}

	@Override
	public void close() throws IOException {
		try (var files = Files.walk(tempDir)) {
			files.sorted(Comparator.<Path> naturalOrder().reversed()) //
					.forEach(path -> {
						try {
							Files.delete(path);
						}
						catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
		}
	}
}
