/*
 * Copyright 2015-2017 the original author or authors.
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
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.engine.TestEngine;

/**
 * @since 1.0
 */
class ServiceLoaderTestEngineRegistry {

	private static final Logger logger = Logger.getLogger(ServiceLoaderTestEngineRegistry.class.getName());

	public Iterable<TestEngine> loadTestEngines() {
		Iterable<TestEngine> testEngines = ServiceLoader.load(TestEngine.class,
			ClassLoaderUtils.getDefaultClassLoader());
		logger.config(() -> createDiscoveredTestEnginesMessage(testEngines));
		return testEngines;
	}

	private String createDiscoveredTestEnginesMessage(Iterable<TestEngine> testEngines) {
		List<String> details = new ArrayList<>();
		for (TestEngine engine : testEngines) {
			details.add(String.format("%s (%s)", engine.getId(), String.join(", ", computeAttributes(engine))));
		}
		if (details.isEmpty()) {
			return "No TestEngine implementation discovered.";
		}
		return "Discovered TestEngines with IDs: [" + String.join(", ", details) + "]";
	}

	private List<String> computeAttributes(TestEngine engine) {
		List<String> attributes = new ArrayList<>();
		engine.getGroupId().ifPresent(groupId -> attributes.add("group ID: " + groupId));
		engine.getArtifactId().ifPresent(artifactId -> attributes.add("artifact ID: " + artifactId));
		engine.getVersion().ifPresent(version -> attributes.add("version: " + version));
		ClassLoaderUtils.getLocation(engine).ifPresent(location -> attributes.add("location: " + location));
		return attributes;
	}

}
