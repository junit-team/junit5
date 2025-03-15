/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.discovery;

import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.EngineDiscoveryListener;
import org.junit.platform.engine.UniqueId;

/**
 * @since 1.13
 */
class DefaultDiscoveryIssueReporter implements DiscoveryIssueReporter {

	private final EngineDiscoveryListener discoveryListener;
	private final UniqueId engineId;

	DefaultDiscoveryIssueReporter(EngineDiscoveryListener discoveryListener, UniqueId engineId) {
		this.discoveryListener = discoveryListener;
		this.engineId = engineId;
	}

	@Override
	public void reportIssue(DiscoveryIssue issue) {
		this.discoveryListener.issueEncountered(this.engineId, issue);
	}

}
