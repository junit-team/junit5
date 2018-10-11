/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.sormuras.bartholdy.Configuration;
import de.sormuras.bartholdy.tool.CyclesDetector;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import platform.tooling.support.Helper;

/**
 * @since 1.3
 */
class PackageCyclesDetectionTests {

	@ParameterizedTest
	@MethodSource("platform.tooling.support.Helper#loadModuleDirectoryNames")
	void moduleDoesNotContainCyclicPackageReferences(String module) {
		var jar = Helper.createJarPath(module);
		var result = new CyclesDetector(jar).run(Configuration.of());

		// TODO Configure shadowed packages to be ignored by the detector.
		//      See https://github.com/junit-team/junit5/issues/1626
		if ("junit-jupiter-params".equals(module)) {
			assertEquals(1, result.getExitCode(), "result=" + result);
			assertTrue(result.getOutputLines("cycles").stream().allMatch(
				line -> line.contains("org.junit.jupiter.params.shadow.com.univocity.parsers.")));
			return;
		}

		assertEquals(0, result.getExitCode(), "result=" + result);
	}

}
