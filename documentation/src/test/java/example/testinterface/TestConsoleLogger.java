/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.testinterface;

import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

//tag::user_guide[]
public interface TestConsoleLogger {

	public static final Logger LOG = Logger.getLogger(TestConsoleLogger.class.getName());

	@BeforeAll
	static void beforeAllTest() {
		LOG.info(() -> "beforeAllTest");
	}

	@AfterAll
	static void afterAllTest() {
		LOG.info(() -> "afterAllTest");
	}

	@BeforeEach
	default void beforeEachTest(TestInfo testInfo) {
		LOG.info(() -> String.format("About to execute [%s]", testInfo.getTestMethod().get().getName()));
	}

	@AfterEach
	default void afterEachTest(TestInfo testInfo) {
		LOG.info(() -> String.format("Finished executing [%s]", testInfo.getTestMethod().get().getName()));
	}

}
//end::user_guide[]
