/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.commons.util.ExceptionUtils.readStackTrace;
import static org.junit.gen5.commons.util.ExceptionUtils.throwAsUncheckedException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.gen5.commons.JUnitException;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ExceptionUtils}.
 *
 * @since 5.0
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
