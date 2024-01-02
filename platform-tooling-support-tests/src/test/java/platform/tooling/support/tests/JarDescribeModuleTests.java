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
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import de.sormuras.bartholdy.jdk.Jar;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import platform.tooling.support.Helper;
import platform.tooling.support.MavenRepo;
import platform.tooling.support.Request;

/**
 * @since 1.3
 */
class JarDescribeModuleTests {

	@ParameterizedTest
	@MethodSource("platform.tooling.support.Helper#loadModuleDirectoryNames")
	void describeModule(String module) throws Exception {
		var modulePath = MavenRepo.jar(module);
		var result = Request.builder() //
				.setTool(new Jar()) //
				.setProject("jar-describe-module") //
				.setProjectToWorkspaceCopyFileFilter(file -> file.getName().startsWith(module)) //
				.setWorkspace("jar-describe-module/" + module) //
				.addArguments("--describe-module", "--file", modulePath) //
				.build() //
				.run();

		assertFalse(result.isTimedOut(), () -> "tool timed out: " + result);

		assertEquals(0, result.getExitCode());
		assertEquals("", result.getOutput("err"), "error log isn't empty");
		var expected = Paths.get("build", "test-workspace", "jar-describe-module", module, module + ".expected.txt");
		if (Files.notExists(expected)) {
			result.getOutputLines("out").forEach(System.err::println);
			fail("No such file: " + expected);
		}
		var expectedLines = Files.lines(expected).map(Helper::replaceVersionPlaceholders).collect(Collectors.toList());
		var origin = Path.of("projects", "jar-describe-module", module + ".expected.txt").toUri();
		assertLinesMatch(expectedLines, result.getOutputLines("out"), () -> String.format("%s\nError", origin));
	}

	@ParameterizedTest
	@MethodSource("platform.tooling.support.Helper#loadModuleDirectoryNames")
	void packageNamesStartWithNameOfTheModule(String module) {
		var modulePath = MavenRepo.jar(module);
		var moduleDescriptor = ModuleFinder.of(modulePath).findAll().iterator().next().descriptor();
		var moduleName = moduleDescriptor.name();
		for (var packageName : moduleDescriptor.packages()) {
			assertTrue(packageName.startsWith(moduleName));
		}
	}

}
