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
import static org.junit.gen5.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.JUnitException;

class ExceptionUtilsTests {

	@Test
	void throwAsUncheckedExceptionForNullThrowable() {
		assertThrows(PreconditionViolationException.class, () -> ExceptionUtils.throwAsUncheckedException(null));
	}

	@Test
	void throwAsUncheckedException() {
		assertThrows(IOException.class, (ExceptionUtilsTests::throwIOExceptionAsUnchecked));
	}

	@Test
	void readStackTraceForNullThrowable() {
		assertThrows(PreconditionViolationException.class, () -> ExceptionUtils.readStackTrace(null));
	}

	@Test
	void readStackTrace() {
		try {
			throw new JUnitException("expected");
		}
		catch (JUnitException e) {
			String stackTrace = ExceptionUtils.readStackTrace(e);
			// @formatter:off
			assertThat(stackTrace)
				.startsWith(JUnitException.class.getName() + ": expected")
				.contains("at " + ExceptionUtilsTests.class.getName());
			// @formatter:on
		}
	}

	private static void throwIOExceptionAsUnchecked() {
		ExceptionUtils.throwAsUncheckedException(new IOException());
	}
}
