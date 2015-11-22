/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestEngine;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
class TestEngineRegistry {

	private static final Logger LOG = Logger.getLogger(TestEngineRegistry.class.getName());

	private static Iterable<TestEngine> testEngines;

	static Iterable<TestEngine> lookupAllTestEngines() {
		return lookupAllTestEngines(ReflectionUtils.getDefaultClassLoader());
	}

	static Iterable<TestEngine> lookupAllTestEngines(ClassLoader classLoader) {
		if (testEngines == null) {
			testEngines = ServiceLoader.load(TestEngine.class, classLoader);
			for (TestEngine testEngine : testEngines) {
				LOG.info(() -> String.format("Discovered test engine with id: '%s'", testEngine.getId()));
			}
		}
		return testEngines;
	}

}
