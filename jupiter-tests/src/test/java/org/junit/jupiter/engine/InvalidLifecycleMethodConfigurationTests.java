/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Events;

/**
 * Integration tests that verify proper handling of invalid configuration for
 * lifecycle methods in conjunction with the {@link JupiterTestEngine}.
 *
 * <p>In general, configuration errors should not be thrown until the
 * execution phase, thereby giving all containers a chance to execute.
 *
 * @since 5.0
 */
class InvalidLifecycleMethodConfigurationTests extends AbstractJupiterTestEngineTests {

	@Test
	void executeValidTestCaseAlongsideTestCaseWithInvalidNonStaticBeforeAllDeclaration() {
		assertContainerFailed(TestCaseWithInvalidNonStaticBeforeAllMethod.class);
	}

	@Test
	void executeValidTestCaseAlongsideTestCaseWithInvalidNonStaticAfterAllDeclaration() {
		assertContainerFailed(TestCaseWithInvalidNonStaticAfterAllMethod.class);
	}

	@Test
	void executeValidTestCaseAlongsideTestCaseWithInvalidStaticBeforeEachDeclaration() {
		assertContainerFailed(TestCaseWithInvalidStaticBeforeEachMethod.class);
	}

	@Test
	void executeValidTestCaseAlongsideTestCaseWithInvalidStaticAfterEachDeclaration() {
		assertContainerFailed(TestCaseWithInvalidStaticAfterEachMethod.class);
	}

	private void assertContainerFailed(Class<?> invalidTestClass) {
		EngineExecutionResults executionResults = executeTests(selectClass(TestCase.class),
			selectClass(invalidTestClass));
		Events containers = executionResults.containerEvents();
		Events tests = executionResults.testEvents();

		// @formatter:off
		assertAll(
			() -> assertEquals(3, containers.started().count(), "# containers started"),
			() -> assertEquals(1, tests.started().count(), "# tests started"),
			() -> assertEquals(1, tests.succeeded().count(), "# tests succeeded"),
			() -> assertEquals(0, tests.failed().count(), "# tests failed"),
			() -> assertEquals(3, containers.finished().count(), "# containers finished"),
			() -> assertEquals(1, containers.failed().count(), "# containers failed")
		);
		// @formatter:on
	}

	// -------------------------------------------------------------------------

	static class TestCase {

		@Test
		void test() {
		}
	}

	static class TestCaseWithInvalidNonStaticBeforeAllMethod {

		// must be static
		@BeforeAll
		void beforeAll() {
		}

		@Test
		void test() {
		}
	}

	static class TestCaseWithInvalidNonStaticAfterAllMethod {

		// must be static
		@AfterAll
		void afterAll() {
		}

		@Test
		void test() {
		}
	}

	static class TestCaseWithInvalidStaticBeforeEachMethod {

		// must NOT be static
		@BeforeEach
		static void beforeEach() {
		}

		@Test
		void test() {
		}
	}

	static class TestCaseWithInvalidStaticAfterEachMethod {

		// must NOT be static
		@AfterEach
		static void afterEach() {
		}

		@Test
		void test() {
		}
	}

}
