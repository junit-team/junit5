/*
 * Copyright 2015-2019 the original author or authors.
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
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import de.sormuras.bartholdy.Result;

import org.junit.jupiter.api.Test;
import platform.tooling.support.Request;

/**
 * @since 1.4
 */
class MultiReleaseJarTests {

	@Test
	void checkDefault() throws Exception {
		var variant = "default";
		var expectedLines = List.of( //
			">> BANNER >>", //
			".", //
			"'-- JUnit Jupiter [OK]", //
			"  '-- JupiterIntegrationTests [OK]", //
			"    +-- javaPlatformModuleSystemIsAvailable() [OK]", //
			"    +-- javaScriptingModuleIsAvailable() [OK]", //
			"    +-- moduleIsNamed() [OK]", //
			"    +-- packageName() [OK]", //
			"    '-- resolve() [OK]", //
			"", //
			"Test run finished after \\d+ ms", //
			"[         2 containers found      ]", //
			"[         0 containers skipped    ]", //
			"[         2 containers started    ]", //
			"[         0 containers aborted    ]", //
			"[         2 containers successful ]", //
			"[         0 containers failed     ]", //
			"[         5 tests found           ]", //
			"[         0 tests skipped         ]", //
			"[         5 tests started         ]", //
			"[         0 tests aborted         ]", //
			"[         5 tests successful      ]", //
			"[         0 tests failed          ]", //
			"" //
		);

		var result = mvn(variant, expectedLines);

		assertEquals(0, result.getExitCode(), result.toString());
		assertEquals("", result.getOutput("err"));
		assertTrue(result.getOutputLines("out").contains("[INFO] BUILD SUCCESS"));
	}

	@Test
	void checkNoScripting() throws Exception {
		var variant = "no-scripting";
		var expectedLines = List.of( //
			">> BANNER >>", ".", //
			"'-- JUnit Jupiter [OK]", //
			"  '-- JupiterIntegrationTests [OK]", //
			"    +-- javaPlatformModuleSystemIsAvailable() [OK]", //
			"    \\Q+-- javaScriptingModuleIsAvailable() [X] Failed to evaluate condition" //
					+ " [org.junit.jupiter.engine.extension.ScriptExecutionCondition]:" //
					+ " Class `javax.script.ScriptEngine` is not loadable," //
					+ " script-based test execution is disabled. If the originating" //
					+ " cause is a `NoClassDefFoundError: javax/script/...` and the" //
					+ " underlying runtime environment is executed with an activated" //
					+ " module system (aka Jigsaw or JPMS) you need to add the" //
					+ " `java.scripting` module to the root modules via" //
					+ " `--add-modules ...,java.scripting`\\E", //
			"    +-- moduleIsNamed() [OK]", //
			"    +-- packageName() [OK]", //
			"    '-- resolve() [OK]", //
			"", //
			">> STACKTRACE >>", //
			"", //
			"Test run finished after \\d+ ms", //
			"[         2 containers found      ]", //
			"[         0 containers skipped    ]", //
			"[         2 containers started    ]", //
			"[         0 containers aborted    ]", //
			"[         2 containers successful ]", //
			"[         0 containers failed     ]", //
			"[         5 tests found           ]", //
			"[         0 tests skipped         ]", //
			"[         5 tests started         ]", //
			"[         0 tests aborted         ]", //
			"[         4 tests successful      ]", //
			"[         1 tests failed          ]", //
			"" //
		);
		var result = mvn(variant, expectedLines);

		assertEquals(1, result.getExitCode(), result.toString());
		assertEquals("", result.getOutput("err"));
		assertTrue(result.getOutputLines("out").contains("[INFO] BUILD FAILURE"));
	}

	private Result mvn(String variant, List<String> expectedLines) throws Exception {
		var result = Request.builder() //
				.setTool(Request.maven()) //
				.setProject("multi-release-jar") //
				.addArguments("--show-version", "--errors", "--file", variant, "test") //
				.setTimeout(Duration.ofMinutes(2)) //
				.build() //
				.run();

		assumeFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		var workspace = Path.of("build/test-workspace/multi-release-jar", variant);
		var actualLines = Files.readAllLines(workspace.resolve("target/junit-platform/console-launcher.out.log"));
		assertLinesMatch(expectedLines, actualLines);

		return result;
	}

}
