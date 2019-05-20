/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Script-based execution condition evaluation tests.
 *
 * @since 1.1
 */
@Deprecated
@DisabledIf("false")
class DisabledIfTests {

	@Test
	@DisabledIf("true")
	void booleanTrue() {
		fail("test must not be executed");
	}

	@Test
	@DisabledIf("java.lang.Boolean.TRUE")
	void booleanWrapperTrue() {
		fail("test must not be executed");
	}

	@Test
	@DisabledIf("'TrUe'")
	void stringTrue() {
		fail("test must not be executed");
	}

	@Test
	@DisabledIf("false")
	void booleanFalse() {
	}

	@Test
	@DisabledIf("java.lang.Boolean.FALSE")
	void booleanWrapperFalse() {
	}

	@Test
	@DisabledIf("'FaLsE'")
	void stringFalse() {
	}

	@Test
	@DisabledIf("1 == 2")
	void oneEqualsTwo() {
	}

	@Test
	@DisabledIf("org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled('Go!')")
	void customResultEnabled() {
	}

	@Test
	@DisabledIf("org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled('No go.')")
	void customResultDisabled() {
		fail("test must not be executed");
	}

	@Test
	@DisabledIf("java.lang.Boolean.getBoolean('is-not-set')")
	void getBoolean() {
	}

	@Test
	@DisabledIf({ //
			"load('nashorn:mozilla_compat.js')", //
			"importPackage(java.time)", //
			"", //
			"var today = LocalDate.now()", //
			"var tomorrow = today.plusDays(1)", //
			"tomorrow.isAfter(today)" //
	})
	void multiLineAndImportJavaPackage() {
		fail("test must not be executed");
	}

	@Test
	@DisabledIf("java.lang.System.getProperty('os.name').toLowerCase().contains('win')")
	void notOnMicrosoftWindows() {
		assertTrue(!System.getProperty("os.name").toLowerCase().contains("win"));
	}

	@Test
	@DisabledIf("java.lang.System.getProperty('os.name').toLowerCase().contains('mac')")
	void notOnMacOs() {
		assertTrue(!System.getProperty("os.name").toLowerCase().contains("mac"));
	}

	@Test
	@DisabledIf("/64/.test(systemProperty.get('os.arch'))")
	void osArch() {
		assertTrue(System.getProperty("os.arch").contains("64"));
	}

	@Test
	@DisabledIf(engine = "java", value = { "System.getProperties().put(0xCAFE, 233);", "return false;" })
	void java() {
		assertEquals(233, System.getProperties().get(0xCAFE));
	}

	@Test
	@DisabledIf("false")
	@Disabled
	void disabledIfAndDisabled() {
		fail("test must not be executed");
	}

	@Test
	@Disabled
	@DisabledIf("false")
	void disabledAndDisabledIf() {
		fail("test must not be executed");
	}

	@Test
	@DisabledIf("false")
	@EnabledIf("false")
	void disabledIfAndEnabledIf() {
		fail("test must not be executed");
	}

	@RepeatedTest(2)
	@CoinToss
	void gamble() {
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	// @CoinToss was originally annotated as follows:
	// @DisabledIf("Math.random() >= 0.5")
	// ... but we've replaced the randomness in order to ensure stability in the
	// number of tests executed within our test suite.
	@DisabledIf("Math.random() >= 0.0")
	@interface CoinToss {
	}

}
