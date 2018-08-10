/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @since 1.0
 */
class AutomaticModuleNameTests {

	private static Properties gradleProperties = new Properties();

	static {
		try {
			gradleProperties.load(Files.newInputStream(Paths.get("..", "gradle.properties")));
		}
		catch (IOException e) {
			throw new AssertionError("loading gradle.properties failed", e);
		}
	}

	private static String version(String module) {
		if (module.startsWith("junit-jupiter")) {
			return gradleProperties.getProperty("version");
		}
		if (module.startsWith("junit-platform")) {
			return gradleProperties.getProperty("platformVersion");
		}
		if (module.startsWith("junit-vintage")) {
			return gradleProperties.getProperty("vintageVersion");
		}
		throw new AssertionError("module name is unknown: " + module);
	}

	@SuppressWarnings("unused")
	private static List<String> moduleDirectoryNames() throws IOException {
		// @formatter:off
		Pattern moduleLinePattern = Pattern.compile("include\\(\"(.+)\"\\)");
		try (Stream<String> stream = Files.lines(Paths.get("../settings.gradle.kts"))
				.map(moduleLinePattern::matcher)
				.filter(Matcher::matches)
				.map(matcher -> matcher.group(1))
				.filter(name -> !name.equals("junit-platform-console-standalone"))
				.filter(name -> !name.equals("junit-bom"))
				.filter(name -> !name.endsWith("-java-9"))
				.filter(name -> name.startsWith("junit-"))) {
			return stream.collect(Collectors.toList());
		}
		// @formatter:on
	}

	@ParameterizedTest
	@MethodSource("moduleDirectoryNames")
	void automaticModuleName(String module) {
		String expected = "org." + module.replace('-', '.');
		String jarName = module + "-" + version(module) + ".jar";
		Path jarPath = Paths.get("..", module).resolve("build/libs").resolve(jarName).normalize();
		try (JarFile jarFile = new JarFile(jarPath.toFile())) {
			// first, check automatic module name
			Manifest manifest = jarFile.getManifest();
			String automaticModuleName = manifest.getMainAttributes().getValue("Automatic-Module-Name");
			assertNotNull(automaticModuleName, "`Automatic-Module-Name` not found in manifest of JAR: " + jarPath);
			assertEquals(expected, automaticModuleName);
			// second, check entries are located in matching packages
			String expectedStartOfPackageName = expected.replace('.', '/');
			// @formatter:off
			List<String> unexpectedNames = jarFile.stream()
					.map(ZipEntry::getName)
					.filter(n -> n.endsWith(".class"))
					.filter(n -> !n.startsWith(expectedStartOfPackageName))
					.filter(n -> !(n.startsWith("META-INF/versions/") && n.contains(expectedStartOfPackageName)))
					.collect(toList());
			// @formatter:on
			assertTrue(unexpectedNames.isEmpty(),
				unexpectedNames.size() + " entries are not located in (a sub-) package of " + expectedStartOfPackageName
						+ ": " + unexpectedNames);
		}
		catch (IOException e) {
			fail("test jar file failed: " + e, e);
		}
	}

}
