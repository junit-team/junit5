/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.engine.EngineDiscoveryListener;
import org.junit.platform.engine.UniqueId;

/**
 * Register a concrete implementation of this interface with a
 * {@link org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder}
 * to be notified of events that occur during test discovery.
 *
 * <p>All methods in this interface have empty <em>default</em> implementations.
 * Concrete implementations may therefore override one or more of these methods
 * to be notified of the selected events.
 *
 * <p>JUnit provides default implementations that are created via the factory
 * methods in
 * {@link org.junit.platform.launcher.listeners.discovery.LauncherDiscoveryListeners}.
 *
 * <p>The methods declared in this interface are called by the {@link Launcher}
 * created via the {@link org.junit.platform.launcher.core.LauncherFactory}
 * during test discovery.
 *
 * @see org.junit.platform.launcher.listeners.discovery.LauncherDiscoveryListeners
 * @see LauncherDiscoveryRequest#getDiscoveryListener()
 * @since 1.6
 */
@API(status = EXPERIMENTAL, since = "1.6")
public abstract class LauncherDiscoveryListener implements EngineDiscoveryListener {

	/**
	 * No-op implementation of {@code LauncherDiscoveryListener}
	 */
	public static final LauncherDiscoveryListener NOOP = new LauncherDiscoveryListener() {
	};

	/**
	 * Called when test discovery is about to be started for an engine.
	 *
	 * @param engineId the unique ID of the engine descriptor
	 */
	public void engineDiscoveryStarted(UniqueId engineId) {
	}

	/**
	 * Called when test discovery has finished for an engine.
	 *
	 * <p>Exceptions thrown by implementations of this method will cause the
	 * complete test discovery to be aborted.
	 *
	 * @param engineId the unique ID of the engine descriptor
	 * @param result the discovery result of the supplied engine
	 * @see EngineDiscoveryResult
	 */
	public void engineDiscoveryFinished(UniqueId engineId, EngineDiscoveryResult result) {
	}

}
