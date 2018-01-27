/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

// tag::user_guide[]
class EnabledIfTestsDemo {

	@Test // Static JavaScript expression.
	@EnabledIf("1 == 1")
	void testWillBeExecuted() {
		assertTrue(1 == 1);
	}

	@RepeatedTest(10) // Dynamic JavaScript expression.
	@EnabledIf("Math.random() >= 0.314159")
	void testWillNeverOrSometimesBeExecuted() {
	}

	@Test // Regular expression testing bound system property.
	@EnabledIf("/64/.test(systemProperty.get('os.arch'))")
	void testWillBeExecutedIfOsArchitectureContains64() {
		assertTrue(System.getProperty("os.arch").contains("64"));
	}

	@Test // Multi-line script, import Java package and set custom reason.
	// end::user_guide[]
	// @formatter:off
	// tag::user_guide[]
	@EnabledIf(value = {
					"load('nashorn:mozilla_compat.js')",
					"importPackage(java.nio.file)",
					"",
					"var path = Files.createTempFile('volatile-', '.temp')",
					"java.lang.System.getProperties().put('volatile', path)",
					"Files.exists(path)"
				},
				reason = "Self-fulfilling: {result}")
	// end::user_guide[]
	// @formatter:on
	// tag::user_guide[]
	void tautology() {
		assertTrue(Files.exists((Path) System.getProperties().get("volatile")));
	}
}
// end::user_guide[]
