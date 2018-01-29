/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
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

// tag::user_guide[]
@TestInstance(Lifecycle.PER_CLASS)
interface TestLifecycleLogger {

	static final Logger LOG = Logger.getLogger(TestLifecycleLogger.class.getName());

	@BeforeAll
	default void beforeAllTests() {
		LOG.info("Before all tests");
	}

	@AfterAll
	default void afterAllTests() {
		LOG.info("After all tests");
	}

	@BeforeEach
	default void beforeEachTest(TestInfo testInfo) {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		LOG.info(() -> String.format("About to execute [%s]",
			testInfo.getDisplayName()));
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

	@AfterEach
	default void afterEachTest(TestInfo testInfo) {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		LOG.info(() -> String.format("Finished executing [%s]",
			testInfo.getDisplayName()));
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

}
// end::user_guide[]
