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
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import platform.tooling.support.Helper;
import platform.tooling.support.Tool;

/**
 * @since 1.3
 */
class JarDescribeModuleTests {

	@ParameterizedTest
	@MethodSource("platform.tooling.support.Helper#loadModuleDirectoryNames")
	void describeModule(String module) throws Exception {
		var version = Helper.version(module);
		var archive = module + '-' + version + ".jar";
		var path = Paths.get("..", module, "build", "libs", archive);
		var result = Tool.JAR.builder() //
				.setProject("jar-describe-module") //
				.setProjectToWorkspaceCopyFileFilter(file -> file.getName().startsWith(module)) //
				.setWorkspace("jar-describe-module/" + module) //
				.setLogFileNames(module + ".out.txt", module + ".err.txt") //
				.addArguments("--describe-module", "--file", path) //
				.build() //
				.run();

		assertEquals(0, result.getStatus());
		assertEquals(List.of(), result.getErrorLines(), "error log isn't empty");
		var expected = result.getWorkspace().resolve(module + ".expected.txt");
		if (Files.notExists(expected)) {
			result.getOutputLines().forEach(System.err::println);
			fail("No such file: " + expected);
		}
		var expectedLines = Files.lines(expected).map(Helper::replaceVersionPlaceholders).collect(Collectors.toList());
		assertLinesMatch(expectedLines, result.getOutputLines());
	}

}
