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

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import de.sormuras.bartholdy.Bartholdy;

/** Google Java Format. */
public class GoogleJavaFormat extends Java {

	public static GoogleJavaFormat install(String version, Path tools) {
		var host = "https://github.com/google/google-java-format/releases/download";
		var name = "google-java-format-" + version;
		var uri = host + '/' + name + '/' + name + "-all-deps.jar";
		var jar = Bartholdy.download(URI.create(uri), tools);
		return new GoogleJavaFormat(jar);
	}

	private final Path jar;

	public GoogleJavaFormat(Path jar) {
		this.jar = jar;
	}

	@Override
	public String getName() {
		return "google-java-format";
	}

	/** {@code google-java-format-${version}-all-deps.jar} */
	@Override
	public String getVersion() {
		return jar.getFileName().toString();
	}

	/** {@code java -jar /path/to/google-java-format-${version}-all-deps.jar <options> [files...]} */
	@Override
	protected List<String> getToolArguments() {
		return List.of("-jar", jar.toString());
	}
}
