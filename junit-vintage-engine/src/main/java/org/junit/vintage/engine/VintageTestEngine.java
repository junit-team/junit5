/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.engine.TestExecutionResult.successful;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.ENGINE_ID;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.config.PrefixedConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageEngineDescriptor;
import org.junit.vintage.engine.discovery.VintageDiscoverer;
import org.junit.vintage.engine.execution.RunnerExecutor;

/**
 * The JUnit Vintage {@link TestEngine}.
 *
 * @since 4.12
 */
@API(status = INTERNAL, since = "4.12")
public final class VintageTestEngine implements TestEngine {

	private static final String PARALLEL_CONFIG_PREFIX = "junit.jupiter.execution.parallel";
	private static final String PARALLEL_ENABLED_CONFIG = PARALLEL_CONFIG_PREFIX + ".enabled";
	private static final String PARALLEL_CONFIG = PARALLEL_CONFIG_PREFIX + ".config.";

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	/**
	 * Returns {@code org.junit.vintage} as the group ID.
	 */
	@Override
	public Optional<String> getGroupId() {
		return Optional.of("org.junit.vintage");
	}

	/**
	 * Returns {@code junit-vintage-engine} as the artifact ID.
	 */
	@Override
	public Optional<String> getArtifactId() {
		return Optional.of("junit-vintage-engine");
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		JUnit4VersionCheck.checkSupported();
		return new VintageDiscoverer().discover(discoveryRequest, uniqueId);
	}

	@Override
	public void execute(ExecutionRequest request) {
		EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();
		VintageEngineDescriptor engineDescriptor = (VintageEngineDescriptor) request.getRootTestDescriptor();
		engineExecutionListener.executionStarted(engineDescriptor);
		RunnerExecutor runnerExecutor = new RunnerExecutor(engineExecutionListener,
			engineDescriptor.getTestSourceProvider());

		final ConfigurationParameters configurationParameters = request.getConfigurationParameters();
		final boolean parallelExecutionEnabled = configurationParameters.getBoolean(PARALLEL_ENABLED_CONFIG).orElse(
			false);

		try (CloseableExecutor executor = parallelExecutionEnabled ? new ParallelExecutor(configurationParameters)
				: new SerialExecutor()) {
			executeAllChildren(executor, runnerExecutor, engineDescriptor);
		}
		catch (final InterruptedException e) {
			throw new JUnitException("Error executing tests for engine " + getId() + ": " + e.getMessage(), e);
		}

		engineExecutionListener.executionFinished(engineDescriptor, successful());
	}

	private void executeAllChildren(final Executor executor, RunnerExecutor runnerExecutor,
			TestDescriptor engineDescriptor) throws InterruptedException {
		final Collection<? extends TestDescriptor> children = engineDescriptor.getChildren();
		final CountDownLatch latch = new CountDownLatch(children.size());
		// @formatter:off
		children
				.stream()
				.map(RunnerTestDescriptor.class::cast)
				.map(descriptor -> (Runnable)() -> {
					runnerExecutor.execute(descriptor);
					latch.countDown();
				})
				.forEach(executor::execute);
		// @formatter:on
		latch.await();
	}

	/**
	 * Wrapper for {@link Executor} to allow it to be used in a try-with-resources block.
	 */
	@API(status = INTERNAL, since = "5.8")
	protected interface CloseableExecutor extends Executor, AutoCloseable {
		default public void close() throws JUnitException {
		};
	}

	/**
	 * {@link CloseableExecutor} that executes tasks synchronously.
	 *
	 * @since 5.8
	 */
	@API(status = INTERNAL, since = "5.8")
	protected class SerialExecutor implements CloseableExecutor {
		public void execute(final Runnable command) {
			command.run();
		}

	}

	/**
	 * {@link CloseableExecutor} backed by a {@link ForkJoinPool} that executes tasks asynchronously
	 * based on the settings in {@value #PARALLEL_CONFIG}. Clients *must* implement their own logic
	 * to wait for tasks to complete.
	 *
	 * @since 5.8
	 */
	@API(status = INTERNAL, since = "5.8")
	protected class ParallelExecutor implements CloseableExecutor {

		private final ForkJoinPool pool;

		/**
		 * @param parameters test execution configuration
		 */
		public ParallelExecutor(final ConfigurationParameters parameters) {
			final ConfigurationParameters prefixedParameters = new PrefixedConfigurationParameters(parameters,
				PARALLEL_CONFIG);
			final String strategyName = prefixedParameters.get("strategy").orElse("dynamic");
			final ParallelExecutionConfigurationStrategy executionStrategy = DefaultParallelExecutionConfigurationStrategy.valueOf(
				strategyName.toUpperCase());
			final ParallelExecutionConfiguration executionConfiguration = executionStrategy.createConfiguration(
				prefixedParameters);
			pool = new ForkJoinPool(executionConfiguration.getParallelism());
		}

		public void execute(final Runnable command) {
			getPool().execute(command);
		}

		public void close() throws JUnitException {
			final ExecutorService executor = getPool();
			executor.shutdown();
			try {
				executor.awaitTermination(1, TimeUnit.MINUTES);
			}
			catch (final InterruptedException ie) {
				throw new JUnitException("Interrupted while waiting for forked tests to complete; " + ie.getMessage(),
					ie);
			}
		}

		protected ForkJoinPool getPool() {
			return pool;
		}

	}

}
