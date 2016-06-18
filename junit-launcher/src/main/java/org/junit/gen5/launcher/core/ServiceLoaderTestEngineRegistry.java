/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.core;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestEngine;

/**
 * @since 5.0
 */
class ServiceLoaderTestEngineRegistry {

	private static final Logger LOG = Logger.getLogger(ServiceLoaderTestEngineRegistry.class.getName());

	public Iterable<TestEngine> loadTestEngines() {
		Iterable<TestEngine> testEngines = ServiceLoader.load(TestEngine.class,
			ReflectionUtils.getDefaultClassLoader());
		LOG.info(() -> "Discovered TestEngines with IDs: "
				+ stream(testEngines.spliterator(), false).map(TestEngine::getId).collect(toList()));
		return testEngines;
	}

}
