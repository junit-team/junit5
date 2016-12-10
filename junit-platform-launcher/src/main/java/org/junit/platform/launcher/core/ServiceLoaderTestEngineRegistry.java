/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.TestEngine;

/**
 * @since 1.0
 */
class ServiceLoaderTestEngineRegistry {

	private static final Logger LOG = Logger.getLogger(ServiceLoaderTestEngineRegistry.class.getName());

	public Iterable<TestEngine> loadTestEngines() {
		Iterable<TestEngine> testEngines = ServiceLoader.load(TestEngine.class,
			ReflectionUtils.getDefaultClassLoader());
		logDiscoveredTestEngines(Level.INFO, testEngines);
		return testEngines;
	}

	private void logDiscoveredTestEngines(Level level, Iterable<TestEngine> testEngines) {
		if (!LOG.isLoggable(level)) {
			return;
		}
		List<String> details = new ArrayList<>();
		for (TestEngine engine : testEngines) {
		 	details.add(engine.getId() + " (" + String.join(", ", information(engine)) + ")");
		}
		LOG.log(level, "Discovered TestEngines with IDs: [" + String.join(", ", details) + "]");
	}

	private List<String> information(TestEngine engine) {
		List<String> information = new ArrayList<>();
		information.add(computeVersion(engine));
		computeArtifactId(engine).ifPresent(information::add);
		computeGroupId(engine).ifPresent(information::add);
		return information;
	}

	private String computeVersion(TestEngine engine) {
		return "version: " + engine.getVersion();
	}

	private Optional<String> computeArtifactId(TestEngine engine) {
		Package implementationPackage = engine.getClass().getPackage();
		if (implementationPackage != null) {
			String implementationTitle = implementationPackage.getImplementationTitle();
			if (implementationTitle != null) {
				return Optional.of("artifact ID: " + implementationTitle);
			}
		}
		return Optional.empty();
	}

	private Optional<String> computeGroupId(TestEngine engine) {
		return Optional.empty();
	}

}
