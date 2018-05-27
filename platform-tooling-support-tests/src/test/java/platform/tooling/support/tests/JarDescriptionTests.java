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

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import platform.tooling.support.Helper;
import platform.tooling.support.Tool;

/**
 * @since 1.3
 */
class JarDescriptionTests {

	@Test
	void junitJupiterApi() {
		var name = "junit-jupiter-api";
		var version = Helper.version(name);
		var file = name + '-' + version + ".jar";
		var path = Paths.get("..", name, "build", "libs", file);
		var result = Tool.JAR.builder() //
				.setProject("jar-description") //
				.setLogFileNames(name + ".out.text", name + ".err.txt") //
				.addArguments("--describe-module", "--file", path) //
				.build() //
				.run();

		assertEquals(0, result.getStatus());
		assertEquals(List.of(), result.getErrorLines(), "error log isn't empty");
		assertLinesMatch(List.of(">> HEAD >>", //
			"", //
			"org.junit.jupiter.api@" + version + " automatic", //
			"requires java.base mandated", //
			"contains org.junit.jupiter.api", //
			"contains org.junit.jupiter.api.condition", //
			"contains org.junit.jupiter.api.extension", //
			"contains org.junit.jupiter.api.function", //
			""), //
			result.getOutputLines());
	}
}
