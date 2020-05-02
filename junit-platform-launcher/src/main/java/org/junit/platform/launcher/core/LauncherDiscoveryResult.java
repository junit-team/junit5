/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.Collections.unmodifiableMap;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;

/**
 * Represents the result of test discovery of the configured
 * {@linkplain TestEngine test engines}.
 *
 * @since 1.7
 */
@API(status = INTERNAL, since = "1.7", consumers = "testkit")
public class LauncherDiscoveryResult {

	private final Map<TestEngine, TestDescriptor> testEngineDescriptors;
	private final ConfigurationParameters configurationParameters;

	LauncherDiscoveryResult(Map<TestEngine, TestDescriptor> testEngineDescriptors,
			ConfigurationParameters configurationParameters) {
		this.testEngineDescriptors = unmodifiableMap(new LinkedHashMap<>(testEngineDescriptors));
		this.configurationParameters = configurationParameters;
	}

	public TestDescriptor getEngineTestDescriptor(TestEngine testEngine) {
		return this.testEngineDescriptors.get(testEngine);
	}

	ConfigurationParameters getConfigurationParameters() {
		return configurationParameters;
	}

	Collection<TestEngine> getTestEngines() {
		return this.testEngineDescriptors.keySet();
	}

	Collection<TestDescriptor> getEngineTestDescriptors() {
		return this.testEngineDescriptors.values();
	}

}
