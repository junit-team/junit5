/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package de.sormuras.bartholdy.tool;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import de.sormuras.bartholdy.Bartholdy;

/** Gradle. */
public class Gradle extends AbstractTool {

	public static Gradle install(String version, Path tools) {
		var uri = "https://services.gradle.org/distributions/gradle-" + version + "-bin.zip";
		var home = Bartholdy.install(URI.create(uri), tools);
		return new Gradle(home);
	}

	private final Path home;
	private final String version;
	private final Path executable;

	public Gradle(Path home) {
		this.home = requireNonNull(home);
		if (!Files.isDirectory(home)) {
			throw new IllegalArgumentException("not a directory: " + home);
		}
		if (!Files.isRegularFile(home.resolve(Path.of("bin", "gradle")))) {
			throw new IllegalArgumentException("`bin/gradle` launch script not found in: " + home);
		}
		this.version = "TODO";
		this.executable = getExecutable(home);
	}

	private Path getExecutable(Path home) {
		var win = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
		var name = "gradle" + (win ? ".bat" : "");
		return Bartholdy.setExecutable(home.resolve("bin").resolve(name));
	}

	@Override
	public Path getHome() {
		return home;
	}

	@Override
	protected Path createPathToProgram() {
		return executable;
	}

	@Override
	public String getName() {
		return "gradle";
	}

	@Override
	public String getVersion() {
		return version;
	}
}
