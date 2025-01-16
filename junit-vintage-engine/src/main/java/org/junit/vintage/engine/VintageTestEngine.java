/*
 * Copyright 2015-2025 the original author or authors.
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
import static org.junit.vintage.engine.Constants.PARALLEL_EXECUTION_ENABLED;
import static org.junit.vintage.engine.Constants.PARALLEL_POOL_SIZE;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.ENGINE_ID;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
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

	private static final Logger logger = LoggerFactory.getLogger(VintageTestEngine.class);

	private static final int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
	private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

	private boolean classes;
	private boolean methods;

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
		executeAllChildren(engineDescriptor, engineExecutionListener, request);
		engineExecutionListener.executionFinished(engineDescriptor, successful());
	}

	private void executeAllChildren(VintageEngineDescriptor engineDescriptor,
			EngineExecutionListener engineExecutionListener, ExecutionRequest request) {
		initializeParallelExecution(request);

		boolean parallelExecutionEnabled = getParallelExecutionEnabled(request);
		if (!parallelExecutionEnabled) {
			executeSequentially(engineDescriptor, engineExecutionListener);
			return;
		}

		if (!classes && !methods) {
			logger.warn(() -> "Parallel execution is enabled but no scope is defined. "
					+ "Falling back to sequential execution.");
			executeSequentially(engineDescriptor, engineExecutionListener);
			return;
		}

		if (executeInParallel(engineDescriptor, engineExecutionListener, request)) {
			Thread.currentThread().interrupt();
		}
	}

	private boolean executeInParallel(VintageEngineDescriptor engineDescriptor,
			EngineExecutionListener engineExecutionListener, ExecutionRequest request) {
		ExecutorService executorService = Executors.newFixedThreadPool(getThreadPoolSize(request));
		RunnerExecutor runnerExecutor = new RunnerExecutor(engineExecutionListener);

		List<RunnerTestDescriptor> runnerTestDescriptors = collectRunnerTestDescriptors(engineDescriptor,
			executorService);

		if (!classes) {
			for (RunnerTestDescriptor runnerTestDescriptor : runnerTestDescriptors) {
				runnerExecutor.execute(runnerTestDescriptor);
			}
			return false;
		}

		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (RunnerTestDescriptor runnerTestDescriptor : runnerTestDescriptors) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(
				() -> runnerExecutor.execute(runnerTestDescriptor), executorService);
			futures.add(future);
		}

		CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
		boolean wasInterrupted = false;
		try {
			allOf.get();
		}
		catch (InterruptedException e) {
			logger.warn(e, () -> "Interruption while waiting for parallel test execution to finish");
			wasInterrupted = true;
		}
		catch (ExecutionException e) {
			throw ExceptionUtils.throwAsUncheckedException(e.getCause());
		}
		finally {
			shutdownExecutorService(executorService);
		}
		return wasInterrupted;
	}

	private RunnerTestDescriptor parallelMethodExecutor(RunnerTestDescriptor runnerTestDescriptor,
			ExecutorService executorService) {
		runnerTestDescriptor.setExecutorService(executorService);

		return runnerTestDescriptor;
	}

	private List<RunnerTestDescriptor> collectRunnerTestDescriptors(VintageEngineDescriptor engineDescriptor,
			ExecutorService executorService) {
		List<RunnerTestDescriptor> runnerTestDescriptors = new ArrayList<>();
		for (TestDescriptor descriptor : engineDescriptor.getModifiableChildren()) {
			RunnerTestDescriptor runnerTestDescriptor = (RunnerTestDescriptor) descriptor;

			if (methods) {
				runnerTestDescriptors.add(parallelMethodExecutor(runnerTestDescriptor, executorService));
				continue;
			}
			runnerTestDescriptors.add(runnerTestDescriptor);
		}
		return runnerTestDescriptors;
	}

	private void shutdownExecutorService(ExecutorService executorService) {
		try {
			executorService.shutdown();
			if (!executorService.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				logger.warn(() -> "Executor service did not terminate within the specified timeout");
				executorService.shutdownNow();
			}
		}
		catch (InterruptedException e) {
			logger.warn(e, () -> "Interruption while waiting for executor service to shut down");
			Thread.currentThread().interrupt();
		}
	}

	private void executeSequentially(VintageEngineDescriptor engineDescriptor,
			EngineExecutionListener engineExecutionListener) {
		RunnerExecutor runnerExecutor = new RunnerExecutor(engineExecutionListener);
		for (Iterator<TestDescriptor> iterator = engineDescriptor.getModifiableChildren().iterator(); iterator.hasNext();) {
			runnerExecutor.execute((RunnerTestDescriptor) iterator.next());
			iterator.remove();
		}
	}

	private boolean getParallelExecutionEnabled(ExecutionRequest request) {
		return request.getConfigurationParameters().getBoolean(PARALLEL_EXECUTION_ENABLED).orElse(false);
	}

	private void initializeParallelExecution(ExecutionRequest request) {
		classes = request.getConfigurationParameters().getBoolean(Constants.PARALLEL_CLASS_EXECUTION).orElse(false);
		methods = request.getConfigurationParameters().getBoolean(Constants.PARALLEL_METHOD_EXECUTION).orElse(false);
	}

	private int getThreadPoolSize(ExecutionRequest request) {
		Optional<String> poolSize = request.getConfigurationParameters().get(PARALLEL_POOL_SIZE);
		if (poolSize.isPresent()) {
			try {
				return Integer.parseInt(poolSize.get());
			}
			catch (NumberFormatException e) {
				logger.warn(() -> "Invalid value for parallel pool size: " + poolSize.get());
			}
		}
		return DEFAULT_THREAD_POOL_SIZE;
	}

}
