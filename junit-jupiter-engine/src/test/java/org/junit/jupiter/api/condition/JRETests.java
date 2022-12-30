/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.JRE.JAVA_10;
import static org.junit.jupiter.api.condition.JRE.JAVA_11;
import static org.junit.jupiter.api.condition.JRE.JAVA_12;
import static org.junit.jupiter.api.condition.JRE.JAVA_13;
import static org.junit.jupiter.api.condition.JRE.JAVA_14;
import static org.junit.jupiter.api.condition.JRE.JAVA_15;
import static org.junit.jupiter.api.condition.JRE.JAVA_16;
import static org.junit.jupiter.api.condition.JRE.JAVA_17;
import static org.junit.jupiter.api.condition.JRE.JAVA_18;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;
import static org.junit.jupiter.api.condition.JRE.JAVA_9;
import static org.junit.jupiter.api.condition.JRE.JAVA_20;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.junit.jupiter.api.condition.JRE.OTHER;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JRE}
 *
 * @since 5.7
 */
public class JRETests {

	@Test
	@EnabledOnJre(JAVA_8)
	void java8() {
		assertEquals(JAVA_8, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_9)
	void java9() {
		assertEquals(JAVA_9, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_10)
	void java10() {
		assertEquals(JAVA_10, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_11)
	void java11() {
		assertEquals(JAVA_11, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_12)
	void java12() {
		assertEquals(JAVA_12, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_13)
	void java13() {
		assertEquals(JAVA_13, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_14)
	void java14() {
		assertEquals(JAVA_14, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_15)
	void java15() {
		assertEquals(JAVA_15, JRE.currentVersion());
	}

	@Test
	@EnabledOnJre(JAVA_16)
	void java16() {
		assertEquals(JAVA_16, JRE.currentVersion());
	}

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
	@EnabledOnJre(OTHER)
	void other() {
		assertEquals(OTHER, JRE.currentVersion());
	}
}
