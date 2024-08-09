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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.JRE.JAVA_17;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.junit.jupiter.api.condition.JRE.JAVA_20;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.junit.jupiter.api.condition.JRE.JAVA_22;
import static org.junit.jupiter.api.condition.JRE.OTHER;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JRE}
 *
 * @since 5.7
 */
public class JRETests {

	@Test
	@EnabledOnJre(JAVA_17)
	void java17() {
		assertEquals(JAVA_17, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_18)
	void java18() {
		assertEquals(JAVA_18, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_19)
	void java19() {
		assertEquals(JAVA_19, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_20)
	void java20() {
		assertEquals(JAVA_20, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_21)
	void java21() {
		assertEquals(JAVA_21, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_22)
	void java22() {
		assertEquals(JAVA_22, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(OTHER)
	void other() {
		assertEquals(OTHER, JRE.currentVersion());
	}
}
