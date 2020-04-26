/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava10;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava11;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava12;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava13;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava14;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava15;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava8;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava9;
import static org.junit.jupiter.api.condition.JRE.JAVA_10;
import static org.junit.jupiter.api.condition.JRE.JAVA_11;
import static org.junit.jupiter.api.condition.JRE.JAVA_12;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;
import static org.junit.jupiter.api.condition.JRE.JAVA_9;
import static org.junit.jupiter.api.condition.JRE.OTHER;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for {@link EnabledForJreRange}.
 *
 * @since 5.6
 */
class EnabledForJreRangeIntegrationTests {

	@Test
	@Disabled("Only used in a unit test via reflection")
	void enabledBecauseAnnotationIsNotPresent() {
	}

	@Test
	@Disabled("Only used in a unit test via reflection")
	@EnabledForJreRange
	void defaultValues() {
		fail("should result in a configuration exception");
	}

	@Test
	@EnabledForJreRange(min = JAVA_8, max = JAVA_8)
	void java8() {
		assertTrue(onJava8());
	}

	@Test
	@EnabledForJreRange(min = JAVA_8, max = JAVA_11, disabledReason = "Disabled on some JRE")
	void java8to11() {
		assertTrue(onJava8() || onJava9() || onJava10() || onJava11());
		assertFalse(onJava12());
	}

	@Test
	@EnabledForJreRange(min = JAVA_9, max = JAVA_12)
	void java9to12() {
		assertTrue(onJava9() || onJava10() || onJava11() || onJava12());
		assertFalse(onJava8() || onJava13());
	}

	@Test
	@EnabledForJreRange(max = JAVA_12)
	void javaMax12() {
		assertTrue(onJava8() || onJava9() || onJava10() || onJava11() || onJava12());
		assertFalse(onJava13());
	}

	@Test
	@EnabledForJreRange(min = JAVA_10)
	void javaMin10() {
		assertTrue(onJava10() || onJava11() || onJava12() || onJava13() || onJava14() || onJava15());
		assertFalse(onJava9());
	}

	@Test
	@EnabledForJreRange(min = OTHER, max = OTHER)
	void other() {
		assertFalse(
			onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13() || onJava14() || onJava15());
	}

}
