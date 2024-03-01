/*
 * Copyright 2015-2024 the original author or authors.
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
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava16;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava17;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava18;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava19;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava20;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava21;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava22;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava23;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava8;
import static org.junit.jupiter.api.condition.EnabledOnJreIntegrationTests.onJava9;
import static org.junit.jupiter.api.condition.JRE.JAVA_17;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
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
	@EnabledForJreRange(min = JAVA_17, max = JAVA_17)
	void java17() {
		assertTrue(onJava17());
	}

	@Test
	@EnabledForJreRange(min = JAVA_18, max = JAVA_19)
	void java18to19() {
		assertTrue(onJava18() || onJava19());
		assertFalse(onJava17());
	}

	@Test
	@EnabledForJreRange(max = JAVA_18)
	void javaMax18() {
		assertTrue(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13() || onJava14()
				|| onJava15() || onJava16() || onJava17() || onJava18());
		assertFalse(onJava19());
	}

	@Test
	@EnabledForJreRange(min = JAVA_18)
	void javaMin18() {
		assertTrue(onJava18() || onJava19() || onJava20() || onJava21() || onJava22() || onJava23());
		assertFalse(onJava17());
	}

	@Test
	@EnabledForJreRange(min = OTHER, max = OTHER)
	void other() {
		assertFalse(onJava8() || onJava9() || onJava10() || onJava11() || onJava12() || onJava13() || onJava14()
				|| onJava15() || onJava16() || onJava17() || onJava18() || onJava19() || onJava20() || onJava21()
				|| onJava22() || onJava23());
	}

}
