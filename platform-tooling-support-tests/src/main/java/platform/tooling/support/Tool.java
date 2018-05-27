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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * @since 1.3
 */
public enum Tool {

	// JDK Foundation Tools

	JAR("jar"),

	// Locally installed Build Tool

	GRADLEW("../gradlew", "../gradlew.bat"),

	// Downloadable Tools

	ANT("bin/ant", "bin/ant.bat", //
			"https://www.apache.org/dist/ant/binaries/${ARCHIVE}", "apache-ant-${VERSION}-bin.zip"),

	GRADLE("bin/gradle", "bin/gradle.bat", //
			"https://services.gradle.org/distributions/${ARCHIVE}", "gradle-${VERSION}-bin.zip"),

	MAVEN("bin/mvn", "bin/mvn.cmd", //
			"https://archive.apache.org/dist/maven/maven-3/${VERSION}/binaries/${ARCHIVE}",
			"apache-maven-${VERSION}-bin.zip");

	public enum Kind {
		JDK, LOCAL, REMOTE
	}

	private final Kind kind;
	private final String executable;
	private final String wincutable;
	private final String uriTemplate;
	private final String zipTemplate;

	Tool(String executable) {
		this(Kind.JDK, executable, null, null, null);
	}

	Tool(String executable, String wincutable) {
		this(Kind.LOCAL, executable, wincutable, null, null);
	}

	Tool(String executable, String wincutable, String uriTemplate, String zipTemplate) {
		this(Kind.REMOTE, executable, wincutable, uriTemplate, zipTemplate);
	}

	Tool(Kind kind, String executable, String wincutable, String uriTemplate, String zipTemplate) {
		this.kind = kind;
		this.executable = executable;
		this.wincutable = wincutable;
		this.uriTemplate = uriTemplate;
		this.zipTemplate = zipTemplate;
	}

	public Request.Builder builder() {
		return new Request.Builder().setTool(this);
	}

	public Request.Builder builder(String version) {
		return new Request.Builder().setTool(this, version);
	}

	public Kind getKind() {
		return kind;
	}

	public String getExecutable() {
		return executable;
	}

	public String computeArchive(String version) {
		return zipTemplate.replace("${VERSION}", version);
	}

	public URI computeUri(String version) {
		var uri = uriTemplate.replace("${ARCHIVE}", zipTemplate).replace("${VERSION}", version);
		return URI.create(uri);
	}

	public Path computeExecutablePath() {
		var windows = System.getProperty("os.name").toLowerCase().contains("win");
		var path = Paths.get(windows ? wincutable : executable);
		if (!windows) {
			try {
				Files.setPosixFilePermissions(path, Set.of(PosixFilePermission.OWNER_EXECUTE));
			}
			catch (IOException e) {
				throw new UncheckedIOException("setting executable failed", e);
			}
		}
		return path;
	}

}
