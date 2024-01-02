/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.HashSet;
import java.util.Set;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestEngine;

/**
 * @since 1.7
 */
class EngineIdValidator {

	private EngineIdValidator() {
	}

	static Iterable<TestEngine> validate(Iterable<TestEngine> testEngines) {
		Set<String> ids = new HashSet<>();
		for (TestEngine testEngine : testEngines) {
			// check usage of reserved ID prefix
			if (!validateReservedIds(testEngine)) {
				getLogger().warn(() -> String.format(
					"Third-party TestEngine implementations are forbidden to use the reserved 'junit-' prefix for their ID: '%s'",
					testEngine.getId()));
			}

			// check uniqueness
			if (!ids.add(testEngine.getId())) {
				throw new JUnitException(String.format(
					"Cannot create Launcher for multiple engines with the same ID '%s'.", testEngine.getId()));
			}
		}
		return testEngines;
	}

	private static Logger getLogger() {
		// Not a constant to avoid problems with building GraalVM native images
		return LoggerFactory.getLogger(EngineIdValidator.class);
	}

	// https://github.com/junit-team/junit5/issues/1557
	private static boolean validateReservedIds(TestEngine testEngine) {
		String engineId = Preconditions.notBlank(testEngine.getId(),
			() -> String.format("ID for TestEngine [%s] must not be null or blank", testEngine.getClass().getName()));
		if (!engineId.startsWith("junit-")) {
			return true;
		}
		switch (engineId) {
			case "junit-jupiter": {
				validateWellKnownClassName(testEngine, "org.junit.jupiter.engine.JupiterTestEngine");
				return true;
			}
			case "junit-vintage": {
				validateWellKnownClassName(testEngine, "org.junit.vintage.engine.VintageTestEngine");
				return true;
			}
			case "junit-platform-suite": {
				validateWellKnownClassName(testEngine, "org.junit.platform.suite.engine.SuiteTestEngine");
				return true;
			}
			default:
				return false;
		}
	}

	private static void validateWellKnownClassName(TestEngine testEngine, String expectedClassName) {
		String actualClassName = testEngine.getClass().getName();
		if (actualClassName.equals(expectedClassName)) {
			return;
		}
		throw new JUnitException(
			String.format("Third-party TestEngine '%s' is forbidden to use the reserved '%s' TestEngine ID.",
				actualClassName, testEngine.getId()));
	}

}
