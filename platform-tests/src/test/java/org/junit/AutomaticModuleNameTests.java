/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
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

	static Stream<String> moduleDirectoryNames() throws IOException {
		// @formatter:off
		return Files.walk(Paths.get(".."), 1)
				.filter(Files::isDirectory)
				.map(Path::getFileName)
				.map(Object::toString)
				.filter(name -> !name.equals("junit-platform-console-standalone"))
				.filter(name -> name.startsWith("junit-"));
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
			List<String> unexpectedNames = new ArrayList<>();
			String expectedStartOfPackageName = expected.replace('.', '/');
			// @formatter:off
			jarFile.stream()
					.map(ZipEntry::getName)
					.filter(n -> n.endsWith(".class"))
					.filter(n -> !n.startsWith(expectedStartOfPackageName))
					.forEach(unexpectedNames::add);
			// @formatter:on
			assertTrue(unexpectedNames.isEmpty(), unexpectedNames.size()
					+ " entries are not located in (a sub-) package of " + expectedStartOfPackageName);
		}
		catch (IOException e) {
			fail("test jar file failed: " + e, e);
		}
	}

}
