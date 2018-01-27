/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.extensions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

/**
 * Script-based execution condition evaluation tests.
 *
 * @since 1.1
 */
@EnabledIf("true")
class EnabledIfTests {

	@Test
	@EnabledIf("true")
	void justTrue() {
	}

	@Test
	@EnabledIf("false")
	void justFalse() {
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
	@EnabledIf(value = { //
			"load('nashorn:mozilla_compat.js')", //
			"importPackage(java.nio.file)", //
			"", //
			"var path = Files.createTempFile('volatile-', '.temp')", //
			"java.lang.System.getProperties().put('volatile', path)", //
			"Files.exists(path)" //
	})
	void multiLineAndImportJavaPackage() {
		assertTrue(Files.exists((Path) System.getProperties().get("volatile")));
	}

	@Test
	@EnabledIf(value = "jupiterConfigurationParameter.get('some.value.or.null')")
	void getJupiterConfigurationParameter() {
		fail("test must not be executed");
	}

	@Test
	@EnabledIf(value = "syntactically, something is not right")
	void syntaxFailure() {
		fail("test must not be executed");
	}

	@Test
	@EnabledIf("java.lang.System.getProperty('os.name').toLowerCase().contains('win')")
	void win() {
		assertTrue(System.getProperty("os.name").toLowerCase().contains("win"));
	}

	@Test
	@EnabledIf("/64/.test(systemProperty.get('os.arch'))")
	void osArch() {
		assertTrue(System.getProperty("os.arch").contains("64"));
	}

	@Test
	@EnabledIf(engine = "groovy", value = { "System.properties['jsr'] = '233'", "'233' == System.properties['jsr']" })
	void groovy() {
		assertEquals("233", System.getProperty("jsr"));
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

	@RepeatedTest(10)
	@CoinToss
	void gamble() {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@EnabledIf("Math.random() >= 0.5")
	@interface CoinToss {
	}
}
