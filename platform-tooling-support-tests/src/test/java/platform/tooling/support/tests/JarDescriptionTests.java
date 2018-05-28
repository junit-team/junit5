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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import platform.tooling.support.Helper;
import platform.tooling.support.Tool;

/**
 * @since 1.3
 */
class JarDescriptionTests {

	@ParameterizedTest
	@ValueSource(strings = { "junit-jupiter-api", "junit-jupiter-engine", "junit-jupiter-params",
			"junit-jupiter-migrationsupport" })
	void describeModule(String name) throws Exception {
		var version = Helper.version(name);
		var file = name + '-' + version + ".jar";
		var path = Paths.get("..", name, "build", "libs", file);
		var result = Tool.JAR.builder() //
				.setProject("jar-description") //
				.setLogFileNames(name + ".out.txt", name + ".err.txt") //
				.addArguments("--describe-module", "--file", path) //
				.build() //
				.run();

		assertEquals(0, result.getStatus());
		assertEquals(List.of(), result.getErrorLines(), "error log isn't empty");
		var expected = result.getWorkspace().resolve(name + ".expected.txt");
		if (Files.notExists(expected)) {
			result.getOutputLines().forEach(System.err::println);
			fail("No such file: " + expected);
		}
		assertLinesMatch(Files.readAllLines(expected), result.getOutputLines());
	}
}
