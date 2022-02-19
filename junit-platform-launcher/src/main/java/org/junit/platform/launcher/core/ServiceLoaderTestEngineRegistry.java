/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.engine.TestEngine;

/**
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0", consumers = "org.junit.platform.suite.engine")
public final class ServiceLoaderTestEngineRegistry {

	public ServiceLoaderTestEngineRegistry() {

	}

	private static final Logger logger = LoggerFactory.getLogger(ServiceLoaderTestEngineRegistry.class);

	public Iterable<TestEngine> loadTestEngines() {
		Iterable<TestEngine> testEngines = ServiceLoader.load(TestEngine.class,
			ClassLoaderUtils.getDefaultClassLoader());

		if (testEngines.iterator().hasNext()) {
			logger.config(() -> createDiscoveredTestEnginesMessage(testEngines));
		}
		else {
			logger.warn(() -> "No TestEngine implementation discovered.");
		}
		return testEngines;
	}

	@SuppressWarnings("unchecked")
	private String createDiscoveredTestEnginesMessage(Iterable<TestEngine> testEngines) {
		// @formatter:off
		List<String> details = ((Stream<TestEngine>) CollectionUtils.toStream(testEngines))
				.map(engine -> String.format("%s (%s)", engine.getId(), join(", ", computeAttributes(engine))))
				.collect(toList());
		// @formatter:on
		return "Discovered TestEngines with IDs: [" + join(", ", details) + "]";
	}

	private List<String> computeAttributes(TestEngine engine) {
		List<String> attributes = new ArrayList<>(4);
		engine.getGroupId().ifPresent(groupId -> attributes.add("group ID: " + groupId));
		engine.getArtifactId().ifPresent(artifactId -> attributes.add("artifact ID: " + artifactId));
		engine.getVersion().ifPresent(version -> attributes.add("version: " + version));
		ClassLoaderUtils.getLocation(engine).ifPresent(location -> attributes.add("location: " + location));
		return attributes;
	}

}
