/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.Events;

/**
 * @since 5.9
 */
class TempDirectoryParameterResolverTests extends AbstractJupiterTestEngineTests {

	@Test
	@DisplayName("Test good and bad @TempDir parameters")
	void testTempDirType() {
		EngineExecutionResults executionResults = executeTestsForClass(ATestCase.class);
		Events tests = executionResults.testEvents();
		tests.assertStatistics(stats -> stats.started(2).failed(1).succeeded(1));
		tests.succeeded().assertEventsMatchExactly(event(test("testGoodTempDirType"), finishedSuccessfully()));
		tests.failed().assertEventsMatchExactly(event(test("testBadTempDirType"),
			finishedWithFailure(instanceOf(ParameterResolutionException.class), message(
				"Failed to resolve parameter [java.lang.String badTempDir] in method [void org.junit.jupiter.engine.extension.TempDirectoryParameterResolverTests$ATestCase.testBadTempDirType(java.lang.String)]: Can only resolve @TempDir parameter of type java.nio.file.Path or java.io.File but was: java.lang.String"))));
	}

	// -------------------------------------------------------------------

	static class ATestCase {

		@Test
		void testGoodTempDirType(@TempDir File goodTempDir) {
		}

		@Test
		void testBadTempDirType(@TempDir String badTempDir) {
		}

	}

}
