/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.testinterface;

import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

// @formatter:off
// tag::user_guide[]
@TestInstance(Lifecycle.PER_CLASS)
interface TestLifecycleLogger {

	static final Logger logger = Logger.getLogger(TestLifecycleLogger.class.getName());

	@BeforeAll
	default void beforeAllTests() {
		logger.info("Before all tests");
	}

	@AfterAll
	default void afterAllTests() {
		logger.info("After all tests");
	}

	@BeforeEach
	default void beforeEachTest(TestInfo testInfo) {
		logger.info(() -> String.format("About to execute [%s]",
			testInfo.getDisplayName()));
	}

	@AfterEach
	default void afterEachTest(TestInfo testInfo) {
		logger.info(() -> String.format("Finished executing [%s]",
			testInfo.getDisplayName()));
	}

}
// end::user_guide[]
// @formatter:on
