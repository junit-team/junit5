/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;

/**
 * @since 5.4
 */
class JUnit4VersionCheckTests {

	@Test
	void handlesErrorsReadingVersion() {
		Error error = new NoClassDefFoundError();

		JUnitException exception = assertThrows(JUnitException.class, () -> JUnit4VersionCheck.checkSupported(() -> {
			throw error;
		}));

		assertEquals("Failed to read version of junit:junit", exception.getMessage());
		assertSame(error, exception.getCause());
	}

	@Test
	void handlesErrorsParsingVersion() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> JUnit4VersionCheck.checkSupported(() -> "not a version"));

		assertEquals("Failed to parse version of junit:junit: not a version", exception.getMessage());
	}

	@Test
	void throwsExceptionOnUnsupportedVersion() {
		JUnitException exception = assertThrows(JUnitException.class,
			() -> JUnit4VersionCheck.checkSupported(() -> "4.11"));

		assertEquals("Unsupported version of junit:junit: 4.11. Please upgrade to version 4.12 or later.",
			exception.getMessage());
	}

}
