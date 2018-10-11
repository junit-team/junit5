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
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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

		// TODO Only retain the 'default' case by removing the cyclic package references
		//      where possible and configure shadowed packages to be ignored by the detector.
		//      See https://github.com/junit-team/junit5/issues/1626
		switch (module) {
			case "junit-jupiter-engine":
				assertEquals(1, result.getExitCode(), "result=" + result);
				assertLinesMatch(List.of(
					"Adding edge 'org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils -> org.junit.jupiter.engine.Constants' failed."
							+ " Cycle detected: Anti-edge: org.junit.jupiter.engine.descriptor <-> org.junit.jupiter.engine",

					"Adding edge 'org.junit.jupiter.engine.discovery.JavaElementsResolver -> org.junit.jupiter.engine.JupiterTestEngine' failed."
							+ " Cycle detected: Anti-edge: org.junit.jupiter.engine.discovery <-> org.junit.jupiter.engine",

					"Adding edge 'org.junit.jupiter.engine.execution.ConditionEvaluator -> org.junit.jupiter.engine.Constants' failed."
							+ " Cycle detected: Anti-edge: org.junit.jupiter.engine.execution <-> org.junit.jupiter.engine",

					"Adding edge 'org.junit.jupiter.engine.extension.ExtensionRegistry -> org.junit.jupiter.engine.Constants' failed."
							+ " Cycle detected: From org.junit.jupiter.engine.extension"
							+ " over [org.junit.jupiter.engine, org.junit.jupiter.engine.descriptor]"
							+ " and org.junit.jupiter.engine.execution"
							+ " back to org.junit.jupiter.engine.extension"),
					result.getOutputLines("cycles"));
				break;
			case "junit-jupiter-params":
				assertEquals(1, result.getExitCode(), "result=" + result);
				assertTrue(result.getOutputLines("cycles").stream().allMatch(
					line -> line.contains("org.junit.jupiter.params.shadow.com.univocity.parsers.")));
				break;
			default:
				assertEquals(0, result.getExitCode(), "result=" + result);
		}

	}

}
