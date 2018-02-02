/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.condition.EnabledOnJreTests.onJava10;
import static org.junit.jupiter.api.condition.EnabledOnJreTests.onJava8;
import static org.junit.jupiter.api.condition.EnabledOnJreTests.onJava9;
import static org.junit.jupiter.api.condition.JRE.JAVA_10;
import static org.junit.jupiter.api.condition.JRE.JAVA_11;
import static org.junit.jupiter.api.condition.JRE.JAVA_8;
import static org.junit.jupiter.api.condition.JRE.JAVA_9;
import static org.junit.jupiter.api.condition.JRE.OTHER;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DisabledOnJre}.
 *
 * @since 5.1
 */
class DisabledOnJreTests {

	@Test
	@DisabledOnJre(JAVA_8)
	void java8() {
		assertFalse(onJava8());
	}

	@Test
	@DisabledOnJre(JAVA_9)
	void java9() {
		assertFalse(onJava9());
	}

	@Test
	@DisabledOnJre(JAVA_10)
	void java10() {
		assertFalse(onJava10());
	}

	@Test
	@DisabledOnJre({ JAVA_8, JAVA_9, JAVA_10, JAVA_11, OTHER })
	void allVersions() {
		fail("Should always be ignored");
	}

}
