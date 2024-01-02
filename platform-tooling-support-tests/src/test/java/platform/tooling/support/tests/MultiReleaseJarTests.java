/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static platform.tooling.support.Helper.TOOL_TIMEOUT;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.sormuras.bartholdy.Result;

import org.junit.jupiter.api.Test;

import platform.tooling.support.MavenRepo;
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
			"  +-- ModuleUtilsTests [OK]", //
			"  | +-- javaPlatformModuleSystemIsAvailable() [OK]", //
			"  | +-- findAllClassesInModule() [OK]", //
			"  | +-- findAllNonSystemBootModuleNames() [OK]", //
			"  | '-- preconditions() [OK]", //
			"  '-- JupiterIntegrationTests [OK]", //
			"    +-- moduleIsNamed() [A] Assumption failed: not running on the module-path", //
			"    +-- packageName() [OK]", //
			"    '-- resolve() [OK]", //
			"", //
			"Test run finished after \\d+ ms", //
			"[         3 containers found      ]", //
			"[         0 containers skipped    ]", //
			"[         3 containers started    ]", //
			"[         0 containers aborted    ]", //
			"[         3 containers successful ]", //
			"[         0 containers failed     ]", //
			"[         7 tests found           ]", //
			"[         0 tests skipped         ]", //
			"[         7 tests started         ]", //
			"[         1 tests aborted         ]", //
			"[         6 tests successful      ]", //
			"[         0 tests failed          ]", //
			"" //
		);

		var result = mvn(variant);

		result.getOutputLines("out").forEach(System.out::println);
		result.getOutputLines("err").forEach(System.err::println);

		assertEquals(0, result.getExitCode());
		assertEquals("", result.getOutput("err"));
		assertTrue(result.getOutputLines("out").contains("[INFO] BUILD SUCCESS"));

		var workspace = Path.of("build/test-workspace/multi-release-jar", variant);
		var actualLines = Files.readAllLines(workspace.resolve("target/junit-platform/console-launcher.out.log"));
		assertLinesMatch(expectedLines, actualLines);
	}

	private Result mvn(String variant) {
		var result = Request.builder() //
				.setTool(Request.maven()) //
				.setProject("multi-release-jar") //
				.addArguments("-Dmaven.repo=" + MavenRepo.dir()) //
				.addArguments("--update-snapshots", "--show-version", "--errors", "--batch-mode", "--file", variant,
					"test") //
				.setTimeout(TOOL_TIMEOUT) //
				.build() //
				.run();

		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		return result;
	}

}
