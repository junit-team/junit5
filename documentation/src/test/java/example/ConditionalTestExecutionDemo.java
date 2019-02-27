/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.condition.JRE.JAVA_10;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;
import static org.junit.jupiter.api.condition.JRE.JAVA_9;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.DisabledOnJre;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;

class ConditionalTestExecutionDemo {

	// tag::user_guide_os[]
	@Test
	@EnabledOnOs(MAC)
	void onlyOnMacOs() {
		// ...
	}

	@TestOnMac
	void testOnMac() {
		// ...
	}

	@Test
	@EnabledOnOs({ LINUX, MAC })
	void onLinuxOrMac() {
		// ...
	}

	@Test
	@DisabledOnOs(WINDOWS)
	void notOnWindows() {
		// ...
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@Test
	@EnabledOnOs(MAC)
	@interface TestOnMac {
	}
	// end::user_guide_os[]

	// tag::user_guide_jre[]
	@Test
	@EnabledOnJre(JAVA_8)
	void onlyOnJava8() {
		// ...
	}

	@Test
	@EnabledOnJre({ JAVA_9, JAVA_10 })
	void onJava9Or10() {
		// ...
	}

	@Test
	@DisabledOnJre(JAVA_9)
	void notOnJava9() {
		// ...
	}
	// end::user_guide_jre[]

	// tag::user_guide_system_property[]
	@Test
	@EnabledIfSystemProperty(named = "os.arch", matches = ".*64.*")
	void onlyOn64BitArchitectures() {
		// ...
	}

	@Test
	@DisabledIfSystemProperty(named = "ci-server", matches = "true")
	void notOnCiServer() {
		// ...
	}
	// end::user_guide_system_property[]

	// tag::user_guide_environment_variable[]
	@Test
	@EnabledIfEnvironmentVariable(named = "ENV", matches = "staging-server")
	void onlyOnStagingServer() {
		// ...
	}

	@Test
	@DisabledIfEnvironmentVariable(named = "ENV", matches = ".*development.*")
	void notOnDeveloperWorkstation() {
		// ...
	}
	// end::user_guide_environment_variable[]

	// tag::user_guide_scripts[]
	@Test // Static JavaScript expression.
	@EnabledIf("2 * 3 == 6")
	void willBeExecuted() {
		// ...
	}

	@RepeatedTest(10) // Dynamic JavaScript expression.
	@DisabledIf("Math.random() < 0.314159")
	void mightNotBeExecuted() {
		// ...
	}

	@Test // Regular expression testing bound system property.
	@DisabledIf("/32/.test(systemProperty.get('os.arch'))")
	void disabledOn32BitArchitectures() {
		assertFalse(System.getProperty("os.arch").contains("32"));
	}

	@Test
	@EnabledIf("'CI' == systemEnvironment.get('ENV')")
	void onlyOnCiServer() {
		assertTrue("CI".equals(System.getenv("ENV")));
	}

	@Test // Multi-line script, custom engine name and custom reason.
	// end::user_guide_scripts[]
	// @formatter:off
	// tag::user_guide_scripts[]
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
	// end::user_guide_scripts[]
	// @formatter:on
	// tag::user_guide_scripts[]
	void theDayAfterTomorrow() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		assertTrue(tomorrow.isAfter(today));
	}
	// end::user_guide_scripts[]

}
