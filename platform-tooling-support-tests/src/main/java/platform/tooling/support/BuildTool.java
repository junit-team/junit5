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

/**
 * @since 1.3
 */
public enum BuildTool {

	ANT("https://www.apache.org/dist/ant/binaries/${ARCHIVE}", "apache-ant-${VERSION}-bin.zip"),

	GRADLE("https://services.gradle.org/distributions/${ARCHIVE}", "gradle-${VERSION}-bin.zip"),

	MAVEN("https://archive.apache.org/dist/maven/maven-3/${VERSION}/binaries/${ARCHIVE}",
			"apache-maven-${VERSION}-bin.zip", "bin/mvn", "bin/mvn.cmd");

	private final String uriTemplate;
	private final String zipTemplate;
	private final String executable;
	private final String wincutable;

	BuildTool(String uriTemplate, String zipTemplate) {
		this.uriTemplate = uriTemplate;
		this.zipTemplate = zipTemplate;
		this.executable = "bin/" + name().toLowerCase();
		this.wincutable = "bin/" + name().toLowerCase() + ".bat";
	}

	BuildTool(String uriTemplate, String zipTemplate, String executable, String wincutable) {
		this.uriTemplate = uriTemplate;
		this.zipTemplate = zipTemplate;
		this.executable = executable;
		this.wincutable = wincutable;
	}

	public String createArchive(String version) {
		return zipTemplate.replace("${VERSION}", version);
	}

	public URI createUri(String version) {
		var uri = uriTemplate.replace("${ARCHIVE}", zipTemplate).replace("${VERSION}", version);
		return URI.create(uri);
	}

	public Path createExecutable() {
		var executable = this.executable;
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			executable = wincutable;
		}
		return Paths.get(executable);
	}
}
