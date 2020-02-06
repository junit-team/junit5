/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.SelectorResolutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.EngineDiscoveryResult;
import org.junit.platform.launcher.LauncherDiscoveryListener;

/**
 * @since 1.6
 * @see LauncherDiscoveryListeners#composite(List)
 */
class CompositeLauncherDiscoveryListener extends LauncherDiscoveryListener {

	private final List<LauncherDiscoveryListener> listeners;

	CompositeLauncherDiscoveryListener(List<LauncherDiscoveryListener> listeners) {
		this.listeners = Collections.unmodifiableList(new ArrayList<>(listeners));
	}

	@Override
	public void engineDiscoveryStarted(UniqueId engineId) {
		listeners.forEach(delegate -> delegate.engineDiscoveryStarted(engineId));
	}

	@Override
	public void engineDiscoveryFinished(UniqueId engineId, EngineDiscoveryResult result) {
		listeners.forEach(delegate -> delegate.engineDiscoveryFinished(engineId, result));
	}

	@Override
	public void selectorProcessed(UniqueId engineId, DiscoverySelector selector, SelectorResolutionResult result) {
		listeners.forEach(delegate -> delegate.selectorProcessed(engineId, selector, result));
	}
}
