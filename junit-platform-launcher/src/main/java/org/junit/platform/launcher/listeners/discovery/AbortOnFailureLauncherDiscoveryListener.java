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

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.EngineDiscoveryResult;

/**
 * @since 1.6
 * @see LauncherDiscoveryListeners#abortOnFailure()
 */
class AbortOnFailureLauncherDiscoveryListener extends LoggingLauncherDiscoveryListener {

	@Override
	public void engineDiscoveryFinished(UniqueId engineId, EngineDiscoveryResult result) {
		result.getThrowable().ifPresent(ExceptionUtils::throwAsUncheckedException);
		super.engineDiscoveryFinished(engineId, result);
	}

}
