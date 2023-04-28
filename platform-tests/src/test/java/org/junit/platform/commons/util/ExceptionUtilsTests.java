/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.commons.util.ExceptionUtils.readStackTrace;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.opentest4j.AssertionFailedError;

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

	@Test
	void pruneStackTraceOfCallsFromSpecificPackage() {
		try {
			throw new JUnitException("expected");
		}
		catch (JUnitException e) {
			ExceptionUtils.pruneStackTrace(e, element -> !element.startsWith("org.junit."));
			assertThat(e.getStackTrace()) //
					.noneMatch(element -> element.toString().contains("org.junit."));
		}
	}

	@Test
	void pruneStackTraceOfEverythingExceptJupiterAssertions() {
		try {
			Assertions.fail();
		}
		catch (AssertionFailedError e) {
			ExceptionUtils.pruneStackTrace(e, element -> false);
			assertStackTraceMatch(e.getStackTrace(), "\\Qorg.junit.jupiter.api.Assertions.fail(Assertions.java:\\E.+");
		}
	}

	@Test
	void pruneStackTraceOfEverythingExceptJupiterAssumptions() {
		try {
			Assumptions.assumeTrue(() -> {
				throw new JUnitException("expected");
			});
		}
		catch (JUnitException e) {
			ExceptionUtils.pruneStackTrace(e, element -> false);
			assertStackTraceMatch(e.getStackTrace(),
				"\\Qorg.junit.jupiter.api.Assumptions.assumeTrue(Assumptions.java:\\E.+");
		}
	}

	@Test
	void pruneStackTraceOfAllLauncherCalls() {
		try {
			throw new JUnitException("expected");
		}
		catch (JUnitException e) {
			ExceptionUtils.pruneStackTrace(e, element -> true);
			assertThat(e.getStackTrace()) //
					.noneMatch(element -> element.toString().contains("org.junit.platform.launcher."));
		}
	}

	@Test
	void pruneStackTraceOfEverythingPriorToFirstLauncherCall() {
		try {
			throw new JUnitException("expected");
		}
		catch (JUnitException e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			stackTrace[stackTrace.length - 1] = new StackTraceElement("org.example.Class", "method", "file", 123);
			e.setStackTrace(stackTrace);

			ExceptionUtils.pruneStackTrace(e, element -> true);
			assertThat(e.getStackTrace()) //
					.noneMatch(element -> element.toString().contains("org.example.Class.method(file:123)"));
		}
	}

	private static void assertStackTraceMatch(StackTraceElement[] stackTrace, String expectedLines) {
		List<String> stackStraceAsLines = Arrays.stream(stackTrace) //
				.map(StackTraceElement::toString) //
				.collect(Collectors.toList());
		assertLinesMatch(expectedLines.lines().toList(), stackStraceAsLines);
	}

}
