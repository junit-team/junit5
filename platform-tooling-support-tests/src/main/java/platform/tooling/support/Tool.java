/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum Tool {

	ANT("https://www.apache.org/dist/ant/binaries/${ARCHIVE}", "apache-ant-${VERSION}-bin.zip"),

	GRADLE("https://services.gradle.org/distributions/${ARCHIVE}", "gradle-${VERSION}-bin.zip"),

	MAVEN("https://archive.apache.org/dist/maven/maven-3/${VERSION}/binaries/${ARCHIVE}",
			"apache-maven-${VERSION}-bin.zip");

	private final String uriTemplate;
	private final String zipTemplate;
	private final String exeTemplate;

	Tool(String uriTemplate, String zipTemplate) {
		this.uriTemplate = uriTemplate;
		this.zipTemplate = zipTemplate;
		this.exeTemplate = "bin/" + name().toLowerCase();
	}

	public String createArchive(String version) {
		return zipTemplate.replace("${VERSION}", version);
	}

	public URI createUri(String version) {
		var uri = uriTemplate.replace("${ARCHIVE}", zipTemplate).replace("${VERSION}", version);
		return URI.create(uri);
	}

	public Path createExecutable() {
		var executable = exeTemplate;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			executable += ".bat";
		}
		return Paths.get(executable);
	}
}
