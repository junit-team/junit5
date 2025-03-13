/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apiguardian.api.API;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.reporting.OutputDirectoryProvider;

/**
 * Represents the result of test discovery of the configured
 * {@linkplain TestEngine test engines}.
 *
 * @since 1.7
 */
@API(status = INTERNAL, since = "1.7", consumers = { "org.junit.platform.testkit", "org.junit.platform.suite.engine" })
public class LauncherDiscoveryResult {

	private final Map<TestEngine, EngineResultInfo> testEngineResults;
	private final ConfigurationParameters configurationParameters;
	private final OutputDirectoryProvider outputDirectoryProvider;

	LauncherDiscoveryResult(Map<TestEngine, EngineResultInfo> testEngineResults,
			ConfigurationParameters configurationParameters, OutputDirectoryProvider outputDirectoryProvider) {
		this.testEngineResults = unmodifiableMap(new LinkedHashMap<>(testEngineResults));
		this.configurationParameters = configurationParameters;
		this.outputDirectoryProvider = outputDirectoryProvider;
	}

	public TestDescriptor getEngineTestDescriptor(TestEngine testEngine) {
		return getEngineResult(testEngine).getRootDescriptor();
	}

	EngineResultInfo getEngineResult(TestEngine testEngine) {
		return this.testEngineResults.get(testEngine);
	}

	ConfigurationParameters getConfigurationParameters() {
		return this.configurationParameters;
	}

	OutputDirectoryProvider getOutputDirectoryProvider() {
		return this.outputDirectoryProvider;
	}

	public Collection<TestEngine> getTestEngines() {
		return this.testEngineResults.keySet();
	}

	Collection<TestDescriptor> getEngineTestDescriptors() {
		return this.testEngineResults.values().stream() //
				.map(EngineResultInfo::getRootDescriptor) //
				.collect(Collectors.toList());
	}

	public LauncherDiscoveryResult withRetainedEngines(Predicate<? super TestDescriptor> predicate) {
		Map<TestEngine, EngineResultInfo> prunedTestEngineResults = retainEngines(predicate);
		if (prunedTestEngineResults.size() < this.testEngineResults.size()) {
			return new LauncherDiscoveryResult(prunedTestEngineResults, this.configurationParameters,
				this.outputDirectoryProvider);
		}
		return this;
	}

	private Map<TestEngine, EngineResultInfo> retainEngines(Predicate<? super TestDescriptor> predicate) {
		// @formatter:off
		return this.testEngineResults.entrySet()
				.stream()
				.filter(entry -> predicate.test(entry.getValue().getRootDescriptor()))
				.collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
		// @formatter:on
	}

	static class EngineResultInfo {

		static EngineResultInfo success(TestDescriptor rootDescriptor) {
			return new EngineResultInfo(rootDescriptor, null);
		}

		static EngineResultInfo failure(TestDescriptor rootDescriptor, Throwable failure) {
			return new EngineResultInfo(rootDescriptor, failure);
		}

		private final TestDescriptor rootDescriptor;
		private final Throwable failure;

		EngineResultInfo(TestDescriptor rootDescriptor, Throwable failure) {
			this.rootDescriptor = rootDescriptor;
			this.failure = failure;
		}

		TestDescriptor getRootDescriptor() {
			return this.rootDescriptor;
		}

		Optional<Throwable> getFailure() {
			return Optional.ofNullable(this.failure);
		}
	}

}
