/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * {@code EngineDiscoveryListener} contains {@link TestEngine} access to the
 * information necessary to discover tests and containers.
 *
 * <p>All methods in this interface have empty <em>default</em> implementations.
 * Concrete implementations may therefore override one or more of these methods
 * to be notified of the selected events.
 *
 * <p>The methods declared in this interface <em>should</em> be called by
 * each {@link TestEngine} during test discovery. However, since this interface
 * was only added in 1.6, older engines might not yet do so.
 *
 * @since 1.6
 * @see EngineDiscoveryRequest#getDiscoveryListener()
 */
@API(status = STABLE, since = "1.10")
public interface EngineDiscoveryListener {

	/**
	 * No-op implementation of {@code EngineDiscoveryListener}
	 */
	EngineDiscoveryListener NOOP = new EngineDiscoveryListener() {
	};

	/**
	 * Must be called after a discovery selector has been processed by a test
	 * engine.
	 *
	 * <p>Exceptions thrown by implementations of this method will cause test
	 * discovery of the current engine to be aborted.
	 *
	 * @param engineId the unique ID of the engine descriptor
	 * @param selector the processed selector
	 * @param result the resolution result of the supplied engine and selector
	 * @see SelectorResolutionResult
	 */
	default void selectorProcessed(UniqueId engineId, DiscoverySelector selector, SelectorResolutionResult result) {
	}

}
