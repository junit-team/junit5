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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisabledIf;
import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

// tag::user_guide[]
class ConditionalTestExecutionDemo {

	@Test // Static JavaScript expression.
	@EnabledIf("1 == 1")
	void testWillBeExecuted() {
		assertTrue(1 == 1);
	}

	@RepeatedTest(10) // Dynamic JavaScript expression.
	@DisabledIf("Math.random() < 0.314159")
	void testWillNeverOrSometimesBeExecuted() {
		assertTrue(Math.random() >= 0);
	}

	@Test // Regular expression testing bound system property.
	@DisabledIf("/32/.test(systemProperty.get('os.arch'))")
	void testWontBeExecutedIfOsArchitectureContains32() {
		assertFalse(System.getProperty("os.arch").contains("32"));
	}

	@Test
	@EnabledIf("'CI' == systemEnvironment.get('ENV')")
	void testOnlyOnCiServer() {
		assertTrue("CI".equals(System.getenv("ENV")));
	}

	@Test // Multi-line script, custom engine name and custom reason.
	// end::user_guide[]
	// @formatter:off
	// tag::user_guide[]
	@EnabledIf(value = {
					"load('nashorn:mozilla_compat.js')",
					"importPackage(java.time)",
					"",
					"var today = LocalDate.now()",
					"var tomorrow = today.plusDays(1)",
					"tomorrow.isAfter(today)"
				},
				engine = "nashorn",
				reason = "Self-fulfilling: {result}")
	// end::user_guide[]
	// @formatter:on
	// tag::user_guide[]
	void theDayAfterTomorrow() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		assertTrue(tomorrow.isAfter(today));
	}
}
// end::user_guide[]
