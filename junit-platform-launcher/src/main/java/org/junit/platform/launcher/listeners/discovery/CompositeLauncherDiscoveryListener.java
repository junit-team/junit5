/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.SelectorResolutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.LauncherDiscoveryListener;

class CompositeLauncherDiscoveryListener implements LauncherDiscoveryListener {

	private final List<LauncherDiscoveryListener> listeners;

	CompositeLauncherDiscoveryListener(List<LauncherDiscoveryListener> listeners) {
		this.listeners = new ArrayList<>(listeners);
	}

	@Override
	public void engineDiscoveryStarted(UniqueId engineId) {
		listeners.forEach(delegate -> delegate.engineDiscoveryStarted(engineId));
	}

	@Override
	public void engineDiscoveryFinished(UniqueId engineId, Optional<Throwable> failure) {
		listeners.forEach(delegate -> delegate.engineDiscoveryFinished(engineId, failure));
	}

	@Override
	public void selectorProcessed(UniqueId engineId, DiscoverySelector selector, SelectorResolutionResult result) {
		listeners.forEach(delegate -> delegate.selectorProcessed(engineId, selector, result));
	}
}
