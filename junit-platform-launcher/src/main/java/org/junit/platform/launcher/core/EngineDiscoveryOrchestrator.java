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

import static java.util.stream.Collectors.joining;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.engine.Filter.composeFilters;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.launcher.EngineDiscoveryResult;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryResult.EngineResultInfo;

/**
 * Orchestrates test discovery using the configured test engines.
 *
 * @since 1.7
 */
@API(status = INTERNAL, since = "1.7", consumers = { "org.junit.platform.testkit", "org.junit.platform.suite.engine" })
public class EngineDiscoveryOrchestrator {

	private static final Logger logger = LoggerFactory.getLogger(EngineDiscoveryOrchestrator.class);

	private final EngineDiscoveryResultValidator discoveryResultValidator = new EngineDiscoveryResultValidator();
	private final Iterable<TestEngine> testEngines;
	private final Collection<PostDiscoveryFilter> postDiscoveryFilters;
	private final ListenerRegistry<LauncherDiscoveryListener> launcherDiscoveryListenerRegistry;

	public EngineDiscoveryOrchestrator(Iterable<TestEngine> testEngines,
			Collection<PostDiscoveryFilter> postDiscoveryFilters) {
		this(testEngines, postDiscoveryFilters, ListenerRegistry.forLauncherDiscoveryListeners());
	}

	EngineDiscoveryOrchestrator(Iterable<TestEngine> testEngines, Collection<PostDiscoveryFilter> postDiscoveryFilters,
			ListenerRegistry<LauncherDiscoveryListener> launcherDiscoveryListenerRegistry) {
		this.testEngines = EngineIdValidator.validate(testEngines);
		this.postDiscoveryFilters = postDiscoveryFilters;
		this.launcherDiscoveryListenerRegistry = launcherDiscoveryListenerRegistry;
	}

	/**
	 * Discovers tests for the supplied request in the supplied phase using the
	 * configured test engines.
	 *
	 * <p>Applies {@linkplain org.junit.platform.launcher.EngineFilter engine
	 * filters} and {@linkplain PostDiscoveryFilter post-discovery filters} and
	 * {@linkplain TestDescriptor#prune() prunes} the resulting test tree.
	 */
	public LauncherDiscoveryResult discover(LauncherDiscoveryRequest request, Phase phase) {
		return discover(request, phase, UniqueId::forEngine);
	}

	/**
	 * Discovers tests for the supplied request in the supplied phase using the
	 * configured test engines to be used by the suite engine.
	 *
	 * <p>Applies {@linkplain org.junit.platform.launcher.EngineFilter engine
	 * filters} and {@linkplain PostDiscoveryFilter post-discovery filters} and
	 * {@linkplain TestDescriptor#prune() prunes} the resulting test tree.
	 *
	 * <p>Note: The test descriptors in the discovery result can safely be used
	 * as non-root descriptors. Engine-test descriptor entries are pruned from
	 * the returned result. As such execution by
	 * {@link EngineExecutionOrchestrator} will not emit start or emit events
	 * for engines without tests.
	 */
	public LauncherDiscoveryResult discover(LauncherDiscoveryRequest request, Phase phase, UniqueId parentId) {
		LauncherDiscoveryResult result = discover(request, phase, parentId::appendEngine);
		return result.withRetainedEngines(TestDescriptor::containsTests);
	}

	private LauncherDiscoveryResult discover(LauncherDiscoveryRequest request, Phase phase,
			Function<String, UniqueId> uniqueIdCreator) {
		DiscoveryIssueCollector issueCollector = new DiscoveryIssueCollector();
		LauncherDiscoveryListener listener = getLauncherDiscoveryListener(request, issueCollector);
		LauncherDiscoveryRequest delegatingRequest = new DelegatingLauncherDiscoveryRequest(request) {
			@Override
			public LauncherDiscoveryListener getDiscoveryListener() {
				return listener;
			}
		};
		listener.launcherDiscoveryStarted(request);
		try {
			Map<TestEngine, EngineResultInfo> testEngineResults = discoverSafely(delegatingRequest, phase,
				issueCollector, uniqueIdCreator);
			return new LauncherDiscoveryResult(testEngineResults, request.getConfigurationParameters(),
				request.getOutputDirectoryProvider());
		}
		finally {
			listener.launcherDiscoveryFinished(request);
		}
	}

	private Map<TestEngine, EngineResultInfo> discoverSafely(LauncherDiscoveryRequest request, Phase phase,
			DiscoveryIssueCollector issueCollector, Function<String, UniqueId> uniqueIdCreator) {
		Map<TestEngine, EngineResultInfo> testEngineDescriptors = new LinkedHashMap<>();
		EngineFilterer engineFilterer = new EngineFilterer(request.getEngineFilters());

		for (TestEngine testEngine : this.testEngines) {
			boolean engineIsExcluded = engineFilterer.isExcluded(testEngine);

			if (engineIsExcluded) {
				logger.debug(() -> String.format(
					"Test discovery for engine '%s' was skipped due to an EngineFilter in %s phase.",
					testEngine.getId(), phase));
				continue;
			}

			logger.debug(() -> String.format("Discovering tests during Launcher %s phase in engine '%s'.", phase,
				testEngine.getId()));

			EngineResultInfo engineResult = discoverEngineRoot(testEngine, request, issueCollector, uniqueIdCreator);
			testEngineDescriptors.put(testEngine, engineResult);
		}

		engineFilterer.performSanityChecks();

		List<PostDiscoveryFilter> filters = new LinkedList<>(postDiscoveryFilters);
		filters.addAll(request.getPostDiscoveryFilters());

		applyPostDiscoveryFilters(testEngineDescriptors, filters);
		prune(testEngineDescriptors);

		return testEngineDescriptors;
	}

	private EngineResultInfo discoverEngineRoot(TestEngine testEngine, LauncherDiscoveryRequest request,
			DiscoveryIssueCollector issueCollector, Function<String, UniqueId> uniqueIdCreator) {
		UniqueId uniqueEngineId = uniqueIdCreator.apply(testEngine.getId());
		LauncherDiscoveryListener listener = request.getDiscoveryListener();
		try {
			listener.engineDiscoveryStarted(uniqueEngineId);
			TestDescriptor engineRoot = testEngine.discover(request, uniqueEngineId);
			discoveryResultValidator.validate(testEngine, engineRoot);
			listener.engineDiscoveryFinished(uniqueEngineId, EngineDiscoveryResult.successful());
			return EngineResultInfo.completed(engineRoot, issueCollector.toNotifier());
		}
		catch (Throwable throwable) {
			UnrecoverableExceptions.rethrowIfUnrecoverable(throwable);
			JUnitException cause = null;
			if (throwable instanceof LinkageError) {
				cause = ClasspathAlignmentChecker.check((LinkageError) throwable).orElse(null);
			}
			if (cause == null) {
				String message = String.format("TestEngine with ID '%s' failed to discover tests", testEngine.getId());
				cause = new JUnitException(message, throwable);
			}
			listener.engineDiscoveryFinished(uniqueEngineId, EngineDiscoveryResult.failed(cause));
			return EngineResultInfo.errored(new EngineDescriptor(uniqueEngineId, testEngine.getId()),
				issueCollector.toNotifier(), cause);
		}
	}

	LauncherDiscoveryListener getLauncherDiscoveryListener(LauncherDiscoveryRequest discoveryRequest,
			DiscoveryIssueCollector issueCollector) {
		return ListenerRegistry.copyOf(launcherDiscoveryListenerRegistry) //
				.add(discoveryRequest.getDiscoveryListener()) //
				.add(issueCollector) //
				.getCompositeListener();
	}

	private void applyPostDiscoveryFilters(Map<TestEngine, EngineResultInfo> testEngineDescriptors,
			List<PostDiscoveryFilter> filters) {
		Filter<TestDescriptor> postDiscoveryFilter = composeFilters(filters);
		Map<String, List<TestDescriptor>> excludedTestDescriptorsByReason = new LinkedHashMap<>();
		TestDescriptor.Visitor removeExcludedTestDescriptors = descriptor -> {
			FilterResult filterResult = postDiscoveryFilter.apply(descriptor);
			if (!descriptor.isRoot() && isExcluded(descriptor, filterResult)) {
				populateExclusionReasonInMap(filterResult.getReason(), descriptor, excludedTestDescriptorsByReason);
				descriptor.removeFromHierarchy();
			}
		};
		acceptInAllTestEngines(testEngineDescriptors, removeExcludedTestDescriptors);
		logTestDescriptorExclusionReasons(excludedTestDescriptorsByReason);
	}

	private void populateExclusionReasonInMap(Optional<String> reason, TestDescriptor testDescriptor,
			Map<String, List<TestDescriptor>> excludedTestDescriptorsByReason) {
		excludedTestDescriptorsByReason.computeIfAbsent(reason.orElse("Unknown"), list -> new LinkedList<>()).add(
			testDescriptor);
	}

	private void logTestDescriptorExclusionReasons(Map<String, List<TestDescriptor>> excludedTestDescriptorsByReason) {
		excludedTestDescriptorsByReason.forEach((exclusionReason, testDescriptors) -> {
			String displayNames = testDescriptors.stream().map(TestDescriptor::getDisplayName).collect(joining(", "));
			long containerCount = testDescriptors.stream().filter(TestDescriptor::isContainer).count();
			long methodCount = testDescriptors.stream().filter(TestDescriptor::isTest).count();
			logger.config(() -> String.format("%d containers and %d tests were %s", containerCount, methodCount,
				exclusionReason));
			logger.debug(
				() -> String.format("The following containers and tests were %s: %s", exclusionReason, displayNames));
		});
	}

	/**
	 * Prune all branches in the tree of {@link TestDescriptor TestDescriptors}
	 * that do not have executable tests.
	 *
	 * <p>If a {@link TestEngine} ends up with no {@code TestDescriptors} after
	 * pruning, it will <strong>not</strong> be removed.
	 */
	private void prune(Map<TestEngine, EngineResultInfo> testEngineResults) {
		acceptInAllTestEngines(testEngineResults, TestDescriptor::prune);
	}

	private boolean isExcluded(TestDescriptor descriptor, FilterResult filterResult) {
		return descriptor.getChildren().isEmpty() && filterResult.excluded();
	}

	private void acceptInAllTestEngines(Map<TestEngine, EngineResultInfo> testEngineResults,
			TestDescriptor.Visitor visitor) {
		testEngineResults.values().forEach(result -> result.getRootDescriptor().accept(visitor));
	}

	public enum Phase {
		DISCOVERY, EXECUTION;

		@Override
		public String toString() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

}
