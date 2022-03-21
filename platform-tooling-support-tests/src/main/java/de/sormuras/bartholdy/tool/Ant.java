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

/** Ant. */
public class Ant extends AbstractTool {

	public static Ant install(String version, Path tools) {
		var host = "https://archive.apache.org/dist/";
		var uri = String.format("%s/ant/binaries/apache-ant-%s-bin.zip", host, version);
		var home = Bartholdy.install(URI.create(uri), tools);
		return new Ant(home);
	}

	private final Path home;
	private final String version;
	private final Path executable;

	public Ant(Path home) {
		this.home = requireNonNull(home);
		if (!Files.isDirectory(home)) {
			throw new IllegalArgumentException("not a directory: " + home);
		}
		if (!Files.isRegularFile(home.resolve(Path.of("bin", "ant")))) {
			throw new IllegalArgumentException("`bin/ant` launch script not found in: " + home);
		}
		var jar = home.resolve(Path.of("lib", "ant.jar"));
		if (!Files.isRegularFile(jar)) {
			throw new IllegalArgumentException("main `lib/ant.jar` not found in: " + home);
		}
		this.version = getVersion(jar);
		this.executable = getExecutable(home);
	}

	private Path getExecutable(Path home) {
		var win = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
		var name = "ant" + (win ? ".bat" : "");
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
		return "ant";
	}

	@Override
	public String getVersion() {
		return version;
	}

	/** Get version from "version.txt" in "lib/ant.jar". */
	private String getVersion(Path jar) {
		return Bartholdy.read(jar, "/org/apache/tools/ant/version.txt", "", "?");
	}
}
