/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.StringUtils.isBlank;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0
 */
class ConsoleLauncherIntegrationTests {

	@Test
	void executeWithoutArgumentsFailsAndPrintsHelpInformation() {
		ConsoleLauncherWrapperResult result = new ConsoleLauncherWrapper().execute();
		assertAll("empty args array results in display of help information and an exception stacktrace", //
			() -> assertArrayEquals(new String[0], result.args), //
			() -> assertEquals(StandardCharsets.UTF_8, result.charset), //
			() -> assertEquals(-1, result.code), //
			() -> assertTrue(result.out.contains("help information")), //
			() -> assertTrue(result.err.contains("No arguments were supplied to the ConsoleLauncher")) //
		);
	}

	@Test
	void executeWithoutExcludeClassnameOptionDoesNotExcludeClasses() {
		String[] args = { "-e", "junit-jupiter", "-p", "org.junit.platform.console.subpackage" };
		ConsoleLauncherWrapperResult result = new ConsoleLauncherWrapper().execute(args);
		assertAll("all subpackage test classes are found and executed", //
			() -> assertArrayEquals(args, result.args), //
			() -> assertEquals(StandardCharsets.UTF_8, result.charset), //
			() -> assertEquals(0, result.code), //
			() -> assertTrue(result.out.contains("2 tests found")), //
			() -> assertTrue(isBlank(result.err)) //
		);
	}

	@Test
	void executeWithExcludeClassnameOptionExcludesClasses() {
		String[] args = { "-e", "junit-jupiter", "-p", "org.junit.platform.console.subpackage", "--exclude-classname",
				"^org\\.junit\\.platform\\.console\\.subpackage\\..*" };
		ConsoleLauncherWrapperResult result = new ConsoleLauncherWrapper().execute(args);
		assertAll("all subpackage test classes are excluded by the class name filter", //
			() -> assertArrayEquals(args, result.args), //
			() -> assertEquals(StandardCharsets.UTF_8, result.charset), //
			() -> assertEquals(0, result.code), //
			() -> assertTrue(result.out.contains("0 tests found")), //
			() -> assertTrue(isBlank(result.err)) //
		);
	}

}
