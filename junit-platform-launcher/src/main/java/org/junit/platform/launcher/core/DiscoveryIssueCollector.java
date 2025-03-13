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

import java.util.ArrayList;
import java.util.List;

import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryListener;

class DiscoveryIssueCollector implements LauncherDiscoveryListener {

	final List<DiscoveryIssue> issues = new ArrayList<>();

	@Override
	public void engineDiscoveryStarted(UniqueId engineId) {
		this.issues.clear();
	}

	@Override
	public void issueEncountered(UniqueId engineId, DiscoveryIssue issue) {
		this.issues.add(issue);
	}
}
