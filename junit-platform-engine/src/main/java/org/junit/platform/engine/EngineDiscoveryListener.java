/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import org.apiguardian.api.API;

@API(status = API.Status.EXPERIMENTAL, since = "1.6")
public interface EngineDiscoveryListener {

	EngineDiscoveryListener NOOP = new EngineDiscoveryListener() {
	};

	default void selectorProcessed(UniqueId engineId, DiscoverySelector selector, SelectorResolutionResult result) {
	}

}
