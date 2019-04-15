/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit.engine;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.DiscoveryFilter;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

/**
 * {@code EngineTestKit} provides support for executing a test plan for a given
 * {@link TestEngine} and then accessing the results via
 * {@linkplain EngineExecutionResults a fluent API} to verify the expected results.
 *
 * @since 1.4
 * @see #engine(String)
 * @see #engine(TestEngine)
 * @see #execute(String, EngineDiscoveryRequest)
 * @see #execute(TestEngine, EngineDiscoveryRequest)
 * @see EngineExecutionResults
 */
@API(status = EXPERIMENTAL, since = "1.4")
public final class EngineTestKit {

	private static final Logger logger = LoggerFactory.getLogger(EngineTestKit.class);

	/**
	 * Create an execution {@link Builder} for the {@link TestEngine} with the
	 * supplied ID.
	 *
	 * <p>The {@code TestEngine} will be loaded via Java's {@link ServiceLoader}
	 * mechanism, analogous to the manner in which test engines are loaded in
	 * the JUnit Platform Launcher API.
	 *
	 * <h3>Example Usage</h3>
	 *
	 * <pre class="code">
	 * EngineTestKit
	 *     .engine("junit-jupiter")
	 *     .selectors(selectClass(MyTests.class))
	 *     .execute()
	 *     .tests()
	 *     .assertStatistics(stats -&gt; stats.started(2).finished(2));
	 * </pre>
	 *
	 * @param engineId the ID of the {@code TestEngine} to use; must not be
	 * {@code null} or <em>blank</em>
	 * @return the engine execution {@code Builder}
	 * @throws PreconditionViolationException if the supplied ID is {@code null}
	 * or <em>blank</em>, or if the {@code TestEngine} with the supplied ID
	 * cannot be loaded
	 * @see #engine(TestEngine)
	 * @see #execute(String, EngineDiscoveryRequest)
	 * @see #execute(TestEngine, EngineDiscoveryRequest)
	 */
	public static Builder engine(String engineId) {
		Preconditions.notBlank(engineId, "TestEngine ID must not be null or blank");
		return engine(loadTestEngine(engineId.trim()));
	}

	/**
	 * Create an execution {@link Builder} for the supplied {@link TestEngine}.
	 *
	 * <h3>Example Usage</h3>
	 *
	 * <pre class="code">
	 * EngineTestKit
	 *     .engine(new MyTestEngine())
	 *     .selectors(selectClass(MyTests.class))
	 *     .execute()
	 *     .tests()
	 *     .assertStatistics(stats -&gt; stats.started(2).finished(2));
	 * </pre>
	 *
	 * @param testEngine the {@code TestEngine} to use; must not be {@code null}
	 * @return the engine execution {@code Builder}
	 * @throws PreconditionViolationException if the {@code TestEngine} is
	 * {@code null}
	 * @see #engine(String)
	 * @see #execute(String, EngineDiscoveryRequest)
	 * @see #execute(TestEngine, EngineDiscoveryRequest)
	 */
	public static Builder engine(TestEngine testEngine) {
		Preconditions.notNull(testEngine, "TestEngine must not be null");
		return new Builder(testEngine);
	}

	/**
	 * Execute tests for the given {@link EngineDiscoveryRequest} using the
	 * {@link TestEngine} with the supplied ID.
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
	 * to build an appropriate discovery request to supply to this method. As
	 * an alternative, consider using {@link #engine(String)} for a more fluent
	 * API.
	 *
	 * @param engineId the ID of the {@code TestEngine} to use; must not be
	 * {@code null} or <em>blank</em>
	 * @param discoveryRequest the {@code EngineDiscoveryRequest} to use
	 * @return the results of the execution
	 * @throws PreconditionViolationException for invalid arguments or if the
	 * {@code TestEngine} with the supplied ID cannot be loaded
	 * @see #execute(TestEngine, EngineDiscoveryRequest)
	 * @see #engine(String)
	 * @see #engine(TestEngine)
	 */
	public static EngineExecutionResults execute(String engineId, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notBlank(engineId, "TestEngine ID must not be null or blank");
		return execute(loadTestEngine(engineId.trim()), discoveryRequest);
	}

	/**
	 * Execute tests for the given {@link EngineDiscoveryRequest} using the
	 * supplied {@link TestEngine}.
	 *
	 * <p>Note that {@link org.junit.platform.launcher.LauncherDiscoveryRequest}
	 * from the {@code junit-platform-launcher} module is a subtype of
	 * {@code EngineDiscoveryRequest}. It is therefore quite convenient to make
	 * use of the DSL provided in
	 * {@link org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder}
	 * to build an appropriate discovery request to supply to this method. As
	 * an alternative, consider using {@link #engine(TestEngine)} for a more fluent
	 * API.
	 *
	 * @param testEngine the {@code TestEngine} to use; must not be {@code null}
	 * @param discoveryRequest the {@code EngineDiscoveryRequest} to use; must
	 * not be {@code null}
	 * @return the recorded {@code EngineExecutionResults}
	 * @throws PreconditionViolationException for invalid arguments
	 * @see #execute(String, EngineDiscoveryRequest)
	 * @see #engine(String)
	 * @see #engine(TestEngine)
	 */
	public static EngineExecutionResults execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest) {
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

	// -------------------------------------------------------------------------

	/**
	 * {@link TestEngine} execution builder.
	 *
	 * <p>See {@link EngineTestKit#engine(String)} and
	 * {@link EngineTestKit#engine(TestEngine)} for example usage.
	 *
	 * @since 1.4
	 * @see #selectors(DiscoverySelector...)
	 * @see #filters(DiscoveryFilter...)
	 * @see #configurationParameter(String, String)
	 * @see #configurationParameters(Map)
	 * @see #execute()
	 */
	public static final class Builder {

		private final LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request();
		private final TestEngine testEngine;

		private Builder(TestEngine testEngine) {
			this.testEngine = testEngine;
		}

		/**
		 * Add all of the supplied {@linkplain DiscoverySelector discovery selectors}.
		 *
		 * <p>Built-in discovery selectors can be created via the static factory
		 * methods in {@link org.junit.platform.engine.discovery.DiscoverySelectors}.
		 *
		 * @param selectors the discovery selectors to add; never {@code null}
		 * @return this builder for method chaining
		 * @see #filters(DiscoveryFilter...)
		 * @see #configurationParameter(String, String)
		 * @see #configurationParameters(Map)
		 * @see #execute()
		 */
		public Builder selectors(DiscoverySelector... selectors) {
			this.requestBuilder.selectors(selectors);
			return this;
		}

		/**
		 * Add all of the supplied {@linkplain DiscoveryFilter discovery filters}.
		 *
		 * <p>Built-in discovery filters can be created via the static factory
		 * methods in {@link org.junit.platform.engine.discovery.ClassNameFilter}
		 * and {@link org.junit.platform.engine.discovery.PackageNameFilter}.
		 *
		 * @param filters the discovery filters to add; never {@code null}
		 * @return this builder for method chaining
		 * @see #selectors(DiscoverySelector...)
		 * @see #configurationParameter(String, String)
		 * @see #configurationParameters(Map)
		 * @see #execute()
		 */
		public Builder filters(DiscoveryFilter<?>... filters) {
			this.requestBuilder.filters(filters);
			return this;
		}

		/**
		 * Add the supplied <em>configuration parameter</em>.
		 *
		 * @param key the configuration parameter key under which to store the
		 * value; never {@code null} or blank
		 * @param value the value to store
		 * @return this builder for method chaining
		 * @see #selectors(DiscoverySelector...)
		 * @see #filters(DiscoveryFilter...)
		 * @see #configurationParameters(Map)
		 * @see #execute()
		 * @see org.junit.platform.engine.ConfigurationParameters
		 */
		public Builder configurationParameter(String key, String value) {
			this.requestBuilder.configurationParameter(key, value);
			return this;
		}

		/**
		 * Add all of the supplied <em>configuration parameters</em>.
		 *
		 * @param configurationParameters the map of configuration parameters to add;
		 * never {@code null}
		 * @return this builder for method chaining
		 * @see #selectors(DiscoverySelector...)
		 * @see #filters(DiscoveryFilter...)
		 * @see #configurationParameter(String, String)
		 * @see #execute()
		 * @see org.junit.platform.engine.ConfigurationParameters
		 */
		public Builder configurationParameters(Map<String, String> configurationParameters) {
			this.requestBuilder.configurationParameters(configurationParameters);
			return this;
		}

		/**
		 * Execute tests for the configured {@link TestEngine},
		 * {@linkplain DiscoverySelector discovery selectors},
		 * {@linkplain DiscoveryFilter discovery filters}, and
		 * <em>configuration parameters</em>.
		 *
		 * @return the recorded {@code EngineExecutionResults}
		 * @see #selectors(DiscoverySelector...)
		 * @see #filters(DiscoveryFilter...)
		 * @see #configurationParameter(String, String)
		 * @see #configurationParameters(Map)
		 */
		public EngineExecutionResults execute() {
			ExecutionRecorder executionRecorder = new ExecutionRecorder();
			EngineTestKit.execute(this.testEngine, this.requestBuilder.build(), executionRecorder);
			return executionRecorder.getExecutionResults();
		}

	}

}
