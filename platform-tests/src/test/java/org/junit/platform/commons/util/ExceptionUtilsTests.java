/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.commons.util.ExceptionUtils.readStackTrace;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link ExceptionUtils}.
 *
 * @since 1.0
 */
class ExceptionUtilsTests {

	@Test
	void throwAsUncheckedExceptionWithNullException() {
		assertThrows(PreconditionViolationException.class, () -> throwAsUncheckedException(null));
	}

	@Test
	void throwAsUncheckedExceptionWithCheckedException() {
		assertThrows(IOException.class, () -> throwAsUncheckedException(new IOException()));
	}

	@Test
	void throwAsUncheckedExceptionWithUncheckedException() {
		assertThrows(RuntimeException.class, () -> throwAsUncheckedException(new NumberFormatException()));
	}

	@Test
	void readStackTraceForNullThrowable() {
		assertThrows(PreconditionViolationException.class, () -> readStackTrace(null));
	}

	@Test
	void readStackTraceForLocalJUnitException() {
		try {
			throw new JUnitException("expected");
		}
		catch (JUnitException e) {
			// @formatter:off
			assertThat(readStackTrace(e))
				.startsWith(JUnitException.class.getName() + ": expected")
				.contains("at " + ExceptionUtilsTests.class.getName());
			// @formatter:on
		}
	}

}
