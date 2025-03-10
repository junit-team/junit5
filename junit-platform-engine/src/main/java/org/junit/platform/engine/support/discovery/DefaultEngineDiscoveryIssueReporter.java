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

import org.junit.platform.engine.EngineDiscoveryIssue;
import org.junit.platform.engine.EngineDiscoveryListener;
import org.junit.platform.engine.UniqueId;

class DefaultEngineDiscoveryIssueReporter implements EngineDiscoveryIssueReporter {

	private final EngineDiscoveryListener discoveryListener;
	private final UniqueId engineId;

	public DefaultEngineDiscoveryIssueReporter(EngineDiscoveryListener discoveryListener, UniqueId engineId) {
		this.discoveryListener = discoveryListener;
		this.engineId = engineId;
	}

	@Override
	public void reportIssue(EngineDiscoveryIssue issue) {
		this.discoveryListener.issueFound(this.engineId, issue);
	}

}
