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

import org.junit.gen5.engine.TestEngine;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
class TestEngineRegistry {

	private static Iterable<TestEngine> testEngines;

	static Iterable<TestEngine> lookupAllTestEngines() {
		if (testEngines == null) {
			testEngines = ServiceLoader.load(TestEngine.class);
		}
		return testEngines;
	}

}
