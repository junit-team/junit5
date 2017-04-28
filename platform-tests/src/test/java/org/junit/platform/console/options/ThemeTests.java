/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.options;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;

class ThemeTests {

	@Test
	void givenUtf8ShouldReturnUnicode() {
		assertEquals(Theme.valueOf(StandardCharsets.UTF_8), Theme.UNICODE);
	}

	@Test
	void givenAnythingElseShouldReturnAscii() {
		assertAll("All character sets that are not UTF-8 should return Theme.ASCII", () -> {
			assertEquals(Theme.valueOf(StandardCharsets.ISO_8859_1), Theme.ASCII);
			assertEquals(Theme.valueOf(StandardCharsets.US_ASCII), Theme.ASCII);
			assertEquals(Theme.valueOf(StandardCharsets.UTF_16), Theme.ASCII);
		});
	}

	@Test
	void givenSuccessfulTestExecutionResultShouldReturnAsciiSuccessfulElement() {
		TestExecutionResult successful = TestExecutionResult.successful();
		assertEquals(Theme.ASCII.successful(), Theme.ASCII.status(successful));
	}

	@Test
	void givenSuccessfulTestExecutionResultShouldReturnUnicodeSuccessfulElement() {
		TestExecutionResult successful = TestExecutionResult.successful();
		assertEquals(Theme.UNICODE.successful(), Theme.UNICODE.status(successful));
	}

	@Test
	void givenAbortedTestExecutionResultShouldReturnAsciiAbortedElement() {
		TestExecutionResult aborted = TestExecutionResult.aborted(new Throwable());
		assertEquals(Theme.ASCII.aborted(), Theme.ASCII.status(aborted));
	}

	@Test
	void givenAbortedTestExecutionResultShouldReturnUnicodeAbortedElement() {
		TestExecutionResult aborted = TestExecutionResult.aborted(new Throwable());
		assertEquals(Theme.UNICODE.aborted(), Theme.UNICODE.status(aborted));
	}

	@Test
	void givenFailedTestExecutionResultShouldReturnAsciiFailedElement() {
		TestExecutionResult failed = TestExecutionResult.failed(new Throwable());
		assertEquals(Theme.ASCII.failed(), Theme.ASCII.status(failed));
	}

	@Test
	void givenFailedTestExecutionResultShouldReturnUnicodeFailedElement() {
		TestExecutionResult failed = TestExecutionResult.failed(new Throwable());
		assertEquals(Theme.UNICODE.failed(), Theme.UNICODE.status(failed));
	}
}
