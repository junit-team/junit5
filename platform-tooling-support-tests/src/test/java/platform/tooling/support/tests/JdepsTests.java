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
class JdepsTests {

	@ParameterizedTest
	@MethodSource("platform.tooling.support.Helper#loadModuleDirectoryNames")
	void modules(String module) {
		var jar = Helper.createJarPath(module);
		var result = new CyclesDetector(jar).run(Configuration.of());

		switch (module) {
			case "junit-jupiter-api":
				assertEquals(1, result.getExitCode(), "result=" + result);
				assertLinesMatch(List.of(
					"org.junit.jupiter.api.extension.ExtensionContext -> org.junit.jupiter.api.TestInstance",
					"org.junit.jupiter.api.extension.ExtensionContext -> org.junit.jupiter.api.TestInstance$Lifecycle"),
					result.getOutputLines("err"));
				break;
			case "junit-jupiter-engine":
				assertEquals(1, result.getExitCode(), "result=" + result);
				assertLinesMatch(List.of(
					"org.junit.jupiter.engine.descriptor.TestInstanceLifecycleUtils -> org.junit.jupiter.engine.Constants",
					"org.junit.jupiter.engine.discovery.JavaElementsResolver -> org.junit.jupiter.engine.JupiterTestEngine",
					"org.junit.jupiter.engine.execution.ConditionEvaluator -> org.junit.jupiter.engine.Constants",
					"org.junit.jupiter.engine.extension.ExtensionRegistry -> org.junit.jupiter.engine.Constants"),
					result.getOutputLines("err"));
				break;
			case "junit-jupiter-params":
				assertEquals(1, result.getExitCode(), "result=" + result);
				assertTrue(result.getOutputLines("err").stream().allMatch(
					line -> line.startsWith("org.junit.jupiter.params.shadow.com.univocity.parsers.")));
				break;
			default:
				assertEquals(0, result.getExitCode(), "result=" + result);
		}

	}

}
