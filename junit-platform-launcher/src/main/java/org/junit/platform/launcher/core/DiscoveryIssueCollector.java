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

import static org.junit.platform.engine.SelectorResolutionResult.Status.FAILED;
import static org.junit.platform.engine.SelectorResolutionResult.Status.UNRESOLVED;

import java.util.ArrayList;
import java.util.List;

import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.SelectorResolutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.launcher.LauncherDiscoveryListener;

class DiscoveryIssueCollector implements LauncherDiscoveryListener {

	final List<DiscoveryIssue> issues = new ArrayList<>();

	@Override
	public void engineDiscoveryStarted(UniqueId engineId) {
		this.issues.clear();
	}

	@Override
	public void selectorProcessed(UniqueId engineId, DiscoverySelector selector, SelectorResolutionResult result) {
		if (result.getStatus() == FAILED) {
			this.issues.add(DiscoveryIssue.builder(Severity.ERROR, selector + " resolution failed") //
					.cause(result.getThrowable()) //
					.build());
		}
		else if (result.getStatus() == UNRESOLVED && selector instanceof UniqueIdSelector) {
			UniqueId uniqueId = ((UniqueIdSelector) selector).getUniqueId();
			if (uniqueId.hasPrefix(engineId)) {
				this.issues.add(DiscoveryIssue.create(Severity.ERROR, selector + " could not be resolved"));
			}
		}
	}

	@Override
	public void issueEncountered(UniqueId engineId, DiscoveryIssue issue) {
		this.issues.add(issue);
	}

	DiscoveryIssueNotifier toNotifier() {
		if (issues.isEmpty()) {
			return DiscoveryIssueNotifier.NO_ISSUES;
		}
		Severity criticalSeverity = Severity.ERROR; // TODO #242 - make this configurable
		return DiscoveryIssueNotifier.from(criticalSeverity, issues);
	}
}
