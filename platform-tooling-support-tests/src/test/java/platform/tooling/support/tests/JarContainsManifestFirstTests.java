/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static platform.tooling.support.Helper.createJarPath;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.jar.JarInputStream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @since 1.8
 */
class JarContainsManifestFirstTests {

	@ParameterizedTest
	@MethodSource("platform.tooling.support.Helper#loadModuleDirectoryNames")
	void manifestFirst(String module) throws Exception {
		var modulePath = createJarPath(module);

		if (Files.notExists(modulePath)) {
			fail("No such file: " + modulePath);
		}

		// JarInputStream expects the META-INF/MANIFEST.MF to be at the start of the JAR archive
		try (final JarInputStream jarInputStream = new JarInputStream(new FileInputStream(modulePath.toFile()))) {
			assertNotNull(jarInputStream.getManifest(), "MANIFEST.MF should be available via JarInputStream");
		}
	}
}
