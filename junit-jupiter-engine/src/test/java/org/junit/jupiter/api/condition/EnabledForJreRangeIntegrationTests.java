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
import static org.junit.jupiter.api.condition.JRE.JAVA_17;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.junit.jupiter.api.condition.JRE.OTHER;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava10;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava11;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava12;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava13;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava14;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava15;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava16;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava17;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava18;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava19;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava8;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onJava9;
import static org.junit.jupiter.api.condition.JavaVersionPredicates.onKnownVersion;

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
		assertTrue(onKnownVersion());
		assertTrue(JRE.currentVersion().compareTo(JAVA_18) >= 0);
		assertFalse(onJava17());
	}

	@Test
	@EnabledForJreRange(min = OTHER, max = OTHER)
	void other() {
		assertFalse(onKnownVersion());
	}

}
