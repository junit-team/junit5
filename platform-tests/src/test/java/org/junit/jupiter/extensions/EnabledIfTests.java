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

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

/**
 * Script-based execution condition evaluation tests.
 *
 * @since 1.1
 */
@Deprecated
@EnabledIf("true")
class EnabledIfTests {

	@Test
	@EnabledIf("true")
	void booleanTrue() {
	}

	@Test
	@EnabledIf("java.lang.Boolean.TRUE")
	void booleanWrapperTrue() {
	}

	@Test
	@EnabledIf("'TrUe'")
	void stringTrue() {
	}

	@Test
	@EnabledIf("false")
	void booleanFalse() {
		fail("test must not be executed");
	}

	@Test
	@EnabledIf("java.lang.Boolean.FALSE")
	void booleanWrapperFalse() {
		fail("test must not be executed");
	}

	@Test
	@EnabledIf("'FaLsE'")
	void stringFalse() {
		fail("test must not be executed");
	}

	@Test
	@EnabledIf("1 == 2")
	void oneEqualsTwo() {
		fail("test must not be executed");
	}

	@Test
	@EnabledIf("org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled('Go!')")
	void customResultEnabled() {
	}

	@Test
	@EnabledIf("org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled('No go.')")
	void customResultDisabled() {
		fail("test must not be executed");
	}

	@Test
	@EnabledIf("java.lang.Boolean.getBoolean('is-not-set')")
	void getBoolean() {
		fail("test must not be executed");
	}

	@Test
	@EnabledIf({ //
			"load('nashorn:mozilla_compat.js')", //
			"importPackage(java.nio.file)", //
			"", //
			"var path = Files.createTempFile('volatile-', '.temp')", //
			"java.lang.System.getProperties().put('volatile', path)", //
			"Files.exists(path)" //
	})
	void multiLineAndImportJavaPackage() throws IOException {
		Path path = (Path) System.getProperties().get("volatile");
		assertTrue(Files.exists(path));
		Files.deleteIfExists(path);
	}

	@Test
	@EnabledIf("java.lang.System.getProperty('os.name').toLowerCase().contains('win')")
	void onMicrosoftWindows() {
		assertTrue(System.getProperty("os.name").toLowerCase().contains("win"));
	}

	@Test
	@EnabledIf("java.lang.System.getProperty('os.name').toLowerCase().contains('mac')")
	void onMacOs() {
		assertTrue(System.getProperty("os.name").toLowerCase().contains("mac"));
	}

	@Test
	@EnabledIf("/64/.test(systemProperty.get('os.arch'))")
	void osArch() {
		assertTrue(System.getProperty("os.arch").contains("64"));
	}

	@Test
	@EnabledIf(engine = "java", value = { "System.getProperties().put(0xCAFE, 233);", "return true;" })
	void java() {
		assertEquals(233, System.getProperties().get(0xCAFE));
	}

	@Test
	@EnabledIf("true")
	@Disabled
	void enabledAndDisabled() {
		fail("test must not be executed");
	}

	@Test
	@Disabled
	@EnabledIf("true")
	void disabledAndEnabled() {
		fail("test must not be executed");
	}

	@RepeatedTest(2)
	@CoinToss
	void gamble() {
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	// @CoinToss was originally annotated as follows:
	// @EnabledIf("Math.random() >= 0.5")
	// ... but we've replaced the randomness in order to ensure stability in the
	// number of tests executed within our test suite.
	@EnabledIf("Math.random() >= 0.0")
	@interface CoinToss {
	}

}
