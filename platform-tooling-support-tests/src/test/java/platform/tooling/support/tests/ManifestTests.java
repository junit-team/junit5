/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import platform.tooling.support.Helper;

/**
 * @since 1.5
 */
class ManifestTests {

	@ParameterizedTest
	@MethodSource("platform.tooling.support.Helper#loadModuleDirectoryNames")
	void manifestEntriesAdhereToConventions(String module) throws Exception {
		var version = Helper.version(module);
		var modulePath = modulePath(module);
		var uri = ModuleFinder.of(modulePath).findAll().iterator().next().location().orElseThrow();
		try (var jar = new JarFile(new File(uri))) {
			var attributes = jar.getManifest().getMainAttributes();
			assertValue(attributes, "Built-By", "JUnit Team");
			assertValue(attributes, "Specification-Title", module);
			assertValue(attributes, "Specification-Version", specificationVersion(version));
			assertValue(attributes, "Specification-Vendor", "junit.org");
			assertValue(attributes, "Implementation-Title", module);
			assertValue(attributes, "Implementation-Version", version);
			assertValue(attributes, "Implementation-Vendor", "junit.org");
			assertValue(attributes, "Automatic-Module-Name", null);
			switch (module) {
				case "junit-platform-commons":
					assertValue(attributes, "Multi-Release", "true");
					break;
				case "junit-platform-console":
					assertValue(attributes, "Main-Class", "org.junit.platform.console.ConsoleLauncher");
					break;
			}
		}
	}

	private static Path modulePath(String module) {
		var version = Helper.version(module);
		var archive = module + '-' + version + ".jar";
		return Paths.get("..", module, "build", "libs", archive);
	}

	private static String specificationVersion(String version) {
		var dash = version.indexOf('-');
		if (dash < 0) {
			return version;
		}
		return version.substring(0, dash);
	}

	private static void assertValue(Attributes attributes, String name, String expected) {
		var actual = attributes.getValue(name);
		assertEquals(expected, actual,
			String.format("Manifest attribute %s expected to be %s, but is: %s", name, expected, actual));
	}
}
