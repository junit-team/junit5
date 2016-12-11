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
import java.util.logging.Logger;

import org.junit.platform.commons.util.PackageUtils;
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
		LOG.info(() -> createDiscoveredTestEnginesMessage(testEngines));
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
		attributes.add("version: " + engine.getVersion());
		computeArtifactId(engine).ifPresent(id -> attributes.add("artifact ID: " + id));
		computeGroupId(engine).ifPresent(id -> attributes.add("group ID: " + id));
		return attributes;
	}

	private Optional<String> computeArtifactId(TestEngine engine) {
		return PackageUtils.getAttribute(engine.getClass(), Package::getImplementationTitle);
	}

	private Optional<String> computeGroupId(TestEngine engine) {
		return Optional.empty();
	}

}
