/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;

/**
 * {@code EngineTestKit} provides support for executing a test plan for a given
 * {@link TestEngine} and then accessing the results via
 * {@linkplain EngineExecutionResults a fluent API} to verify the expected results.
 *
 * @since 1.4
 * @see #execute(String, EngineDiscoveryRequest)
 * @see #execute(TestEngine, EngineDiscoveryRequest)
 * @see EngineExecutionResults
 */
@API(status = EXPERIMENTAL, since = "1.4")
public final class EngineTestKit {

	private static final Logger logger = LoggerFactory.getLogger(EngineTestKit.class);

	/**
	 * Execute tests for a given {@link EngineDiscoveryRequest} using the
	 * {@link TestEngine} with the provided ID.
	 *
	 * <p>The {@code TestEngine} will be loaded via Java's {@link ServiceLoader}
	 * mechanism, analogous to the manner in which test engines are loaded in
	 * the JUnit Platform Launcher API.
	 *
	 * <p>Note that {@link org.junit.platform.launcher.LauncherDiscoveryRequest}
	 * from the {@code junit-platform-launcher} module is a subtype of
	 * {@code EngineDiscoveryRequest}. It is therefore quite convenient to make
	 * use of the DSL provided in
	 * {@link org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder}
	 * to build an appropriate discovery request to supply to this method.
	 *
	 * @param engineId the ID of the {@code TestEngine} to use; must not be
	 * {@code null} or <em>blank</em>
	 * @param discoveryRequest the {@code EngineDiscoveryRequest} to use
	 * @return the results of the execution
	 * @throws PreconditionViolationException for invalid arguments or if the
	 * {@code TestEngine} with the supplied ID cannot be loaded
	 * @see #execute(TestEngine, EngineDiscoveryRequest)
	 */
	public static EngineExecutionResults execute(String engineId, EngineDiscoveryRequest discoveryRequest)
			throws PreconditionViolationException {

		Preconditions.notBlank(engineId, "TestEngine ID must not be null or blank");

		return execute(loadTestEngine(engineId.trim()), discoveryRequest);
	}

	/**
	 * Execute tests for a given {@link EngineDiscoveryRequest} using the
	 * provided {@link TestEngine}.
	 *
	 * <p>Note that {@link org.junit.platform.launcher.LauncherDiscoveryRequest}
	 * from the {@code junit-platform-launcher} module is a subtype of
	 * {@code EngineDiscoveryRequest}. It is therefore quite convenient to make
	 * use of the DSL provided in
	 * {@link org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder}
	 * to build an appropriate discovery request to supply to this method.
	 *
	 * @param testEngine the {@code TestEngine} to use; must not be {@code null}
	 * @param discoveryRequest the {@code EngineDiscoveryRequest} to use; must
	 * not be {@code null}
	 * @return the recorded {@code EngineExecutionResults}
	 * @throws PreconditionViolationException for invalid arguments
	 * @see #execute(String, EngineDiscoveryRequest)
	 */
	public static EngineExecutionResults execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest)
			throws PreconditionViolationException {

		Preconditions.notNull(testEngine, "TestEngine must not be null");
		Preconditions.notNull(discoveryRequest, "EngineDiscoveryRequest must not be null");

		ExecutionRecorder executionRecorder = new ExecutionRecorder();
		execute(testEngine, discoveryRequest, executionRecorder);
		return executionRecorder.getExecutionResults();
	}

	private static void execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest,
			EngineExecutionListener listener) {

		UniqueId engineUniqueId = UniqueId.forEngine(testEngine.getId());
		TestDescriptor engineTestDescriptor = testEngine.discover(discoveryRequest, engineUniqueId);
		ExecutionRequest request = new ExecutionRequest(engineTestDescriptor, listener,
			discoveryRequest.getConfigurationParameters());
		testEngine.execute(request);
	}

	private static TestEngine loadTestEngine(String engineId) {
		return stream((loadTestEngines()).spliterator(), false)//
				.filter(engine -> engineId.equals(engine.getId()))//
				.findFirst()//
				.orElseThrow(() -> new PreconditionViolationException(
					String.format("Failed to load TestEngine with ID [%s]", engineId)));
	}

	private static Iterable<TestEngine> loadTestEngines() {
		ClassLoader defaultClassLoader = ClassLoaderUtils.getDefaultClassLoader();
		Iterable<TestEngine> testEngines = ServiceLoader.load(TestEngine.class, defaultClassLoader);
		logger.config(() -> createDiscoveredTestEnginesMessage(testEngines));
		return testEngines;
	}

	@SuppressWarnings("unchecked")
	private static String createDiscoveredTestEnginesMessage(Iterable<TestEngine> testEngines) {
		// @formatter:off
		List<String> details = ((Stream<TestEngine>) CollectionUtils.toStream(testEngines))
				.map(engine -> String.format("%s (%s)", engine.getId(), join(", ", computeAttributes(engine))))
				.collect(toList());

		return details.isEmpty()
				? "No TestEngine implementation discovered."
				: "Discovered TestEngines with IDs: [" + join(", ", details) + "]";
		// @formatter:on
	}

	private static List<String> computeAttributes(TestEngine engine) {
		List<String> attributes = new ArrayList<>(4);
		engine.getGroupId().ifPresent(groupId -> attributes.add("group ID: " + groupId));
		engine.getArtifactId().ifPresent(artifactId -> attributes.add("artifact ID: " + artifactId));
		engine.getVersion().ifPresent(version -> attributes.add("version: " + version));
		ClassLoaderUtils.getLocation(engine).ifPresent(location -> attributes.add("location: " + location));
		return attributes;
	}

	private EngineTestKit() {
		/* no-op */
	}

}
