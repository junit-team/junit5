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

import java.nio.file.Path;
import java.util.Locale;

import de.sormuras.bartholdy.Bartholdy;

public class GradleWrapper extends AbstractTool {

	private final Path home;

	public GradleWrapper() {
		this(Path.of("."));
	}

	public GradleWrapper(Path home) {
		this.home = home;
	}

	@Override
	public Path getHome() {
		return home;
	}

	@Override
	protected Path createPathToProgram() {
		return getHome().resolve(getProgram());
	}

	@Override
	public String getName() {
		return "gradle-wrapper";
	}

	@Override
	public String getProgram() {
		var win = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");
		return "gradlew" + (win ? ".bat" : "");
	}

	@Override
	public String getVersion() {
		var jar = getHome().resolve("gradle").resolve("wrapper").resolve("gradle-wrapper.jar");
		var text = Bartholdy.read(jar, "/build-receipt.properties", System.lineSeparator(), "?");
		return Bartholdy.readProperty(text, "versionNumber", "unknown");
	}
}
