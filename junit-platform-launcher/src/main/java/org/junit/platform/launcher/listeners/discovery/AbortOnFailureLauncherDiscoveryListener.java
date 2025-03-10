/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners.discovery;

import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.engine.SelectorResolutionResult.Status.FAILED;
import static org.junit.platform.engine.SelectorResolutionResult.Status.UNRESOLVED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryIssue;
import org.junit.platform.engine.EngineDiscoveryIssue.Severity;
import org.junit.platform.engine.SelectorResolutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.launcher.EngineDiscoveryResult;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * @since 1.6
 * @see LauncherDiscoveryListeners#abortOnFailure()
 */
class AbortOnFailureLauncherDiscoveryListener implements LauncherDiscoveryListener {

	private final Map<UniqueId, List<EngineDiscoveryIssue>> issuesByEngineId = new LinkedHashMap<>();

	@Override
	public void launcherDiscoveryStarted(LauncherDiscoveryRequest request) {
		this.issuesByEngineId.clear();
	}

	@Override
	public void engineDiscoveryFinished(UniqueId engineId, EngineDiscoveryResult result) {
		result.getThrowable().ifPresent(ExceptionUtils::throwAsUncheckedException);
	}

	@Override
	public void selectorProcessed(UniqueId engineId, DiscoverySelector selector, SelectorResolutionResult result) {
		if (result.getStatus() == FAILED) {
			throw new JUnitException(selector + " resolution failed", result.getThrowable().orElse(null));
		}
		if (result.getStatus() == UNRESOLVED && selector instanceof UniqueIdSelector) {
			UniqueId uniqueId = ((UniqueIdSelector) selector).getUniqueId();
			if (uniqueId.hasPrefix(engineId)) {
				throw new JUnitException(selector + " could not be resolved");
			}
		}
	}

	@Override
	public void issueFound(UniqueId engineId, EngineDiscoveryIssue issue) {
		issuesByEngineId.computeIfAbsent(engineId, __ -> new ArrayList<>()) //
				.add(issue);
	}

	@Override
	public void launcherDiscoveryFinished(LauncherDiscoveryRequest request) {
		String configValue = request.getConfigurationParameters().get(
			LauncherDiscoveryListeners.ABORT_ON_FAILURE_FAILURE_ISSUE_SEVERITIES_CONFIGURATION_PROPERTY_NAME).orElse(
				LauncherDiscoveryListeners.DEFAULT_ABORT_ON_FAILURE_FAILURE_ISSUE_SEVERITIES);
		Set<Severity> failureIssueSeverities = Arrays.stream(configValue.split(",")) //
				.filter(StringUtils::isNotBlank) //
				.map(Severity::valueOf) // TODO case-insensitive parsing
				.collect(toCollection(() -> EnumSet.noneOf(Severity.class)));
		// TODO Collect errors and throw exception or log all issues
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		return getClass() == obj.getClass();
	}

	@Override
	public int hashCode() {
		return 0;
	}

}
